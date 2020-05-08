package it.unimol.understandability.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.ui.tasks.*;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 01/02/17.
 */
public class CalculateUnderstandabilityAction extends AnAction {
    private enum Criterion {
        LOC,
        LIST
    }

    private static final Criterion CRITERION = Criterion.LOC;
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        TaskChain chain = new TaskChain();
        MyTask computeUnderstandabilityTask;
        if (CRITERION == Criterion.LOC) {
            computeUnderstandabilityTask = new ComputeUnderstandabilityMetricsTask(anActionEvent.getProject(), 30, 70);
        } else if (CRITERION == Criterion.LIST) {
            try {
                String[] enabledContents = FileUtils.readFileToString(ProjectUtils.getInstance().getProjectFile(anActionEvent.getProject(), "candidates"), "UTF-8").split("\n");
                String[] enabledSignatures = new String[enabledContents.length];
                for (int i = 0; i < enabledContents.length; i++) {
                    enabledSignatures[i] = enabledContents[i].split(";")[0];
                }
                computeUnderstandabilityTask = new ComputeUnderstandabilityMetricsTask(anActionEvent.getProject(), enabledSignatures);
            } catch (IOException e) {
                throw new RuntimeException("Candidate file not found." + e.getMessage());
            }
        } else
            throw new RuntimeException("Invalid criterion");

        chain.appendTask(computeUnderstandabilityTask);

        chain.executeChain();
    }
}
