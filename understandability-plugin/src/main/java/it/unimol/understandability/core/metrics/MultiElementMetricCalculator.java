package it.unimol.understandability.core.metrics;

import com.intellij.psi.*;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.parts.SourceUnderstandabilityCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 07/12/16.
 */
public abstract class MultiElementMetricCalculator extends MetricCalculator {
    public enum Mode {
        MEAN,
        MAX,
        MIN
    }

    private String name;
    protected Mode mode;

    public MultiElementMetricCalculator(String name, Mode mode) {
        super(name);
        this.name = name;
        this.mode = mode;
    }

    public String getName() {
        return name;
    }

    public double compute(PsiMethod method) {
        final ResultHolder holder = new ResultHolder(this.mode);

        method.accept(new PsiRecursiveElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                for (PsiReference reference : element.getReferences()) {
                    PsiElement referredElement = reference.resolve();

                    if (referredElement instanceof PsiClass) {
                        if (((PsiClass) referredElement).isAnnotationType())
                            continue;

                        try {
                            holder.addPart(computeForEach((PsiClass) referredElement));
                        } catch (IgnoreValueException e) {
                        }
                    }

                    if (referredElement instanceof PsiMethod) {
                        try {
                            holder.addPart(computeForEach((PsiMethod) referredElement));
                        } catch (IgnoreValueException e) {
                        }
                    }
                }

                super.visitElement(element);
            }
        });
        return holder.getResult();
    }

    public abstract double computeForEach(PsiMethod pMethod) throws IgnoreValueException;
    public abstract double computeForEach(PsiClass pClass) throws IgnoreValueException;

    class ResultHolder implements Cloneable {
        private List<Double> partsList;
        private Mode mode;

        public ResultHolder(Mode mode) {
            this.partsList = new ArrayList<>();
            this.mode = mode;
        }

        public void addPart(double part) {
            this.partsList.add(part);
        }

        public double getResult() {
            if (this.partsList.size() == 0)
                return Double.NaN;

            switch (mode) {
            case MAX:
                double max = -Double.MAX_VALUE;

                for (Double part : this.partsList) {
                    if (part > max)
                        max = part;
                }

                return max;
            case MEAN:
                double mean = 0.0;

                for (Double part : this.partsList) {
                    mean += part;
                }

                return mean / this.partsList.size();
            case MIN:
                double min = Double.MAX_VALUE;

                for (Double part : this.partsList) {
                    if (part < min)
                        min = part;
                }

                return min;
            }

            throw new RuntimeException("Impossible!");
        }
    }
}
