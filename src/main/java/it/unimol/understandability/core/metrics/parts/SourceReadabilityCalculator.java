package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.utils.ReadabilityProxy;

/**
 * Created by simone on 08/12/16.
 */
public class SourceReadabilityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "SourceReadability";
    private static final Logger LOG = Logger.getInstance(SourceReadabilityCalculator.class);

    public SourceReadabilityCalculator(Mode mode) {
        super(NAME, mode);
    }

    @Override
    public double computeForEach(PsiMethod pMethod) throws IgnoreValueException {
        if (!PsiUtils.isSourceElement(pMethod))
            throw new IgnoreValueException();

        try {
            return ReadabilityProxy.getReadability(pMethod);
        } catch (Throwable e) {
            throw new IgnoreValueException();
        }
    }

    @Override
    public double computeForEach(PsiClass pClass) throws IgnoreValueException {
        throw new IgnoreValueException();
    }

}
