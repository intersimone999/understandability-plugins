package it.unimol.understandability.core.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by simone on 07/12/16.
 */
public abstract class MetricCalculator {
    private Map<MetricCalculatorSet.Option, Integer> intOptions;
    private String name;

    public MetricCalculator(String name) {
        this.intOptions = new HashMap<>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract double compute(PsiMethod method);

    public void initialize(Project project) {
    }

    public void setIntOption(MetricCalculatorSet.Option name, int value) {
        this.intOptions.put(name, value);
    }

    public int getIntOption(MetricCalculatorSet.Option name) {
        return this.intOptions.get(name);
    }
}
