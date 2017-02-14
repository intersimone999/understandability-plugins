package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.calculator.MethodUnderstandabilityCalculator;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MetricCalculatorSet;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;

/**
 * Created by simone on 08/12/16.
 */
public class SourceUnderstandabilityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "SourceUnderstandability";
    private static Logger LOG = Logger.getInstance(SourceUnderstandabilityCalculator.class);

    public SourceUnderstandabilityCalculator(Mode mode) {
        super(NAME, mode);
    }

    @Override
    public double computeForEach(PsiMethod pMethod) throws IgnoreValueException {
        if (!PsiUtils.isSourceElement(pMethod))
            throw new IgnoreValueException();

        int maxDepth = this.getIntOption(MetricCalculatorSet.Option.MAX_DEPTH);
        if (maxDepth == 0)
            throw new IgnoreValueException();

        MethodUnderstandabilityCalculator calculator = new MethodUnderstandabilityCalculator(maxDepth - 1);
        Double sourceCodeUnderstandability = calculator.computeUnderstandability(pMethod);
        if (sourceCodeUnderstandability.isNaN())
            throw new IgnoreValueException();

        return sourceCodeUnderstandability;
    }

    @Override
    public double computeForEach(PsiClass pClass) throws IgnoreValueException {
        throw new IgnoreValueException();
    }

}
