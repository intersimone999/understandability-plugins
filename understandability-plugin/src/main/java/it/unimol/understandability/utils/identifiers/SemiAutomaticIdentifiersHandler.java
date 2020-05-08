package it.unimol.understandability.utils.identifiers;

import it.unimol.understandability.core.preferences.UnderstandabilityPreferences;
import it.unimol.understandability.utils.CacheManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by simone on 05/02/17.
 */
public class SemiAutomaticIdentifiersHandler extends IdentifiersHandler {
    private Map<String, String> expansions;
    private Set<String> dictionary;

    public List<String> splitIdentifier(String identifier) {
        String[] splitted =  identifier.replaceAll("[0-9]+", " ").replaceAll(
                String.format("%s|%s|%s|%s",
                        "(?<=[A-Z])(?=[A-Z][a-z])",
                        "(?<=[^A-Z])(?=[A-Z])",
                        "(?<=[A-Za-z])(?=[^A-Za-z])",
                        "\\_+"
                ),
                " "
        ).split(" ");

        List<String> result = new ArrayList<>();
        for (String part : splitted) {
            result.add(part);
        }

        return result;
    }

    public List<String> expandWord(String word) {
        if (this.expansions == null) {
            try {
                this.expansions = CacheManager.getStringInstance().loadMap(UnderstandabilityPreferences.getExpansionFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.dictionary == null) {
            try {
                this.dictionary = CacheManager.getStringInstance().loadSet(UnderstandabilityPreferences.getDictionaryFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        String candidate = null;
        if (this.expansions.containsKey(word)) {
            candidate = this.expansions.get(word);
        } else {
            if (this.dictionary.contains(word)) {
                candidate = word;
            } else {
//                candidate = JOptionPane.showInputDialog(null, "What is the expansion for \"" + word + "\" (separate with whitespaces for splitting)?", word);
//                if (candidate == null)
//                    candidate = word;

                candidate = word;
                this.expansions.put(word, candidate);
                CacheManager.getStringInstance().addCacheMapEntry(UnderstandabilityPreferences.getExpansionFile(), word, candidate);
            }
        }

        String[] parts = candidate.split(" ");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            result.add(part);
        }

        return result;
    }

    @Override
    public List<String> getWordsFromCode(String code) {
        List<String> result = new ArrayList<>();

        String[] coreParts = code.split("[^A-Za-z0-9]+");
        for (String part : coreParts) {
            for (String idPart : this.splitIdentifier(part))
                result.add(idPart.toLowerCase());
        }

        return result;
    }


//    Anode alpha[256]; /* offset for each alphabetic character */
//    int count[256]; /* count of the characters that don't match */


    // char *t; /* text */
    // int n, m; /* number of characters in text and pattern */
    // int k; /* the Hamming Distance */
    // anode alpha[]; /* index of alphabet */
    // int count[]; /* circular buffer of count of mismatched characters */
    /* string searching with mismatches */
    void search(String t, int n, int m, int k, PatternRepresenter patternRepresenter) {
        int curChar = -1;
        for (int i = 0; i < n; i++) {
            curChar++;
            Anode aptr = patternRepresenter.alpha[t.charAt(curChar)];
            int off1 = aptr.offset;
            if (off1 >= 0) {
                patternRepresenter.count[(i+off1)&0xff]--;
                for (aptr=aptr.next; aptr != null; aptr = aptr.next)
                    patternRepresenter.count[(i + aptr.offset)&0xff]--;
            }
            if (patternRepresenter.count[i&0xff] <= k)
                System.out.println(String.format("Match in position %d with %d mismatches\n", i - m + 1, patternRepresenter.count[i&0xff]));
            patternRepresenter.count[i&0xff] = m;
        }
    }


    // char *p; /* pointer to pattern */
    // int m; /* number of characters in pattern */
    // anode alpha[]; /* alphabetical index giving offsets */
    // int count[]; /* circular buffer for counts of mismatches */
    /* preprocessing routine */
    void preprocess(String p, int m, PatternRepresenter patternRepresenter) {
        Anode aptr;

        for (int i = 0; i < 256; i++) {
            patternRepresenter.alpha[i].offset = -1;
            patternRepresenter.alpha[i].next = null;
            patternRepresenter.count[i] = m;
        }

        int curCharIndex = 0;
        for (int i = 0, j = 128; i < m; i++, curCharIndex++) {
            char curChar = p.charAt(curCharIndex);
            patternRepresenter.count[i] = 256;
            if (patternRepresenter.alpha[curChar].offset == -1)
                patternRepresenter.alpha[curChar].offset = m-i-1;
            else {
                aptr = patternRepresenter.alpha[curChar].next;
                patternRepresenter.alpha[curChar].next = patternRepresenter.alpha[j++];
                patternRepresenter.alpha[curChar].next.offset = m - i - 1;
                patternRepresenter.alpha[curChar].next.next = aptr;
            }
        }

        patternRepresenter.count[m-1] = m;
    }

    class Anode {
        public Anode next;
        public int offset;
    }

    class PatternRepresenter {
        public Anode[] alpha;
        public int[] count;
    }
}
