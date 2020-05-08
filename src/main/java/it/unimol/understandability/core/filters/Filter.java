package it.unimol.understandability.core.filters;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by simone on 15/12/16.
 */
public abstract class Filter {
    public abstract boolean pass(PsiElement element);

    public void filter(Collection<PsiElement> elements) {
        List<PsiElement> toRemove = new ArrayList<>();
        for (PsiElement element : elements) {
            if (!this.pass(element))
                toRemove.add(element);
        }

        elements.removeAll(toRemove);
    }
}
