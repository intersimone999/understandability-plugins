package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.*;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import it.unimol.understandability.utils.CacheManager;

import java.io.IOException;
import java.util.Map;

/**
 * Created by simone on 30/01/17.
 */
public class APIExternalPopularityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "APIExternalPopularity";
    private static Map<String, Double> cachedPopularities;
    private static double cachedMaxPopularity;

    private Map<String, Double> popularities;
    private double maxPopularity;

    private static Logger LOG = Logger.getInstance(APIExternalPopularityCalculator.class);
    public APIExternalPopularityCalculator(Mode mode) {
        super(NAME, mode);

        if (this.popularities == null) {
            if (cachedPopularities != null) {
                this.popularities = cachedPopularities;
                this.maxPopularity = cachedMaxPopularity;
            } else {
                this.maxPopularity = 0.0;

                try {
                    this.popularities = CacheManager.getNumericInstance().loadMap(UnderstandabilityPreferences.getPopularityFile());

                    for (Map.Entry<String, Double> entry : this.popularities.entrySet()) {
                        if (entry.getValue() > this.maxPopularity)
                            this.maxPopularity = entry.getValue();
                    }
                } catch (IOException e) {
                    LOG.error(e);
                    this.popularities = null;

                    throw new RuntimeException(e);
                }

                cachedPopularities = this.popularities;
                cachedMaxPopularity = this.maxPopularity;
            }
        }
    }

    public double computeForEach(PsiClass targetClass) throws IgnoreValueException {
        if (PsiUtils.isSourceElement(targetClass) || targetClass.getQualifiedName() == null)
            throw new IgnoreValueException();

        if (targetClass.getQualifiedName().matches("java\\.lang\\.[A-Za-z0-9_]+"))
            return 1.0;

        if (this.popularities.containsKey(targetClass.getQualifiedName()))
            return this.popularities.get(targetClass.getQualifiedName()) / this.maxPopularity;
        else
            return 0.0;
    }

    public double computeForEach(PsiMethod method) throws IgnoreValueException {
        throw new IgnoreValueException();
    }
}