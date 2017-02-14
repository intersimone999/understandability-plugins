package it.unimol.understandability.core.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.parts.*;

import java.util.*;

/**
 * Created by simone on 12/12/16.
 */
public class MetricCalculatorSet {
    public enum Option {
        MAX_DEPTH
    }

    public static List<String> getMetricNames() {
        List<String> names = new ArrayList<>();

        names.add(NameQualityCalculator.NAME);
        names.add(InternalDocumentationQualityCalculator.NAME);
        names.add(ExternalDocumentationQualityCalculator.NAME);
//        names.add(APIInternalPopularityCalculator.NAME);
        names.add(APIExternalPopularityCalculator.NAME);
        names.add(SourceUnderstandabilityCalculator.NAME);

        return names;
    }

    public static MetricCalculatorSet getInstance(MultiElementMetricCalculator.Mode mode) {
        List<String> metricNames = MetricCalculatorSet.getMetricNames();
        MetricCalculatorSet metrics = new MetricCalculatorSet();

        if (metricNames.contains(NameQualityCalculator.NAME))
            metrics.addMetric(new NameQualityCalculator(mode));

        if (metricNames.contains(InternalDocumentationQualityCalculator.NAME))
            metrics.addMetric(new InternalDocumentationQualityCalculator(mode));

        if (metricNames.contains(ExternalDocumentationQualityCalculator.NAME))
            metrics.addMetric(new ExternalDocumentationQualityCalculator(mode));

        if (metricNames.contains(APIInternalPopularityCalculator.NAME))
            metrics.addMetric(new APIInternalPopularityCalculator(mode));

        if (metricNames.contains(APIExternalPopularityCalculator.NAME))
            metrics.addMetric(new APIExternalPopularityCalculator(mode));

        if (metricNames.contains(SourceUnderstandabilityCalculator.NAME))
            metrics.addMetric(new SourceUnderstandabilityCalculator(mode));

        return metrics;
    }

    public void initialize(Project project) {
        for (MetricCalculator calculator : this.calculators)
            calculator.initialize(project);
    }

    private Set<MetricCalculator> calculators;

    public MetricCalculatorSet() {
        this.calculators = new HashSet<>();
    }

    public void addMetric(MetricCalculator calculator) {
        this.calculators.add(calculator);
    }

    public Set<MetricCalculator> getMetricCalculators() {
        return this.calculators;
    }

    public Map<String, Double> calculateMetrics(PsiMethod method) {
        Map<String, Double> metrics = new HashMap<>();
        for (MetricCalculator calculator : this.calculators) {
            metrics.put(calculator.getName(), calculator.compute(method));
        }

        return metrics;
    }

    public void setIntOption(Option name, int value) {
        for (MetricCalculator calculator : this.calculators) {
            calculator.setIntOption(name, value);
        }
    }
}
