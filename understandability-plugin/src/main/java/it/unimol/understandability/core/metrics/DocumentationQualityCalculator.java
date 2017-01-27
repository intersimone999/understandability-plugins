package it.unimol.understandability.core.metrics;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiReference;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by simone on 08/12/16.
 */
public class DocumentationQualityCalculator extends MetricCalculator {
    public static final String NAME = "DocumentationQuality";

    public DocumentationQualityCalculator() {
        super(NAME);
    }

    @Override
    public double compute(PsiMethod method) {
        double dir = computeDIR(method);
        double readability = computeReadability(method.getDocComment());
        return (readability + computeDIR(method))/2D;
    }

    private double computeReadability(PsiDocComment comment) {
        return 0.5;
    }

    private double computeDIR(PsiMethod method) {
        if (method.getDocComment() == null)
            return 0.0;

        Set<String> documentableItems = new HashSet<>();
        if (method.getReturnType() == null || !method.getReturnType().getCanonicalText().equals("void"))
            documentableItems.add(".RETURN");

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            documentableItems.add(parameter.getName());
        }

        for (PsiClassType thrown : method.getThrowsList().getReferencedTypes()) {
            documentableItems.add(thrown.getClassName());
        }


        Set<String> documentedItems = new HashSet<>();
        PsiDocTag[] tags = method.getDocComment().getTags();
        for (PsiDocTag tag : tags) {
            if (tag.getName().equals("returns"))
                documentedItems.add(".RETURN");

            String value;
            if (tag.getValueElement() == null) {
                value = "??";
            } else {
                value = tag.getValueElement().getText();
            }
            documentedItems.add(value);
        }

        Set<String> intersection = new HashSet<>(documentableItems);
        intersection.retainAll(documentedItems);

        return ((double)intersection.size())/((double)documentableItems.size());
    }
}
