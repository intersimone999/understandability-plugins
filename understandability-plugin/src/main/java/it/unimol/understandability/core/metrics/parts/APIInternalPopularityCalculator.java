package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.core.structures.ReferenceGraphBuilder;
import it.unimol.understandability.core.structures.ReferenceGraph;

/**
 * Created by simone on 08/12/16.
 */
public class APIInternalPopularityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "InternalPopularity";
    private static Logger LOG = Logger.getInstance(APIInternalPopularityCalculator.class);

    private ReferenceGraph referenceGraph;
    public APIInternalPopularityCalculator(Mode mode) {
        super(NAME, mode);
    }

    public void initialize(Project project) {
        this.referenceGraph = ReferenceGraphBuilder.getInstance().getReferenceGraph(project);
    }

    @Override
    public double computeForEach(PsiMethod pMethod) throws IgnoreValueException {
        if (!referenceGraph.getNormalizedPageRank().containsKey(pMethod)) {
            LOG.warn("Method missing in the knowledge graph: " + pMethod.getName() + " from " + pMethod.getContainingFile().getVirtualFile().toString());

            throw new IgnoreValueException();
        }

        return referenceGraph.getNormalizedPageRank().get(pMethod);
    }

    @Override
    public double computeForEach(PsiClass pClass) throws IgnoreValueException {
        //TODO fix
//        if (pClass == null)
//            throw new IgnoreValueException();
//
//        if (referenceGraph == null)
//            throw new RuntimeException();
//
//        if (referenceGraph.getNormalizedPageRank() == null)
//            throw new RuntimeException();

        if (!referenceGraph.getNormalizedPageRank().containsKey(pClass)) {
            LOG.warn("Class missing in the knowledge graph: " + pClass.getName() + " from " + pClass.getContainingFile().getVirtualFile().toString());

            throw new IgnoreValueException();
        }

        try {
            return referenceGraph.getNormalizedPageRank().get(pClass);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }
}
