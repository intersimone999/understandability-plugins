package it.unimol.understandability.utils;

import com.intellij.psi.PsiMethod;
import it.unimol.readability.metric.API.UnifiedMetricClassifier;
import it.unimol.readability.metric.API.WekaException;
import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;

import java.io.File;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by simone on 22/02/17.
 */
public class ReadabilityProxy {
    private static UnifiedMetricClassifier readabilityClassifier;
    private static Map<String, Double> proxied;
    private static MessageDigest md5;

    static {
        try {
            readabilityClassifier = UnifiedMetricClassifier.loadClassifier(new File(UnderstandabilityPreferences.getReadabilityClassifierFile()));
        } catch (RuntimeException e) {
            throw e;
        }
        proxied = new HashMap<>();
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new Error("No MD5 algorithm");
        }
    }

    public static double getReadability(PsiMethod pMethod) throws WekaException {
        return getReadability(pMethod.getText());
    }

    public static double getReadability(String body) throws WekaException {
        String hash = String.valueOf(md5.digest(body.getBytes()));

        if (proxied.containsKey(hash))
            return proxied.get(hash);

        Double value = readabilityClassifier.classify(body);
        proxied.put(hash, value);

        return value;
    }
}
