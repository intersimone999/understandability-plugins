package it.unimol.understandability.ui.tasks;

import com.intellij.openapi.application.AccessToken;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.ProjectUtils;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.MetricCalculator;
import it.unimol.understandability.core.metrics.MetricCalculatorSet;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.core.metrics.parts.APIInternalPopularityCalculator;
import it.unimol.understandability.core.metrics.parts.ExternalDocumentationQualityCalculator;
import it.unimol.understandability.utils.CKMetrics;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by simone on 01/02/17.
 */
public class ComputeUnderstandabilityMetricsTask extends MyTask {
    private final int minLoc;
    private final int maxLoc;
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

            MetricCalculatorSet calculatorsMean = MetricCalculatorSet.getInstance(MultiElementMetricCalculator.Mode.MEAN);
            calculatorsMean.initialize(this.project);
            calculatorsMean.setIntOption(MetricCalculatorSet.Option.MAX_DEPTH, 3);

            MetricCalculatorSet calculatorsMax = MetricCalculatorSet.getInstance(MultiElementMetricCalculator.Mode.MAX);
            calculatorsMax.initialize(this.project);
            calculatorsMax.setIntOption(MetricCalculatorSet.Option.MAX_DEPTH, 3);

            MetricCalculatorSet calculatorsMin = MetricCalculatorSet.getInstance(MultiElementMetricCalculator.Mode.MIN);
            calculatorsMin.initialize(this.project);
            calculatorsMin.setIntOption(MetricCalculatorSet.Option.MAX_DEPTH, 3);

            MetricCalculatorSet calculatorsAbs = MetricCalculatorSet.getInstance(MultiElementMetricCalculator.Mode.ABSOLUTE);
            calculatorsAbs.initialize(this.project);
            calculatorsAbs.setIntOption(MetricCalculatorSet.Option.MAX_DEPTH, 3);

            FileWriter writer = new FileWriter(ProjectUtils.getInstance().getProjectFile(project, "METRICS_ALL_ASE.csv"));
            PrintWriter printWriter = new PrintWriter(writer);

            List<String> csvFields = new ArrayList<>();
            csvFields.add("method");
            csvFields.add("loc");
            csvFields.add("complexity");
            for (MetricCalculator metricCalculator : calculatorsMin.getMetricCalculators()) {
                csvFields.add(metricCalculator.getName());
            }

            for (MetricCalculator metricCalculator : calculatorsMean.getMetricCalculators()) {
                csvFields.add(metricCalculator.getName());
            }

            for (MetricCalculator metricCalculator : calculatorsMax.getMetricCalculators()) {
                csvFields.add(metricCalculator.getName());
            }

            for (MetricCalculator metricCalculator : calculatorsAbs.getMetricCalculators()) {
                csvFields.add(metricCalculator.getName());
            }

            printWriter.println(StringUtils.join(csvFields, ";"));
            double done = 0;

            List<PsiMethod> toEvaluate = new ArrayList<>();
            for (PsiClass psiClass : allClasses) {
                done++;
                progressIndicator.setFraction(done / allClasses.size());
                if (isEnabledClass(psiClass)) {
                    for (PsiMethod method : psiClass.getAllMethods()) {
                        if (this.isEnabled(method))
                            toEvaluate.add(method);
                    }
                }
            }

            Collections.shuffle(toEvaluate);
            for (PsiMethod method : toEvaluate) {

                System.out.println(PsiUtils.getSignature(method));
                Map<String, Double> allMetrics = new HashMap<>();
                Map<String, Double> metricsMin = calculatorsMin.calculateMetrics(method);
                Map<String, Double> metricsMean = calculatorsMean.calculateMetrics(method);
                Map<String, Double> metricsMax = calculatorsMax.calculateMetrics(method);
                Map<String, Double> metricsAbs = calculatorsAbs.calculateMetrics(method);

                allMetrics.putAll(metricsMin);
                allMetrics.putAll(metricsMean);
                allMetrics.putAll(metricsMax);
                allMetrics.putAll(metricsAbs);

                System.out.println(PsiUtils.getSignature(method) + " => " + metricsMean);

                List<String> csvRow = new ArrayList<>();
                csvRow.add(PsiUtils.getSignature(method));
                csvRow.add(String.valueOf(CKMetrics.getLOC(method)));
                csvRow.add(String.valueOf(CKMetrics.getCyclomaticComplexity(method)));
//                csvRow.add(String.valueOf(readability));

                int toJump = 3;
                for (String metricName : csvFields) {
                    if (toJump > 0) {
                        --toJump;
                        continue;
                    }
                    csvRow.add(String.valueOf(allMetrics.get(metricName)));
                }

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

    private boolean isEnabledClass(PsiClass psiClass) {
        if (psiClass == null || psiClass.getQualifiedName() == null)
            return false;

        String className = psiClass.getQualifiedName();
        if (this.enabledSignatures != null) {
            for (String enabledSignature : this.enabledSignatures) {
                if (enabledSignature.startsWith(className))
                    return true;
            }

            return false;
        } else {
            return true;
        }
    }

    private boolean isEnabled(PsiMethod psiMethod) {
        if (this.enabledSignatures != null) {
            String methodSignature = PsiUtils.getSignature(psiMethod);
            for (String enabledSignature : this.enabledSignatures) {
//                if (SignatureMatcher.weakMatches(enabledSignature, methodSignature))
                if (enabledSignature.equals(methodSignature))
                    return true;
            }
            return false;
        } else {
            if (psiMethod.getBody() == null)
                return false;

            int loc = psiMethod.getBody().getText().split("\n").length;
            return loc >= minLoc && loc <= maxLoc;
        }
    }
}
