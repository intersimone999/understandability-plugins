package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.calculator.MethodUnderstandabilityCalculator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * It is required a CSV file with a list of snippets (full method signature)
 *
 * Created by simone on 01/02/17.
 */
public class SelectSnippetsTask extends MyTask {
    private static int classId;
    private static Map<PsiClass, Integer> classToId;

    private static int methodId;
    private static Map<PsiMethod, Integer> methodToId;

    static {
        classToId = new HashMap<>();
        classId = 0;

        methodToId = new HashMap<>();
        methodId = 0;
    }

    private static int getClassUniqueId(PsiClass psiClass) {
        if (!classToId.containsKey(psiClass))
            classToId.put(psiClass, classId++);
        return classToId.get(psiClass);
    }

    private static int getMethodUniqueId(PsiMethod psiMethod) {
        if (!methodToId.containsKey(psiMethod))
            methodToId.put(psiMethod, methodId++);
        return methodToId.get(psiMethod);
    }

    private List<String> allSnippetsSignatures;
    private Set<PsiClass> alreadyAddedClasses;
    private List<String> queries;
    private File outputFile;
    private int maxDepth;

    public SelectSnippetsTask(Project project, int maxDepth, File snippetsFile, File outputFile, int startingMethodId, int startingClassId) throws IOException {
        super(project, "Exporting snippets to SQL file...", project.getBaseDir());

        String content = FileUtils.readFileToString(snippetsFile, "UTF-8");

        String[] snippets = content.split("\n");
        this.allSnippetsSignatures = Arrays.asList(snippets);

        this.outputFile = outputFile;

        this.maxDepth = maxDepth;

        this.alreadyAddedClasses = new HashSet<>();
        this.queries = new ArrayList<>();

        methodId = startingMethodId;
        classId = startingClassId;
    }

    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        this.queries.clear();
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

            List<PsiClass> allClasses = ProjectUtils.getInstance().getClassesFromVirtualFile(this.project, this.root);

            MethodUnderstandabilityCalculator understandabilityCalculator = new MethodUnderstandabilityCalculator(3);

            Queue<TempPsiClass> toAnalyze = new LinkedList<>();
            Visitor visitor = new Visitor(toAnalyze);

            for (PsiClass psiClass : allClasses) {
                for (PsiMethod method : psiClass.getMethods()) {
                    String methodSignature = PsiUtils.getSignature(method);
                    if (allSnippetsSignatures.contains(methodSignature)) {
                        Snippet snippet = new Snippet();
                        snippet.id = getMethodUniqueId(method);
                        snippet.qualifiedName = null;
                        snippet.content = method.getText();
                        snippet.understandability = understandabilityCalculator.computeUnderstandability(method);
                        snippet.systemName = this.project.getName();

                        visitor.setSnippet(snippet);
                        method.accept(visitor);

                        addSnippetInDB(snippet);
                    }
                }
            }

            while (!toAnalyze.isEmpty()) {
                TempPsiClass element = toAnalyze.poll();
                if (alreadyAddedClasses.contains(element.psiClass))
                    continue;

                Snippet snippet = new Snippet();
                snippet.id = getClassUniqueId(element.psiClass);
                snippet.qualifiedName = element.psiClass.getQualifiedName();
                snippet.content = element.psiClass.getText();
                snippet.distance = element.distance;
                snippet.systemName = this.project.getName();

                if (snippet.distance <= this.maxDepth) {
                    visitor.setSnippet(snippet);
                    element.psiClass.accept(visitor);
                }

                addClassInDB(snippet);

                this.alreadyAddedClasses.add(element.psiClass);
            }

            try {
                FileUtils.write(this.outputFile, StringUtils.join(this.queries, ";\n"), "UTF-8");
            } catch (IOException e) {
                System.out.println("An error occurred while saving the SQL file");
            }

        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }

    private void addSnippetInDB(Snippet element) {
        String query = "INSERT INTO `snippet` (`id`, `text_to_show`, `system_name`, `understandability`, `related_resources`) VALUES (" +
                element.id + ", " +
                "'" + element.content.replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n") + "', " +
                "'" + element.systemName.replaceAll("'", "\\\\'") + "', " +
                "" + element.understandability + ", " +
                "'" + element.relatedToString().replaceAll("'", "\\\\'") + "')";

        this.queries.add(query);
    }

    private void addClassInDB(Snippet element) {
        String query = "INSERT INTO `classes` (`id`, `text_to_show`, `system_name`, `related_resources`) VALUES (" +
                element.id + ", " +
                "'" + element.content.replaceAll("'", "\\\\'").replaceAll("\n", "\\\\n") + "', " +
                "'" + element.systemName.replaceAll("'", "\\\\'") + "', " +
                "'" + element.relatedToString().replaceAll("'", "\\\\'") + "')";

        this.queries.add(query);
    }

    class Visitor extends PsiRecursiveElementVisitor {
        private Queue<TempPsiClass> toAnalyze;
        private Snippet snippet;

        public Visitor(Queue<TempPsiClass> toAnalyze) {
            this.toAnalyze = toAnalyze;
        }

        public void setSnippet(Snippet snippet) {
            this.snippet = snippet;
        }

        @Override
        public void visitElement(PsiElement element) {
            for (PsiReference reference : element.getReferences()) {
                PsiElement referredElement = reference.resolve();

                if (referredElement instanceof PsiClass) {
                    if (((PsiClass) referredElement).isAnnotationType())
                        continue;

                    if (!PsiUtils.isSourceElement((PsiClass) referredElement))
                        continue;

                    this.snippet.addRelated((PsiClass)referredElement);
                    toAnalyze.add(new TempPsiClass((PsiClass)referredElement, this.snippet.distance + 1));
                }

                if (referredElement instanceof PsiMethod) {
                    if (!PsiUtils.isSourceElement((PsiMethod) referredElement))
                        continue;

                    this.snippet.addRelated(((PsiMethod)referredElement).getContainingClass());
                    PsiClass containing =((PsiMethod)referredElement).getContainingClass();
                    toAnalyze.add(new TempPsiClass(containing, this.snippet.distance + 1));
                }
            }

            super.visitElement(element);
        }
    }

    class Snippet {
        public int id;
        public String qualifiedName;
        public String content;
        public String systemName;
        public double understandability;
        public Set<PsiClass> related;
        public int distance;

        public Snippet() {
            this.related = new HashSet<>();
        }

        public void addRelated(PsiClass psiClass) {
            this.related.add(psiClass);
        }

        public String relatedToString() {
            String pattern = "<a href=\"showClass.jsp?id=%d&title=%s\" target=\"_blank\">%s</a><br/> ";

            String result = "";

            for (PsiClass related : related) {
                if (related.getQualifiedName() == null || related.getQualifiedName().equals(this.qualifiedName))
                    continue;
                result += String.format(pattern, getClassUniqueId(related), related.getQualifiedName(), related.getQualifiedName());
            }

            return result;
        }
    }

    class TempPsiClass {
        public PsiClass psiClass;
        public int distance;

        public TempPsiClass(PsiClass psiClass, int distance) {
            this.psiClass = psiClass;
            this.distance = distance;
        }
    }
}
