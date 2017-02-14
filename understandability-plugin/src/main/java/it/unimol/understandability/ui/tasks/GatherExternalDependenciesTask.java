package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import it.unimol.understandability.core.structure.ExternalDocumentationScoreContainer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Created by simone on 01/02/17.
 */
public class GatherExternalDependenciesTask extends MyTask {
    public GatherExternalDependenciesTask(Project project) {
        super(project, "Gathering project information...", project.getBaseDir());
    }
    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

            ExternalDocumentationScoreContainer container = ExternalDocumentationScoreContainer.getInstance(this.project);
            container.gatherComponents(progressIndicator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
