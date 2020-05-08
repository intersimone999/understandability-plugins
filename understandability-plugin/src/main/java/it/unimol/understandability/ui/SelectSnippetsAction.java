package it.unimol.understandability.ui;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import it.unimol.understandability.ui.tasks.*;

import java.io.File;
import java.io.IOException;

/**
 * Created by simone on 01/02/17.
 */
public class SelectSnippetsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        TaskChain chain = new TaskChain();

        File candidateFile = new File(anActionEvent.getProject().getBaseDir().getCanonicalPath(), "candidates");
        File sqlFile = new File(anActionEvent.getProject().getBaseDir().getCanonicalPath(), "updatedb.sql");

//        chain.appendTask(new BuildCallGraphTask(anActionEvent.getProject()));
//        chain.appendTask(new DownloadExternalDocumentationScoreTask(anActionEvent.getProject()));

        int startingSnippetId = UnderstandabilityPreferences.getSnippetId();
        int startingClassId = UnderstandabilityPreferences.getClassId();
        try {
            chain.appendTask(new SelectSnippetsTask(anActionEvent.getProject(), 3, candidateFile, sqlFile, startingSnippetId, startingClassId));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        chain.executeChain();
    }
}
