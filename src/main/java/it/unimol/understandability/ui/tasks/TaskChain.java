package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.progress.ProgressManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 16/12/16.
 */
public class TaskChain {
    private final List<MyTask> chain;

    public TaskChain() {
        this.chain = new ArrayList<>();
    }

    public void appendTask(MyTask task) {
        for (MyTask dependency : task.getDependencyTasks())
            this.appendTask(dependency);
        this.chain.add(task);
    }

    public void executeChain() {
        ProgressManager progressManager = ProgressManager.getInstance();
        for (int i = this.chain.size() - 1; i > 0; i--)
            this.chain.get(i - 1).setNext(this.chain.get(i));

        progressManager.run(this.chain.get(0));
    }
}
