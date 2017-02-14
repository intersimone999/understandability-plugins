package it.unimol.understandability.core.structures;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.PsiMethodImpl;
import com.intellij.psi.util.PsiTreeUtil;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.core.PsiUtils;
import org.jgrapht.graph.DefaultEdge;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by simone on 09/12/16.
 */
public class ReferenceGraphBuilder {
    private Map<PsiFileSystemItem, ReferenceGraph> referenceGraphs;
    private static ReferenceGraphBuilder instance;

    public static ReferenceGraphBuilder getInstance() {
        if (instance == null)
            instance = new ReferenceGraphBuilder();

        return instance;
    }

    private ProgressIndicator progressIndicator;

    private ReferenceGraphBuilder() {
        this.referenceGraphs = new HashMap<>();
    }

    public boolean isReady(Project project) {
        PsiFileSystemItem root = PsiUtils.virtualFileToPsi(project, project.getBaseDir());

        return this.referenceGraphs.containsKey(root);
    }

    public ReferenceGraph getReferenceGraph(Project project) {
        PsiFileSystemItem root = PsiUtils.virtualFileToPsi(project, project.getBaseDir());
        if (!this.referenceGraphs.containsKey(root)) {
            ReferenceGraph newReferenceGraph = this.buildReferenceGraphFromScratch(root);

            this.referenceGraphs.put(root, newReferenceGraph);

            return newReferenceGraph;
        }

        return this.referenceGraphs.get(root); //this.justUpdateReferenceGraph(project, this.referenceGraphs.get(project));
    }

    public ReferenceGraph getReferenceGraph(PsiFileSystemItem rootFile) {
        if (!this.referenceGraphs.containsKey(rootFile)) {
            ReferenceGraph newReferenceGraph = this.buildReferenceGraphFromScratch(rootFile);

            this.referenceGraphs.put(rootFile, newReferenceGraph);

            return newReferenceGraph;
        }

        return this.referenceGraphs.get(rootFile); //this.justUpdateReferenceGraph(project, this.referenceGraphs.get(project));
    }

    private ReferenceGraph buildReferenceGraphFromScratch(PsiFileSystemItem rootFile) {
        ReferenceGraph referenceGraph = new ReferenceGraph(DefaultEdge.class);

        return this.justUpdateReferenceGraph(rootFile, referenceGraph);
    }

    private ReferenceGraph justUpdateReferenceGraph(PsiFileSystemItem rootFile, ReferenceGraph referenceGraph) {
        List<PsiClass> allClasses = ProjectUtils.getInstance().getClassesFromVirtualFile(rootFile.getProject(), rootFile.getVirtualFile());
        double classes = allClasses.size();
        double doneClasses = -1D;

        RecursiveReferenceVisitor visitor = new RecursiveReferenceVisitor(referenceGraph);

        for (PsiClass aClass : allClasses) {
            doneClasses++;
            this.updateProgress(doneClasses/classes);

            visitor.setCurrentReferringElement(aClass);
            aClass.accept(visitor);

            for (PsiMethod method : aClass.getMethods()) {
                visitor.setCurrentReferringElement(method);

//                referenceGraph.registerReference(method, aClass);

                if (method instanceof PsiMethodImpl) {
                    PsiMethod[] superMethods = method.findSuperMethods();
                    if (superMethods.length > 0) {
                        PsiMethod directlySuper = superMethods[superMethods.length - 1];

                        referenceGraph.registerReference(method, directlySuper);
                    }
                }

                method.accept(visitor);
            }
        }

        return referenceGraph;
    }

    public void createReferenceGraph(PsiFileSystemItem root, ProgressIndicator progressIndicator) {
        this.progressIndicator = progressIndicator;
        this.getReferenceGraph(root);
        this.progressIndicator = null;
    }

    private void updateProgress(double progress) {
        if (this.progressIndicator != null)
            progressIndicator.setFraction(progress);
    }

    class RecursiveReferenceVisitor extends PsiRecursiveElementVisitor {
        private ReferenceGraph graph;
        private PsiElement referringElement;

        public RecursiveReferenceVisitor(ReferenceGraph referenceGraph) {
            this.graph = referenceGraph;
        }

        public void setCurrentReferringElement(PsiElement referringElement) {
            this.referringElement = referringElement;
        }

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiMethod && !element.equals(this.referringElement))
                return;

            if (element instanceof PsiThisExpression) {
                PsiJavaCodeReferenceElement qualifier = ((PsiThisExpression) element).getQualifier();

                PsiClass thisClass = (qualifier == null ? PsiTreeUtil.getParentOfType(element, PsiClass.class, true) : ((PsiClass)qualifier.resolve()));

                graph.registerReference(referringElement, thisClass);
            }

            if (element instanceof PsiSuperExpression) {
                PsiJavaCodeReferenceElement qualifier = ((PsiSuperExpression) element).getQualifier();

                if (qualifier != null) {
                    PsiClass superClass = (PsiClass) qualifier.resolve();

                    graph.registerReference(referringElement, superClass);
                }
            }

            for (PsiReference reference : element.getReferences()) {
                PsiElement referredElement = reference.resolve();

//                if (referredElement instanceof PsiClass) {
//                    if (((PsiClass) referredElement).isAnnotationType())
//                        continue;
//                }

                if (referredElement == null)
                    continue;

                if (referredElement instanceof PsiMethod ||
                        referredElement instanceof PsiClass) {
                    graph.registerReference(referringElement, referredElement);
                }
            }

            super.visitElement(element);
        }
    }
}
