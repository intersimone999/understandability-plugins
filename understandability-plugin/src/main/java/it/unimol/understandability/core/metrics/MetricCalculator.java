package it.unimol.understandability.core.metrics;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiMethod;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 07/12/16.
 */
public abstract class MetricCalculator {
    private String name;

    public MetricCalculator(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract double compute(PsiMethod method);

}
