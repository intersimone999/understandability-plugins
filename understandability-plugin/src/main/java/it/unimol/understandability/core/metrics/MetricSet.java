package it.unimol.understandability.core.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import java.util.*;

/**
 * Created by simone on 12/12/16.
 */
public class MetricSet {
    public static MetricSet getInstance() {
        MetricSet metrics = new MetricSet();

//        metrics.addMetric(new DocumentationQualityCalculator());
        metrics.addMetric(new APIInternalPopularityCalculator2());

        return metrics;
    }

    private Set<MetricCalculator> calculators;

    public MetricSet() {
        this.calculators = new HashSet<>();
    }

    public void addMetric(MetricCalculator calculator) {
        this.calculators.add(calculator);
    }

    public Map<String, Double> calculateMetrics(PsiMethod method) {
        Map<String, Double> metrics = new HashMap<>();
        for (MetricCalculator calculator : this.calculators) {
            metrics.put(calculator.getName(), calculator.compute(method));
        }

        return metrics;
    }
}
