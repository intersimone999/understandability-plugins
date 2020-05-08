package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.readability.metric.API.WekaException;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MetricCalculator;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.utils.ReadabilityProxy;

/**
 * Created by simone on 08/12/16.
 */
public class ReadabilityCalculator extends MetricCalculator {
    public static final String NAME = "Readability";
    private static Logger LOG = Logger.getInstance(ReadabilityCalculator.class);

    public ReadabilityCalculator() {
        super(NAME);
    }


    @Override
    public double compute(PsiMethod method) {
        try {
            return ReadabilityProxy.getReadability(method);
        } catch (WekaException e) {
            return Double.NaN;
        }
    }
}
