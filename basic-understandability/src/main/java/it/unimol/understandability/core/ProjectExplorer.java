package it.unimol.understandability.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentIterator;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Explores an IntelliJ project
 *
 * Created by simone on 07/12/16.
 */
public class ProjectExplorer {
    private static Logger LOG = Logger.getInstance(ProjectExplorer.class);
    private static ProjectExplorer instance;

    public static ProjectExplorer getInstance() {
        if (instance == null)
            instance = new ProjectExplorer();

        return instance;
    }

    private ProjectExplorer() {

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
}
