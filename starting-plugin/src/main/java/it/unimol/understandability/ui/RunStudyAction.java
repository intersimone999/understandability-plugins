package it.unimol.understandability.ui;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.vfs.VirtualFile;
import it.unimol.understandability.ui.tasks.*;

/**
 * Created by simone on 07/12/16.
 */
public class RunStudyAction extends AnAction {
    private static Logger LOG = Logger.getInstance(RunStudyAction.class);
    @Override
    public void actionPerformed(AnActionEvent e) {
        TaskChain taskChain = new TaskChain();

        VirtualFile file = e.getData(DataKeys.VIRTUAL_FILE);

        taskChain.appendTask(new BuildCallGraphTask(e.getProject(), file));
        taskChain.appendTask(new ComputeCognitiveScoreTask(e.getProject(), file));
        taskChain.appendTask(new RunStudyTask(e.getProject(), file));

        taskChain.executeChain();
    }
}
