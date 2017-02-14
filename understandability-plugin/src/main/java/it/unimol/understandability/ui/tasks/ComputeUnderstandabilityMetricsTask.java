package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.readability.metric.API.UnifiedMetricClassifier;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.MetricCalculatorSet;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.core.metrics.parts.*;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import it.unimol.understandability.utils.SignatureMatcher;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by simone on 01/02/17.
 */
public class ComputeUnderstandabilityMetricsTask extends MyTask {
    private int minLoc;
    private int maxLoc;
    private List<String> enabledSignatures;

    public ComputeUnderstandabilityMetricsTask(Project project, int minLoc, int maxLoc) {
        super(project, "Computing all the metrics...", project.getBaseDir());

        this.minLoc = minLoc;
        this.maxLoc = maxLoc;
    }

    public ComputeUnderstandabilityMetricsTask(Project project, List<String> enabledSignatures) {
        super(project, "Computing all the metrics...", project.getBaseDir());

        this.enabledSignatures = enabledSignatures;

        this.minLoc = -1;
        this.maxLoc = -1;
    }

    public ComputeUnderstandabilityMetricsTask(Project project, String[] enabledSignatures) {
        this(project, Arrays.asList(enabledSignatures));
    }

    @Override
    public List<MyTask> getDependencyTasks() {
        List<MyTask> dependencies = new ArrayList<>();

        List<String> metricCalculatorSet = MetricCalculatorSet.getMetricNames();

        if (metricCalculatorSet.contains(APIInternalPopularityCalculator.NAME))
            dependencies.add(new BuildCallGraphTask(this.project));

        if (metricCalculatorSet.contains(ExternalDocumentationQualityCalculator.NAME))
            dependencies.add(new DownloadExternalDocumentationScoreTask(this.project));

        return dependencies;
    }

    @Override
    public void runTask(@NotNull ProgressIndicator progressIndicator) {
        AccessToken readAccessToken = null;
        try {
            readAccessToken = ApplicationManager.getApplication().acquireReadActionLock();

            List<PsiClass> allClasses = ProjectUtils.getInstance().getClassesFromVirtualFile(this.project, this.root);

            MetricCalculatorSet calculators = MetricCalculatorSet.getInstance(MultiElementMetricCalculator.Mode.MEAN);
            calculators.initialize(this.project);
            calculators.setIntOption(MetricCalculatorSet.Option.MAX_DEPTH, 3);

            FileWriter writer = new FileWriter(ProjectUtils.getInstance().getProjectFile(project, "METRICS.csv"));
            PrintWriter printWriter = new PrintWriter(writer);

            List<String> csvFields = new ArrayList<>();
            csvFields.add("method");
            csvFields.add("loc");
            csvFields.addAll(MetricCalculatorSet.getMetricNames());
            csvFields.add("readability");

            printWriter.println(StringUtils.join(csvFields, ";"));
            double done = 0;

            UnifiedMetricClassifier readabilityClassifier = UnifiedMetricClassifier.loadClassifier(new File(UnderstandabilityPreferences.getReadabilityClassifierFile()));
            List<PsiMethod> toEvaluate = new ArrayList<>();
            for (PsiClass psiClass : allClasses) {
                for (PsiMethod method : psiClass.getMethods()) {
                    toEvaluate.add(method);
                }
            }
            Collections.shuffle(toEvaluate);
            for (PsiMethod method : toEvaluate) {
                done++;
                progressIndicator.setFraction(done / toEvaluate.size());

                if (method.getBody() == null || !this.isEnabled(method))
                    continue;

                System.out.println(PsiUtils.getSignature(method));
                Map<String, Double> metrics = calculators.calculateMetrics(method);
                System.out.println(PsiUtils.getSignature(method) + " => " + metrics);

                double readability;
                try {
                    readability = readabilityClassifier.classify(method.getText());
                } catch (Exception e) {
                    readability = Double.NaN;
                } catch (Throwable e) {
                    continue;
                }

                List<String> csvRow = new ArrayList<>();
                csvRow.add(PsiUtils.getSignature(method));
                csvRow.add(String.valueOf(method.getText().split("\n").length));
                for (String metricName : MetricCalculatorSet.getMetricNames()) {
                    csvRow.add(String.valueOf(metrics.get(metricName)));
                }
                csvRow.add(String.valueOf(readability));

                printWriter.println(StringUtils.join(csvRow, ";"));
            }

            printWriter.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (readAccessToken != null)
                readAccessToken.finish();
        }
    }

    private boolean isEnabled(PsiMethod psiMethod) {
        String methodSignature = PsiUtils.getSignature(psiMethod);

        if (this.enabledSignatures != null) {
            for (String enabledSignature : this.enabledSignatures) {
                if (SignatureMatcher.weakMatches(enabledSignature, methodSignature))
                    return true;
            }
        }

        int loc = psiMethod.getBody().getText().split("\n").length;
        if (loc < minLoc || loc > maxLoc)
            return false;

        return true;
    }
}
