package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.calculator.MethodUnderstandabilityCalculator;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * It is required a CSV file with a list of snippets (full method signature)
 *
 * Created by simone on 01/02/17.
 */
public class SelectSnippetsTask extends MyTask {
    private static int classId;
    private static Map<String, Integer> classToId;

    private static int methodId;
    private static Map<String, Integer> methodToId;

    static {
        classToId = new HashMap<>();
        classId = 0;

        methodToId = new HashMap<>();
        methodId = 0;
    }

    private static int getClassUniqueId(PsiClass psiClass) {
        String signature = psiClass.getQualifiedName();

        if (!classToId.containsKey(signature))
            classToId.put(signature, classId++);
        return classToId.get(signature);
    }

    private static int getMethodUniqueId(PsiMethod psiMethod) {
        String signature = PsiUtils.getSignature(psiMethod);
        if (!methodToId.containsKey(signature))
            methodToId.put(signature, methodId++);
        Logger.getInstance(SelectSnippetsTask.class).warn(signature + " => " + methodToId.get(signature));
        return methodToId.get(signature);
    }

    @Override
    public List<MyTask> getDependencyTasks() {
        List<MyTask> tasks = new ArrayList<>();
        tasks.add(new GatherExternalDependenciesTask(project));
        tasks.add(new DownloadExternalDocumentationScoreTask(project));
        return tasks;
    }

    private List<String> allSnippetsSignatures;
    private Set<String> alreadyAddedClasses;
    private List<String> queries;
    private File outputFile;
    private int maxDepth;

    public SelectSnippetsTask(Project project, int maxDepth, File snippetsFile, File outputFile, int startingMethodId, int startingClassId) throws IOException {
        super(project, "Exporting snippets to SQL file...", project.getBaseDir());

        String content = FileUtils.readFileToString(snippetsFile, "UTF-8");

        String[] snippets = content.split("\n");
        this.allSnippetsSignatures = new ArrayList<>();
        for (String snippet : snippets) {
            String[] parts = snippet.split(";");
            this.allSnippetsSignatures.add(parts[0]);
        }

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

            double done = 0.0;
            double total = allClasses.size();
            int numberOfFoundSnippets = 0;
            Map<String, String> mostSimilar = new HashMap<>();
            Set<String> missingMethods = new HashSet<>(allSnippetsSignatures);

            for (String signature : allSnippetsSignatures) {
                mostSimilar.put(signature, "");
            }

            Logger.getInstance(SelectSnippetsTask.class).info("Analyzing " + allClasses.size() + "...");
            Logger.getInstance(SelectSnippetsTask.class).info("For example: " + allClasses.stream().findFirst() );
            for (PsiClass psiClass : allClasses) {
                progressIndicator.setFraction(done++ / total);
                System.out.println(done / total);
//                String qualified = psiClass.getQualifiedName();
//                if (qualified == null)
//                    continue;
//
//                boolean ahead = false;
//                for (String signature : allSnippetsSignatures)
//                    if (signature.startsWith(qualified))
//                        ahead = true;
//
//                if (!ahead)
//                    continue;
                for (PsiMethod method : psiClass.getMethods()) {
                    String methodSignature;
                    try {
                        methodSignature = PsiUtils.getSignature(method);
                    } catch (RuntimeException e) {
                        Logger.getInstance(SelectSnippetsTask.class).warn(e);
                        continue;
                    }
//                    System.out.println(methodSignature);
                    for (Map.Entry<String, String> entry : mostSimilar.entrySet()) {
                        int similarityOld = similarity(entry.getKey(), entry.getValue());
                        int similarityNew = similarity(entry.getKey(), methodSignature);

                        if (similarityNew > similarityOld)
                            mostSimilar.put(entry.getKey(), methodSignature);
                    }
                    if (allSnippetsSignatures.contains(methodSignature)) {
                        missingMethods.remove(methodSignature);
                        Snippet snippet = new Snippet();
                        snippet.id = getMethodUniqueId(method);
                        snippet.qualifiedName = methodSignature;
                        snippet.content = method.getText();
                        snippet.understandability = 0.0;//understandabilityCalculator.computeUnderstandability(method);
                        snippet.systemName = this.project.getName();
                        snippet.addRelated(psiClass);
                        toAnalyze.add(new TempPsiClass(psiClass, 1));

                        visitor.setSnippet(snippet);
                        method.accept(visitor);

                        addSnippetInDB(snippet);
                        numberOfFoundSnippets++;
                    }
                }
            }

            if (numberOfFoundSnippets != allSnippetsSignatures.size()) {
                String message = "";
                for (String missingMethod : missingMethods) {
                    message += "; " + missingMethod + "; most similar: \"" + mostSimilar.get(missingMethod) + "\"";
                }

                throw new RuntimeException("Unable to find some snippets: " + message);
            }

            while (!toAnalyze.isEmpty()) {
                progressIndicator.setFraction(1 - (toAnalyze.size() / (toAnalyze.size() +1)));
                System.out.println(toAnalyze.size());
                TempPsiClass element = toAnalyze.poll();
                if (alreadyAddedClasses.contains(element.psiClass.getQualifiedName()))
                    continue;

                if (element.psiClass.getText().length() < 30) {
                    alreadyAddedClasses.add(element.psiClass.getQualifiedName());
                    continue;
                }

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

                this.alreadyAddedClasses.add(element.psiClass.getQualifiedName());
            }

            try {
                FileWriter writer = new FileWriter(this.outputFile);
                PrintWriter printer = new PrintWriter(writer);
                for (String query : this.queries)
                    printer.println(query + ";");
                printer.close();
                writer.close();

                //Stores the values
                UnderstandabilityPreferences.setSnippetId(methodId);
                UnderstandabilityPreferences.setClassId(classId);
            } catch (IOException e) {
                System.out.println("An error occurred while saving the SQL file");
            }

        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }

