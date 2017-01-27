package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import it.unimol.understandability.core.structures.ReferenceGraph;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by simone on 07/12/16.
 */
public class ExportCognitiveScoresTask extends MyTask {
    public ExportCognitiveScoresTask(Project project) {
        this(project, project.getBaseDir());
    }

    public ExportCognitiveScoresTask(Project project, VirtualFile root) {
        super(project, "Exporting cognitive scores...", root);
    }

    @Override
    public void runTask  (@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();
            PsiFileSystemItem psiItem = PsiUtils.virtualFileToPsi(project, root);
            ReferenceGraph graph = ReferenceGraphBuilder.getInstance().getReferenceGraph(psiItem);
            Map<PsiElement, Double> pageRank = graph.getPageRank();

            List<Map.Entry<PsiElement, Double>> sorted = new ArrayList<>();
            for (Map.Entry<PsiElement, Double> entry : pageRank.entrySet()) {
                sorted.add(entry);
            }

            sorted.sort((t0, t1) -> {
                if (t0 == null)
                    return 1;
                else if (t1 == null)
                    return -1;

                return t0.getValue().compareTo(t1.getValue());
            });

            try {
                FileWriter fileWriter = new FileWriter(new File(root.getCanonicalPath() + "/cognitiveScores.csv"));
                PrintWriter writer = new PrintWriter(fileWriter);
                double done = -1;
                for (Map.Entry<PsiElement, Double> entry : sorted) {
                    done++;
                    progressIndicator.setFraction(done/sorted.size());
                    if (entry.getKey() instanceof PsiClass) {
                        String qualifiedName = ((PsiClass) entry.getKey()).getQualifiedName();
                        if (qualifiedName == null || qualifiedName.startsWith("java."))
                            continue;
                    } else if (entry.getKey() instanceof PsiMethod) {
                        String qualifiedName = ((PsiMethod) entry.getKey()).getContainingClass().getQualifiedName();
                        if (qualifiedName == null || qualifiedName.startsWith("java."))
                            continue;
                    }

                    writer.println(PsiUtils.toString(entry.getKey()) + ";" + entry.getValue());
                }
                writer.close();
                fileWriter.close();
            } catch (Exception exception) {
                LOG.error(exception);
            }

            LOG.warn("Done!");
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }
}
