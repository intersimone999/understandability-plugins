package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Created by simone on 07/12/16.
 */
public class BuildCallGraphTask extends MyTask {

    public BuildCallGraphTask(Project project) {
        this(project, project.getBaseDir());
    }

    public BuildCallGraphTask(Project project, VirtualFile root) {
        super(project, "Building cognitive graph...", root);
    }

    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

            PsiFileSystemItem psiItem = PsiUtils.virtualFileToPsi(project, root);
            ReferenceGraphBuilder.getInstance().createReferenceGraph(psiItem, progressIndicator);
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
