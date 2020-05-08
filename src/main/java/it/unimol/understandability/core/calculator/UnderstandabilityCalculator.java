package it.unimol.understandability.core.calculator;

import com.intellij.psi.PsiMethod;

/**
 * Created by simone on 08/12/16.
 */
public interface UnderstandabilityCalculator {
    double computeUnderstandability(PsiMethod method);
}
