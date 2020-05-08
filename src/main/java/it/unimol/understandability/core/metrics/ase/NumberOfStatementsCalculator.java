package it.unimol.understandability.core.metrics.ase;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import it.unimol.understandability.core.metrics.MetricCalculator;

/**
 * Created by simone on 08/12/16.
 */
public class NumberOfStatementsCalculator extends MetricCalculator {
    public static final String NAME = "KASTO-NumberOfStatements";
    private static final Logger LOG = Logger.getInstance(NumberOfStatementsCalculator.class);

    public NumberOfStatementsCalculator() {
        super(NAME);
    }


    @Override
    public double compute(PsiMethod method) {
        if (method.getBody() != null) {
            StatementCounter statementCounter = new StatementCounter();
            method.getBody().accept(statementCounter);
            return statementCounter.numberOfStatements;
        } else {
            return 0;
        }
    }

    class StatementCounter extends PsiRecursiveElementVisitor {

        private int numberOfStatements = 0;

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiStatement) {
                if (!(element instanceof PsiBlockStatement)) {
                    ++numberOfStatements;
                }
            }

            super.visitElement(element);
        }
    }
}
