package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFileSystemItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 16/12/16.
 */
public abstract class MyTask extends Task.Backgroundable {
    protected static Logger LOG = Logger.getInstance(MyTask.class);
    protected VirtualFile root;
    protected Project project;
    private MyTask next;

    public MyTask(Project project, String title, VirtualFile root) {
        super(project, title);
        this.root = root;
        this.project = project;
    }

    public void setNext(MyTask next) {
        this.next = next;
    }

    public List<MyTask> getDependencyTasks() {
        return new ArrayList<>();
    }

    @Override
    public void run(@NotNull ProgressIndicator progressIndicator) {
        this.runTask(progressIndicator);

        ProgressManager progressManager = ProgressManager.getInstance();
        if (this.next != null) {
            ApplicationManager.getApplication().invokeLater(() -> {
                ApplicationManager.getApplication().runWriteAction(() -> {
                    progressManager.run(this.next);
                });
            });
        }
    }

    public abstract void runTask(@NotNull ProgressIndicator progressIndicator);
}
