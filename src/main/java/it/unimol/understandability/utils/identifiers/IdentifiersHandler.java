package it.unimol.understandability.utils.identifiers;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by simone on 05/02/17.
 */
public abstract class IdentifiersHandler {
    private static IdentifiersHandler instance;

    public static IdentifiersHandler getInstance() {
        if (instance == null) {
            instance = new SemiAutomaticIdentifiersHandler();
        }

        return instance;
    }

    public abstract List<String> splitIdentifier(String identifier);

    public abstract List<String> expandWord(String word);

    public abstract List<String> getWordsFromCode(String code);

    public List<String> splitAndExpandIdentifier(String identifier) {
        List<String> splitted = this.splitIdentifier(identifier);
        List<String> result = new ArrayList<>();

        for (String part : splitted) {
            result.addAll(this.expandWord(part.toLowerCase()));
        }

        return result;
    }

    public List<String> splitAndNaiveExpandIdentifier(String identifier) {
        List<String> splitted = this.splitIdentifier(identifier);
        List<String> result = new ArrayList<>();

        for (String part : splitted) {
            result.add(StringUtils.join(this.expandWord(part.toLowerCase()), " "));
        }

        return result;
    }
}