    private int similarity(String a, String b) {
        int minLength = a.length() < b.length() ? a.length() : b.length();

        int similarity = 0;
        for (int i = 0; i < minLength; i++) {
            if (a.charAt(i) == b.charAt(i))
                similarity++;
            else
                return similarity;
        }

        return similarity;
    }

    private void addSnippetInDB(Snippet element) {
        String query = "INSERT INTO `snippet` (`id`, `title`, `text_to_show`, `system_name`, `understandability`, `related_resources`) VALUES (" +
                element.id + ", " +
                "'" + escape(element.qualifiedName) + "', " +
                "'" + escape(element.content) + "', " +
                "'" + escape(element.systemName) + "', " +
                "" + element.understandability + ", " +
                "'" + escape(element.relatedToString()) + "')";

        this.queries.add(query);
    }

    private void addClassInDB(Snippet element) {
        String query = "INSERT INTO `classes` (`id`, `text_to_show`, `system_name`, `related_resources`) VALUES (" +
                element.id + ", " +
                "'" + escape(element.content) + "', " +
                "'" + escape(element.systemName) + "', " +
                "'" + escape(element.relatedToString()) + "')";

        this.queries.add(query);
    }

    private String escape(String a) {
        return a.replace("\\", "\\\\").replace("'", "''").replace("\n", "\\n");
    }

    class Visitor extends PsiRecursiveElementVisitor {
        private Queue<TempPsiClass> toAnalyze;
        private Set<String> analyzedClasses;
        private Snippet snippet;

        public Visitor(Queue<TempPsiClass> toAnalyze) {
            this.toAnalyze = toAnalyze;
            this.analyzedClasses = new HashSet<>();
        }

        public void setAnalyzedClasses(Set<String> analyzedClasses) {
            this.analyzedClasses = analyzedClasses;
        }

        void setSnippet(Snippet snippet) {
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
                    TempPsiClass toAdd = new TempPsiClass((PsiClass)referredElement, this.snippet.distance + 1);
//                    if (!toAnalyze.contains(toAdd))
                    toAnalyze.add(toAdd);
                }

                if (referredElement instanceof PsiMethod) {
                    if (!PsiUtils.isSourceElement((PsiMethod) referredElement))
                        continue;

                    this.snippet.addRelated(((PsiMethod)referredElement).getContainingClass());
                    PsiClass containing =((PsiMethod)referredElement).getContainingClass();

                    TempPsiClass toAdd = new TempPsiClass(containing, this.snippet.distance + 1);
//                    if (!toAnalyze.contains(toAdd))
                    toAnalyze.add(toAdd);
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

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof TempPsiClass))
                return false;

            TempPsiClass tempPsiClass = (TempPsiClass)o;

            if (tempPsiClass.psiClass.getQualifiedName() == null || psiClass.getQualifiedName() == null)
                return false;

            return tempPsiClass.psiClass.getQualifiedName().equals(psiClass.getQualifiedName()) && distance != tempPsiClass.distance;
        }
    }
}
