package it.unimol.understandability.core.metrics;

import com.intellij.psi.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 07/12/16.
 */
public abstract class MultiElementMetricCalculator extends MetricCalculator {
    public enum Mode {
        MEAN,
        MAX,
        MIN,
        ABSOLUTE
    }

    private final String name;
    protected Mode mode;

    public MultiElementMetricCalculator(String name, Mode mode) {
        super(name);
        this.name = name;
        this.mode = mode;

        if (mode == Mode.ABSOLUTE)
            throw new RuntimeException("Not allowed to use a MultiElementMetricCalculator in ABSOLUTE mode");
    }

    public String getName() {
        switch (mode) {
            case MEAN:
                return "Mean" + name;
            case MIN:
                return "Min" + name;
            case MAX:
                return "Max" + name;
        }
        throw new RuntimeException("IMPOSSIBLE!");
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
        private final List<Double> partsList;
        private final Mode mode;

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
