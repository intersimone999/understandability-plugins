package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import it.unimol.understandability.core.structure.ExternalDocumentationScoreContainer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * Created by simone on 01/02/17.
 */
public class DownloadExternalDocumentationScoreTask extends MyTask {
    public DownloadExternalDocumentationScoreTask(Project project) {
        super(project, "Downloading external documentation info...", project.getBaseDir());
    }

    @Override
    public List<MyTask> getDependencyTasks() {
        List<MyTask> dependencies = super.getDependencyTasks();

        dependencies.add(new GatherExternalDependenciesTask(this.project));

        return dependencies;
    }

    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

            ExternalDocumentationScoreContainer container = ExternalDocumentationScoreContainer.getInstance(this.project);
            container.downloadAll(true, progressIndicator);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
