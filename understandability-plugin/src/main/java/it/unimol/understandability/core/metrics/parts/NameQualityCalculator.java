package it.unimol.understandability.core.metrics.parts;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import it.unimol.understandability.utils.CacheManager;
import it.unimol.understandability.utils.identifiers.IdentifiersHandler;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by simone on 08/12/16.
 */
public class NameQualityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "NameQuality";
    private static Logger LOG = Logger.getInstance(NameQualityCalculator.class);

    private Set<String> stopWords;

    public NameQualityCalculator(Mode mode) {
        super(NAME, mode);
    }

    @Override
    public double computeForEach(PsiMethod pMethod) throws IgnoreValueException {
        if (!PsiUtils.isSourceElement(pMethod))
            throw new IgnoreValueException();

        String methodBody = null;
        if (pMethod.getBody() != null)
            methodBody = pMethod.getBody().getText();

        double sumQuality = 0.0;
        int totQuality = 1;
        sumQuality += computeQuality(pMethod.getName(), methodBody);

        for (PsiParameter parameter : pMethod.getParameterList().getParameters()) {
            if (parameter.getName() != null) {
                sumQuality += computeParamaterQuality(parameter.getName(), methodBody);
                totQuality++;
            }
        }

        return sumQuality / totQuality;
    }

    @Override
    public double computeForEach(PsiClass pClass) throws IgnoreValueException {
        throw new IgnoreValueException();
    }

    private String[] splitIdentifier(String s) {
        return s.replaceAll(
                String.format("%s|%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])",
                        "\\_+"
                ),
                " "
        ).split(" ");
    }

    private double computeQuality(String identifier, String methodBody) {
        List<String> splitted = IdentifiersHandler.getInstance().splitIdentifier(identifier);
        List<String> expanded = IdentifiersHandler.getInstance().splitAndExpandIdentifier(identifier);
        List<String> naiveExpanded = IdentifiersHandler.getInstance().splitAndNaiveExpandIdentifier(identifier);

        double expansionEasiness = computeExpansionEasiness(splitted, expanded);

        double overlapMeasure = 0.0;
        if (methodBody != null) {
            overlapMeasure = this.computeOverlap(methodBody, splitted, naiveExpanded);
        }

        return (expansionEasiness + overlapMeasure) / 2;
    }

    private double computeParamaterQuality(String identifier, String methodBody) {
        List<String> splitted = IdentifiersHandler.getInstance().splitIdentifier(identifier);
        List<String> expanded = IdentifiersHandler.getInstance().splitAndExpandIdentifier(identifier);

        return computeExpansionEasiness(splitted, expanded);
    }

    private double computeExpansionEasiness(List<String> splitted, List<String> expanded) {
        String completeSplitted = StringUtil.join(splitted, " ").toLowerCase();
        String completeExpanded = StringUtil.join(expanded, " ").toLowerCase();

        double levenshtein = StringUtils.getLevenshteinDistance(completeSplitted, completeExpanded);

        if (completeExpanded.length() != 0)
            return 1 - levenshtein/completeExpanded.length();
        else
            return 0;
    }

    private double computeOverlap(String methodBody, List<String> splitted, List<String> naiveExpanded) {
        List<String> methodTokens = IdentifiersHandler.getInstance().getWordsFromCode(methodBody);

        double numberOfMatching = 0.0;
        int numberOfTotal = 0;
        for (int i = 0; i < splitted.size(); i++) {
            String word = splitted.get(i).toLowerCase();
            if (this.isStopWord(word))
                continue;

            // If the word is in the dictionary, trivial case
            if (methodTokens.contains(word)) {
                numberOfMatching++;
            } else {
                //Else, the exact sequence of expanded words should be present in source code. E.g., "cms" expands in "content management system".
                //Source code should contain such a sequence of tokens.
                int matching = 0;
                String[] expandedParts = naiveExpanded.get(i).split(" ");

                int j = 0;
                while (matching < expandedParts.length && j < methodTokens.size()) {
                    String methodToken = methodTokens.get(j);
                    if (methodToken.equals(expandedParts[matching])) {
                        matching++;
                    } else {
                        if (methodToken.equals(expandedParts[0])) {
                            matching = 1;
                        } else {
                            matching = 0;
                        }
                    }

                    j++;
                }

                if (matching == expandedParts.length)
                    numberOfMatching++;
            }

            numberOfTotal++;
        }

        if (numberOfTotal != 0)
            return numberOfMatching / numberOfTotal;
        else
            return 0.0;
    }

    private boolean isStopWord(String word) {
        if (this.stopWords == null) {
            try {
                this.stopWords = CacheManager.getStringInstance().loadSet(UnderstandabilityPreferences.getStopWordFile());
            } catch (IOException e) {
                LOG.error(e);

                throw new RuntimeException(e);
            }
        }

        return this.stopWords.contains(word);
    }
}
