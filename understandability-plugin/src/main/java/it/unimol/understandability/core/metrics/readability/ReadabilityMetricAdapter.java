package it.unimol.understandability.core.metrics.readability;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiMethod;
import it.unimol.readability.metric.FeatureCalculator;
import it.unimol.readability.metric.wordnet.WordnetImplementation;
import it.unimol.understandability.core.metrics.MetricCalculator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Simone Scalabrino.
 */
public class ReadabilityMetricAdapter extends MetricCalculator {
    private static Logger LOG = Logger.getInstance(ReadabilityMetricAdapter.class);

    private FeatureCalculator originalFeatureCalculator;

    public static List<MetricCalculator> getAllMetricCalculators() {
        List<MetricCalculator> result = new ArrayList<>();

        List<FeatureCalculator> allFeatureCalculators = FeatureCalculator.getFeatureCalculators();
        for (FeatureCalculator featureCalculator : allFeatureCalculators) {
            result.add(new ReadabilityMetricAdapter(featureCalculator));
        }

        new WordnetImplementation(); //Calls the static initializer to avoid the bug.

        System.setProperty("wordnet.database.dir", "/home/simone/Workspace/Ricerca/RSE/dict/");

        return result;
    }

    public ReadabilityMetricAdapter(FeatureCalculator featureCalculator) {
        super("READABILITY-" + featureCalculator.getName());

        this.originalFeatureCalculator = featureCalculator;
    }

    @Override
    public double compute(PsiMethod method) {
        this.originalFeatureCalculator.setSource(method.getText());

        return this.originalFeatureCalculator.calculate();
    }
}
