package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFileSystemItem;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.discovery.ProgramDiscoverer;
import it.unimol.understandability.core.structures.ReferenceGraph;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Created by simone on 07/12/16.
 */
public class RunStudyTask extends MyTask {
    public RunStudyTask(Project project) {
        this(project, project.getBaseDir());
    }

    public RunStudyTask(Project project, VirtualFile root) {
        super(project, "Opening important classes...", root);
    }

    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();
            PsiFileSystemItem psiItem = PsiUtils.virtualFileToPsi(project, root);
            ReferenceGraph graph = ReferenceGraphBuilder.getInstance().getReferenceGraph(psiItem);

            ProgramDiscoverer discoverer = new ProgramDiscoverer(project, graph);
            List<PsiElement> startingPoints = discoverer.getStartingPointsByPopularity(100);
            List<PsiElement> centralPoints = discoverer.getCentralPoints(100);
            try {
                FileWriter studySheet = new FileWriter(new File(root.getCanonicalPath() + "/study_sheet.csv"));
                PrintWriter studySheetWriter = new PrintWriter(studySheet);

                FileWriter oracleSheet = new FileWriter(new File(root.getCanonicalPath() + "/oracle_sheet.csv"));
                PrintWriter oracleSheetWriter = new PrintWriter(oracleSheet);
                studySheetWriter.println("artifact;startingScore;centralityScore");
                oracleSheetWriter.println("artifact;isStarting;isCentral");

                for (int i = 0; i < 20; i++) {
                    PsiElement element = startingPoints.get(i);
                    studySheetWriter.println(PsiUtils.toString(element) + ";;");
                    oracleSheetWriter.println(PsiUtils.toString(element) + ";1;0");
                }

                for (int i = 0; i < 20; i++) {
                    PsiElement element = centralPoints.get(i);
                    studySheetWriter.println(PsiUtils.toString(element) + ";;");
                    oracleSheetWriter.println(PsiUtils.toString(element) + ";0;1");
                }

                for (int i = 99; i >= 95; i--) {
                    PsiElement element = startingPoints.get(i);
                    studySheetWriter.println(PsiUtils.toString(element) + ";;");
                    oracleSheetWriter.println(PsiUtils.toString(element) + ";-1;0");
                }

                for (int i = 99; i >= 95; i--) {
                    PsiElement element = centralPoints.get(i);
                    studySheetWriter.println(PsiUtils.toString(element) + ";;");
                    oracleSheetWriter.println(PsiUtils.toString(element) + ";0;-1");
                }

                studySheetWriter.close();
                studySheet.close();

                oracleSheetWriter.close();
                oracleSheet.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
