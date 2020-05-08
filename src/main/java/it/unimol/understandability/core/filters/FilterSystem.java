package it.unimol.understandability.core.filters;

import com.intellij.psi.PsiElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by simone on 15/12/16.
 */
public class FilterSystem {
    private static FilterSystem instance;

    public static FilterSystem getStandardInstance() {
        if (instance == null) {
            instance = new FilterSystem();
            instance.addFilter(new FilterJavaStandard());
        }

        return instance;
    }

    private final List<Filter> filters;

    public FilterSystem() {
        this.filters = new ArrayList<>();
    }

    public boolean pass(PsiElement element) {
        for (Filter filter : this.filters) {
            if (!filter.pass(element))
                return false;
        }
        return true;
    }

    public void addFilter(Filter filter) {
        this.filters.add(filter);
    }

    public void filter(Collection<PsiElement> elements) {
        for (Filter filter : this.filters) {
            filter.filter(elements);
        }
    }
}
