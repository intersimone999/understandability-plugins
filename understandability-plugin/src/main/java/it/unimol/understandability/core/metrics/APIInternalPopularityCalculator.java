package it.unimol.understandability.core.metrics;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import it.unimol.understandability.core.structures.ReferenceGraph;

/**
 * Created by simone on 08/12/16.
 */
public class APIInternalPopularityCalculator extends MetricCalculator {
    public static final String NAME = "APIInternalPopularity";

    private static Logger LOG = Logger.getInstance(APIInternalPopularityCalculator.class);
    public APIInternalPopularityCalculator() {
        super(NAME);
    }

    @Override
    public double compute(PsiMethod method) {
        ReferenceGraph referenceGraph = ReferenceGraphBuilder.getInstance().getReferenceGraph(method.getProject());
        return referenceGraph.getPageRank().get(PsiUtils.getSignature(method));
    }

    class Result {
        public double result;
    }
}
