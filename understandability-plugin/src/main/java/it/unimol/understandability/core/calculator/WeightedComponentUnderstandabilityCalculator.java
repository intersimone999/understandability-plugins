package it.unimol.understandability.core.calculator;

import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.MetricCalculator;
import it.unimol.understandability.core.metrics.MetricCalculatorSet;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by simone on 07/12/16.
 */
public class WeightedComponentUnderstandabilityCalculator implements UnderstandabilityCalculator {
    private MetricCalculatorSet calculators;
    private Map<String, Double> weights;
    private int maxDepth;

    public WeightedComponentUnderstandabilityCalculator(MetricCalculatorSet calculators) {
        this.calculators    = calculators;
        this.weights        = new HashMap<>();

        for (MetricCalculator calculator : this.calculators.getMetricCalculators()) {
            this.weights.put(calculator.getName(), 1D);
        }
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void setWeight(String metric, double weight) {
        this.weights.put(metric, weight);
    }

    public double computeUnderstandability(PsiMethod method) {
        double totalWeight  = 0;
        double sum          = 0;

        this.calculators.setIntOption(MetricCalculatorSet.Option.MAX_DEPTH, maxDepth);

        for (MetricCalculator calculator : this.calculators.getMetricCalculators()) {
            double weight = this.weights.get(calculator.getName());
            Double value = calculator.compute(method);

            if (!value.isNaN()){
                sum         += weight * value;
                totalWeight += weight;
            }
        }

        return (sum / totalWeight);
    }
}
