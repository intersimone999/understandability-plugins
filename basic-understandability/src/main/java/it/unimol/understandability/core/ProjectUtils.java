package it.unimol.understandability.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;

import java.io.File;
import java.util.*;

/**
 * Explores an IntelliJ project
 *
 * Created by simone on 07/12/16.
 */
public class ProjectUtils {
    private static Logger LOG = Logger.getInstance(ProjectUtils.class);
    private static ProjectUtils instance;

    public static ProjectUtils getInstance() {
        if (instance == null)
            instance = new ProjectUtils();

        return instance;
    }

    private Map<Project, Set<PsiElement>> projectElements;

    private ProjectUtils() {
        this.projectElements = new HashMap<>();
    }

    public List<PsiClass> getClassesFromFile(PsiFile psiFile) {
        List<PsiClass> classes = new ArrayList<>();
        if (psiFile != null) {
            if (psiFile instanceof PsiJavaFile) {
                PsiJavaFile javaClass = (PsiJavaFile) psiFile;
                for (PsiClass jclass : javaClass.getClasses()) {
                    classes.add(jclass);
                }
            }
        }

        return classes;
    }

    public List<PsiClass> getClassesFromPackage(PsiDirectory directory) {
        List<PsiClass> classes = new ArrayList<>();

        ProjectRootManager.getInstance(directory.getProject()).getFileIndex().iterateContentUnderDirectory(directory.getVirtualFile(), new ContentIterator() {
            @Override
            public boolean processFile(VirtualFile file) {
                if (!file.isDirectory()) {
                    PsiFile psiFile = (PsiFile)PsiUtils.virtualFileToPsi(directory.getProject(), file);
                    classes.addAll(getClassesFromFile(psiFile));
                }
                return true;
            }
        });

        return classes;
    }

    public List<PsiClass> getClassesFromVirtualFile(Project project, VirtualFile file) {
        if (file.isDirectory()) {
            PsiDirectory directory = PsiManager.getInstance(project).findDirectory(file);
            return this.getClassesFromPackage(directory);
        } else {
            PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
            return this.getClassesFromFile(psiFile);
        }
    }

    public List<PsiClass> getAllClasses(Project project) {
        VirtualFile[] contentSourceRoots = ProjectRootManager.getInstance(project).getContentSourceRoots();
        List<PsiClass> classes = new ArrayList<>();
        for (VirtualFile root : contentSourceRoots) {
            ProjectRootManager.getInstance(project).getFileIndex().iterateContentUnderDirectory(root, new ContentIterator() {
                @Override
                public boolean processFile(VirtualFile file) {
                    if (!file.isDirectory()) {
                        PsiFile psiFile = (PsiFile)PsiUtils.virtualFileToPsi(project, file);
                        classes.addAll(getClassesFromFile(psiFile));
                    }
                    return true;
                }
            });
        }

        return classes;
    }

    public Set<PsiElement> getAllProjectElements(Project project) {
        return this.getAllProjectElements(project, null);
    }

    public Set<PsiElement> getAllProjectElements(Project project, ProgressIndicator indicator) {
        if (this.projectElements.containsKey(project))
            return this.projectElements.get(project);

        if (ReferenceGraphBuilder.getInstance().isReady(project))
            return ReferenceGraphBuilder.getInstance().getReferenceGraph(project).vertexSet();

        List<PsiClass> allClasses = this.getAllClasses(project);
        Set<PsiElement> allElements = new HashSet<>();

        double done = 0.0;
        for (PsiClass aClass : allClasses) {
            aClass.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(PsiElement element) {
                    //Takes the class/method under analysis
                    if (element instanceof PsiMethod || element instanceof PsiClass)
                        allElements.add(element);

                    if (element instanceof PsiClass) {
                        PsiElement superClass = ((PsiClass) element).getSuperClass();

                        allElements.add(superClass);
                    }

                    //Takes all the references in the class/method
                    for (PsiReference reference : element.getReferences()) {
                        PsiElement referredElement = reference.resolve();

                        if (referredElement == null)
                            continue;

                        if (referredElement instanceof PsiMethod ||
                                referredElement instanceof PsiClass) {
                            allElements.add(referredElement);
                        }
                    }

                    super.visitElement(element);
                }
            });

            done++;
            if (indicator != null) {
                indicator.setFraction(done / allClasses.size());
            }
        }

        this.projectElements.put(project, allElements);
        return allElements;
    }

    public File getProjectFile(Project project, String relativePath) {
        return new File(project.getBaseDir().getPath(), relativePath);
    }
}
