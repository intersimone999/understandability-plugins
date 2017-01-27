package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Created by simone on 07/12/16.
 */
public class ComputeCognitiveScoreTask extends MyTask {
    public ComputeCognitiveScoreTask(Project project) {
        this(project, project.getBaseDir());
    }

    public ComputeCognitiveScoreTask(Project project, VirtualFile root) {
        super(project, "Building cognitive graph...", root);
    }


    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            progressIndicator.setFraction(0.0);
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();
            PsiFileSystemItem psiItem = PsiUtils.virtualFileToPsi(project, root);
            ReferenceGraphBuilder.getInstance().getReferenceGraph(psiItem).getPageRank();
            progressIndicator.setFraction(1.0);
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
