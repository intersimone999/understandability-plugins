package it.unimol.understandability.core.metrics.ase;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.MetricCalculator;

/**
 * Created by simone on 08/12/16.
 */
public class NumberOfParametersCalculator extends MetricCalculator {
    public static final String NAME = "KASTO-NumberOfParameters";
    private static final Logger LOG = Logger.getInstance(NumberOfParametersCalculator.class);

    public NumberOfParametersCalculator() {
        super(NAME);
    }


    @Override
    public double compute(PsiMethod method) {
        return method.getParameterList().getParametersCount();
    }
}
