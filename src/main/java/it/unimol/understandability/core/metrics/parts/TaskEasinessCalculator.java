package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.MetricCalculator;
import it.unimol.understandability.utils.CKMetrics;

/**
 * Created by simone on 08/12/16.
 */
public class TaskEasinessCalculator extends MetricCalculator {
    public static final String NAME = "TaskEasiness";
    private static final Logger LOG = Logger.getInstance(TaskEasinessCalculator.class);

    public TaskEasinessCalculator() {
        super(NAME);
    }


    @Override
    public double compute(PsiMethod method) {
        double value = (10D - CKMetrics.getCyclomaticComplexity(method)) / 10D;
        if (value < 0)
            value = 0;

        return value;
    }
}
