package it.unimol.understandability.core.calculator;

import com.intellij.psi.PsiMethod;
import it.unimol.readability.metric.API.UnifiedMetricClassifier;
import it.unimol.readability.metric.API.WekaException;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.MetricCalculatorSet;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator.Mode;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simone on 01/02/17.
 */
public class MethodUnderstandabilityCalculator implements UnderstandabilityCalculator {
    private static UnifiedMetricClassifier readabilityClassifier;

    private static Map<String, Double> cachedValues;

    static {
        cachedValues = new HashMap<>();
    }

    private int maxDepthLevel;
    public MethodUnderstandabilityCalculator(int maxDepthLevel) {
         this.maxDepthLevel = maxDepthLevel;
    }

    @Override
    public double computeUnderstandability(PsiMethod method) {
        if (isCached(method, this.maxDepthLevel))
            return getCached(method, this.maxDepthLevel);

        MetricCalculatorSet metrics = MetricCalculatorSet.getInstance(Mode.MEAN);
        metrics.initialize(method.getProject());
        WeightedComponentUnderstandabilityCalculator componentUnderstandabilityCalculator =
                new WeightedComponentUnderstandabilityCalculator(metrics);

        componentUnderstandabilityCalculator.setMaxDepth(this.maxDepthLevel);

        if (readabilityClassifier == null) {
            readabilityClassifier = UnifiedMetricClassifier.loadClassifier(new File(UnderstandabilityPreferences.getReadabilityClassifierFile()));
        }
        try {
            double readability = readabilityClassifier.classify(method.getText());
            double componentsUnderstandability = componentUnderstandabilityCalculator.computeUnderstandability(method);

            double value = readability * componentsUnderstandability;

            cache(method, this.maxDepthLevel, value);
            return value;
        } catch (WekaException e) {
            throw new RuntimeException(e);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
