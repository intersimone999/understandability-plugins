package it.unimol.understandability.core.filters;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

/**
 * Created by simone on 15/12/16.
 */
public class FilterJavaStandard extends Filter {
    private static final String[] FILTER_OUT_NAMES = {"equals", "clone", "wait", "toString", "hashCode"};

    @Override
    public boolean pass(PsiElement element) {
        if (element == null)
            return false;

        if (element instanceof PsiClass) {
            PsiClass psiClass = (PsiClass)element;

            if (psiClass.getQualifiedName() == null)
                return false;
            if (psiClass.getQualifiedName().startsWith("java."))
                return false;
        }

        if (element instanceof PsiMethod) {
            PsiMethod method = (PsiMethod) element;
            PsiClass psiClass = method.getContainingClass();
            if (psiClass == null || psiClass.getQualifiedName() == null)
                return false;

            if (psiClass.getQualifiedName().startsWith("java."))
                return false;

            for (String toFilter : FILTER_OUT_NAMES) {
                if (toFilter.equals(method.getName()))
                    return false;
            }
        }

        return true;
    }
}
