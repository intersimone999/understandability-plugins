package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.core.structure.ExternalDocumentationScoreContainer;

/**
 *
 * Created by simone on 08/12/16.
 */
public class ExternalDocumentationQualityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "ExternalDocumentationQuality";

    private ExternalDocumentationScoreContainer documentationContainer;

    public ExternalDocumentationQualityCalculator(Mode mode) {
        super(NAME, mode);
    }

    @Override
    public void initialize(Project project) {
        this.documentationContainer = ExternalDocumentationScoreContainer.getInstance(project);
    }

    @Override
    public double computeForEach(PsiClass pClass) throws IgnoreValueException {
        if (PsiUtils.isSourceElement(pClass) || pClass.getQualifiedName() == null) {
            throw new IgnoreValueException();
        }

        Double normScore = this.documentationContainer.getNormalizedScoreFor(pClass);
        if (normScore.isNaN()) {
            throw new IgnoreValueException();
        }

        return normScore;
    }

    @Override
    public double computeForEach(PsiMethod m) throws IgnoreValueException {
        throw new IgnoreValueException();
    }
}
