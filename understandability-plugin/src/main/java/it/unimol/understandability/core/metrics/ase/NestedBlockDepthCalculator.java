package it.unimol.understandability.core.metrics.ase;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import it.unimol.understandability.core.metrics.MetricCalculator;

import java.util.List;

/**
 * Created by simone on 08/12/16.
 */
public class NestedBlockDepthCalculator extends MetricCalculator {
    public static final String NAME = "KASTO-NestedBlocks";
    private static Logger LOG = Logger.getInstance(NestedBlockDepthCalculator.class);

    public NestedBlockDepthCalculator() {
        super(NAME);
    }


    @Override
    public double compute(PsiMethod method) {
        if (method.getBody() == null)
            return 0;

        LevelCounter counter = new LevelCounter();

        method.getBody().accept(counter);

        if (counter.numDepth == 0)
            return 0;
        else
            return counter.sumDepth / counter.numDepth;
    }


    class LevelCounter extends PsiRecursiveElementVisitor {
        private int depth;
        private double sumDepth = 0;
        private int numDepth = 0;

        private boolean ascending;

        @Override
        public void visitElement(PsiElement element) {
            if (element instanceof PsiCodeBlock || element instanceof PsiAnonymousClass) {
                ++depth;
                ascending = true;

                super.visitElement(element);

                if (ascending) {
                    ascending = false;
                    ++numDepth;
                    sumDepth += depth;
                }
                --depth;
            } else {
                super.visitElement(element);
            }
        }
    }
}
