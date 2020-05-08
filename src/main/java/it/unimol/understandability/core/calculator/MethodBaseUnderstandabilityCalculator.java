package it.unimol.understandability.core.calculator;

import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.MetricCalculatorSet;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator.Mode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by simone on 01/02/17.
 */
public class MethodBaseUnderstandabilityCalculator implements UnderstandabilityCalculator {
    private static final Map<String, Double> cachedValues;

    static {
        cachedValues = new HashMap<>();
    }

    private final int maxDepthLevel;
    private final Mode mode;

    public MethodBaseUnderstandabilityCalculator(int maxDepthLevel, Mode mode) {
        this.maxDepthLevel = maxDepthLevel;
        this.mode = mode;
    }

    public MethodBaseUnderstandabilityCalculator(int maxDepthLevel) {
        this(maxDepthLevel, Mode.MEAN);
    }

    @Override
    public double computeUnderstandability(PsiMethod method) {
        if (isCached(method, this.maxDepthLevel))
            return getCached(method, this.maxDepthLevel);

        MetricCalculatorSet metrics = MetricCalculatorSet.getInstance(this.mode);
        metrics.initialize(method.getProject());
        WeightedComponentUnderstandabilityCalculator componentUnderstandabilityCalculator =
                new WeightedComponentUnderstandabilityCalculator(metrics);

        componentUnderstandabilityCalculator.setMaxDepth(this.maxDepthLevel);

        double componentsUnderstandability = componentUnderstandabilityCalculator.computeUnderstandability(method);

        cache(method, this.maxDepthLevel, componentsUnderstandability);
        return componentsUnderstandability;
    }

    private static void cache(PsiMethod method, int maxDepth, double value) {
        cachedValues.put(PsiUtils.getSignature(method) + "@" + maxDepth, value);
    }

    private static boolean isCached(PsiMethod method, int maxDepth) {
        return cachedValues.containsKey(PsiUtils.getSignature(method) + "@" + maxDepth);
    }

    private static double getCached(PsiMethod method, int maxDepth) {
        return cachedValues.get(PsiUtils.getSignature(method) + "@" + maxDepth);
    }
}
