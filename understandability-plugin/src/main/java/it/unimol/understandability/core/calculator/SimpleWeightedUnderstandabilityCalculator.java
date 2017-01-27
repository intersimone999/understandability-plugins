package it.unimol.understandability.core.calculator;

import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.MetricCalculator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by simone on 07/12/16.
 */
public class SimpleWeightedUnderstandabilityCalculator implements UnderstandabilityCalculator {
    private List<MetricCalculator> calculators;
    private Map<String, Double> weights;

    public SimpleWeightedUnderstandabilityCalculator(List<MetricCalculator> calculators) {
        this.calculators    = calculators;
        this.weights        = new HashMap<>();

        for (MetricCalculator calculator : this.calculators) {
            this.weights.put(calculator.getName(), 1D);
        }
    }

    public void setWeight(String metric, double weight) {
        this.weights.put(metric, weight);
    }

    public double computeUnderstandability(PsiMethod method) {
        double totalWeight  = 0;
        double sum          = 0;

        for (MetricCalculator calculator : this.calculators) {
            double weight = this.weights.get(calculator.getName());
            sum         += weight * calculator.compute(method);
            totalWeight += weight;
        }

        return (sum / totalWeight);
    }
}
