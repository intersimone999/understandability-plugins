package it.unimol.understandability.core.preferences;

import com.intellij.ide.util.PropertiesComponent;

/**
 * Created by simone on 01/02/17.
 */
public class UnderstandabilityPreferences {
    private static final String POPULARITY_PROPERTY = "popularity";
    private static final String EXPANSION_PROPERTY = "expansion";
    private static final String STOPWORD_PROPERTY = "stopword";
    private static final String DICTIONARY_PROPERTY = "dictionary";
    private static final String STACKOVERFLOW_APIKEY_PROPERTY = "stackoverflow.apikey";
    private static final String STACKOVERFLOW_CACHE_PROPERTY = "stackoverflow.cache";
    private static final String READABILITY_PROPERTY = "readability.classifier";

    private static PropertiesComponent properties = PropertiesComponent.getInstance();

    public static void savePreference(String key, String value) {
        properties.setValue("understandability."+key, value);
    }

    public static String loadPreference(String key) {
        return properties.getValue("understandability."+key);
    }

    public static boolean isChanged(String key, String value) {
        String prefs = loadPreference(key);

        if (prefs == null)
            return value != null;
        else
            return !prefs.equals(value);
    }

    //Stack Overflow API Key
    public static String getStackOverflowApiKey() {
        return loadPreference(STACKOVERFLOW_APIKEY_PROPERTY);
    }

    public static void setStackOverflowApiKey(String value) {
        savePreference(STACKOVERFLOW_APIKEY_PROPERTY, value);
    }

    public static boolean isChangedStackOverflowApiKey(String value) {
        return isChanged(STACKOVERFLOW_APIKEY_PROPERTY, value);
    }

    //Stack Overflow cache CSV file
    public static String getStackOverflowCacheFile() {
        return loadPreference(STACKOVERFLOW_CACHE_PROPERTY);
    }

    public static void setStackOverflowCacheFile(String value) {
        savePreference(STACKOVERFLOW_CACHE_PROPERTY, value);
    }

    public static boolean isChangedStackOverflowCacheFile(String value) {
        return isChanged(STACKOVERFLOW_CACHE_PROPERTY, value);
    }

    //Popularity CSV file
    public static String getPopularityFile() {
        return loadPreference(POPULARITY_PROPERTY);
    }

    public static void setPopularityFile(String value) {
        savePreference(POPULARITY_PROPERTY, value);
    }

    public static boolean isChangedPopularityFile(String value) {
        return isChanged(POPULARITY_PROPERTY, value);
    }

    //Expansion names CSV file
    public static String getExpansionFile() {
        return loadPreference(EXPANSION_PROPERTY);
    }

    public static void setExpansionFile(String value) {
        savePreference(EXPANSION_PROPERTY, value);
    }

    public static boolean isChangedExpansionFile(String value) {
        return isChanged(EXPANSION_PROPERTY, value);
    }

    //Dictionary file
    public static String getDictionaryFile() {
        return loadPreference(DICTIONARY_PROPERTY);
    }

    public static void setDictionaryFile(String value) {
        savePreference(DICTIONARY_PROPERTY, value);
    }

    public static boolean isChangedDictionaryFile(String value) {
        return isChanged(DICTIONARY_PROPERTY, value);
    }

    //Stop word file
    public static String getStopWordFile() {
        return loadPreference(STOPWORD_PROPERTY);
    }

    public static void setStopWordFile(String value) {
        savePreference(STOPWORD_PROPERTY, value);
    }

    public static boolean isChangedStopWordFile(String value) {
        return isChanged(STOPWORD_PROPERTY, value);
    }

    //Readability classifier file
    public static String getReadabilityClassifierFile() {
        return loadPreference(READABILITY_PROPERTY);
    }

    public static void setReadabilityClassifierFile(String value) {
        savePreference(READABILITY_PROPERTY, value);
    }

    public static boolean isChangedReadabilityClassifierFile(String value) {
        return isChanged(READABILITY_PROPERTY, value);
    }
}
