package it.unimol.understandability.utils;

/**
 * Created by simone on 09/02/17.
 */
public class SignatureMatcher {
    public static boolean weakMatches(String signature1, String signature2) {
        Signature sign1 = new Signature(signature1);
        Signature sign2 = new Signature(signature2);

        return sign1.weakMatches(sign2);
    }
}

class Signature {
    private final String[] path;
    private final String[] parameters;

    public Signature(String raw) {
        String clean = removeGenerics(raw);
        String[] parts = clean.split("[()]");

        String methodId = parts[0];
        String parameters = parts[1];

        this.path = methodId.split("[.#]");
        this.parameters = parameters.split(",");
    }

    public boolean weakMatches(Signature other) {
        if (other.path.length != this.path.length || other.parameters.length != this.parameters.length)
            return false;

        for (int i = 0; i < this.path.length; i++) {
            if (!this.path[i].equals(other.path[i]))
                return false;
        }

        for (int i = 0; i < this.parameters.length; i++) {
            String[] paramPath1 = this.parameters[i].split("\\.");
            String[] paramPath2 = other.parameters[i].split("\\.");

            String last1 = paramPath1[paramPath1.length - 1];
            String last2 = paramPath2[paramPath2.length - 1];

            if (!last1.equals(last2))
                return false;
        }

        return true;
    }

    private String removeGenerics(String withGenerics) {
        String withoutGenerics = withGenerics.replaceAll("<[^>]+>", "");
        withoutGenerics = withoutGenerics.replaceAll(">[^>]+>", "");
        withoutGenerics = withoutGenerics.replaceAll(">", "");

        return withoutGenerics;
    }
}