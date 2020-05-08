package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.discovery.ProgramDiscoverer;
import it.unimol.understandability.core.structures.ReferenceGraph;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import it.unimol.understandability.ui.panels.TestDialog;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by simone on 07/12/16.
 */
public class OpenStartingClassesTask extends MyTask {
    public OpenStartingClassesTask(Project project) {
        this(project, project.getBaseDir());
    }

    public OpenStartingClassesTask(Project project, VirtualFile root) {
        super(project, "Opening starting classes...", root);
    }

    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();
            PsiFileSystemItem psiItem = PsiUtils.virtualFileToPsi(project, root);
            ReferenceGraph graph = ReferenceGraphBuilder.getInstance().getReferenceGraph(psiItem);

            ProgramDiscoverer discoverer = new ProgramDiscoverer(project, graph);
//            Set<PsiElement> topElements = discoverer.discover(10);
            List<PsiElement> topElements = discoverer.getStartingPointsByPopularity(10);

            DialogBuilder builder = new DialogBuilder(project);
            builder.setTitle("Close files");
            builder.setDimensionServiceKey("FrameSwitcherCloseProjects");
            builder.removeAllActions();
            builder.addOkAction();
            builder.addCancelAction();
            builder.setCenterPanel(new TestDialog());
            builder.setErrorText("Do you want to close all the files?");

            ApplicationManager.getApplication().invokeLater(() -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    boolean isOk = builder.show() == DialogWrapper.OK_EXIT_CODE;

                    if (!isOk)
                        return;

                    for (VirtualFile openFile : FileEditorManager.getInstance(project).getOpenFiles()) {
                        FileEditorManager.getInstance(project).closeFile(openFile);
                    }

                    for (PsiElement element : topElements) {
                        LOG.warn(PsiUtils.toString(element));
                        ApplicationManager.getApplication().invokeLater(() -> {
                            ApplicationManager.getApplication().runWriteAction(() -> {

                                VirtualFile toOpen = null;
                                if (element instanceof PsiClass) {
                                    toOpen = element.getContainingFile().getVirtualFile();
                                } else if (element instanceof PsiMethod) {
                                    toOpen = element.getContainingFile().getVirtualFile();
                                }

                                if (toOpen != null)
                                    FileEditorManager.getInstance(project).openFile(toOpen, false, true);
                            });
                        });
                    }
                });
            });

            LOG.warn("Done!");
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
