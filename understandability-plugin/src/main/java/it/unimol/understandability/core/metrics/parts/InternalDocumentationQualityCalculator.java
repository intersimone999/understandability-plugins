package it.unimol.understandability.core.metrics.parts;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import it.unimol.understandability.core.PsiUtils;
import it.unimol.understandability.core.metrics.IgnoreValueException;
import it.unimol.understandability.core.metrics.MultiElementMetricCalculator;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by simone on 08/12/16.
 */
public class InternalDocumentationQualityCalculator extends MultiElementMetricCalculator {
    public static final String NAME = "DocumentationQuality";

    public InternalDocumentationQualityCalculator(Mode mode) {
        super(NAME, mode);
    }

    @Override
    public double computeForEach(PsiMethod method) throws IgnoreValueException {
        if (!PsiUtils.isSourceElement(method))
            throw new IgnoreValueException();

        double dir = computeDIR(method);
        double readability = computeReadability(method.getDocComment());
        return (readability + dir)/2D;
    }

    public double computeForEach(PsiClass c) throws IgnoreValueException {
        throw new IgnoreValueException();
    }

    private double computeReadability(PsiDocComment comment) {
        if (comment == null)
            return 0.0;
        String preprocessedText = comment.getText()
                .replaceAll("[^\\sA-Za-z.,;:!?\n@<>{}]+", " ")      //Replaces all special characters with a whitespace
                .replaceAll("\\<[^>]+\\>", " ")                     //Removes HTML tags
                .replaceAll("\\@(param|return)", ".")              //Removes JavaDoc tags (e.g. param, return)
                .replaceAll("\\{\\s*\\@[a-zA-Z]+[^}]*\\}", " ")     //Removes multiline JavaDoc tags (e.g. code, link)
                .replaceAll("\\@[A-Za-z]+", " ")                    //Removes inline JavaDoc tags
                .replaceAll("^[^A-Za-z]+", "")                      //Removes everything before the first letter
                .replaceAll("\n[ \n\r]+", "\n")                     //Replaces all sequences of \n and whitespaces with a single \n
                .replaceAll("\n\\s*([A-Z])", ". $1");               //Replaces all the \n followed by a capital letter with a fullstop.
//              .replaceAll("\\@[^ ]+", ".")                        //Adds a sentence when JavaDoc tags are found
        int[] data = this.getNLTextInfo(preprocessedText);

        int syllables = data[0];
        int words = data[1];
        int sentences = data[2];

        double result;
        if (words != 0 && sentences != 0)
            result = 206.835 - 1.015*(words/sentences) - 84.6*(syllables/words);
        else
            result = 0;

        if (result < 0)
            result = 0;
        if (result > 100)
            result = 100;

        return result / 100;
    }

    private double computeDIR(PsiMethod method) {
        if (method.getDocComment() == null)
            return 0.0;

        Set<String> documentableItems = new HashSet<>();
        if (method.getReturnType() == null || !method.getReturnType().getCanonicalText().equals("void"))
            documentableItems.add(".RETURN");

        for (PsiParameter parameter : method.getParameterList().getParameters()) {
            documentableItems.add(parameter.getName());
        }

        for (PsiClassType thrown : method.getThrowsList().getReferencedTypes()) {
            documentableItems.add(thrown.getClassName());
        }

        if (documentableItems.size() == 0)
            return 1.0;

        Set<String> documentedItems = new HashSet<>();
        PsiDocTag[] tags = method.getDocComment().getTags();
        for (PsiDocTag tag : tags) {
            if (tag.getName().equals("return"))
                documentedItems.add(".RETURN");

            String value;
            if (tag.getValueElement() == null) {
                value = "??";
            } else {
                value = tag.getValueElement().getText();
            }
            documentedItems.add(value);
        }

        Set<String> intersection = new HashSet<>(documentableItems);
        intersection.retainAll(documentedItems);

        return ((double)intersection.size())/((double)documentableItems.size());
    }

    private int[] getNLTextInfo(String text) {
        Pattern syllablesPattern = Pattern.compile("[AaEeIiOoUuYy]+[^$e(,.:;!?)]");
        Matcher syllablesMatcher = syllablesPattern.matcher(text);

        int syllables = 0;
        while (syllablesMatcher.find())
            syllables++;

        Pattern wordsPattern = Pattern.compile("[A-Za-z]+");
        Matcher wordsMatcher = wordsPattern.matcher(text);

        int words = 0;
        while (wordsMatcher.find())
            words++;

        int sentences = text.split("[.:;!?][ \n,.:;!?]*").length;

        return new int[] {syllables, words, sentences};
    }

    /**
     * Returns an array with all information about the given natural language text.
     * @param pText Text to be analyzed
     * @return An array with (in order) number of syllables, number of words and
     * number of sentences of the given text.
     */
    private int[] getOldNLTextInfo(String pText) {
        String text = pText;
        text = text.replaceAll("[\\s\n]+", " ");
        text = text.replaceAll("[^\\\\p{L}\\\\p{Nd}\\\\p{P} \\n]", "");
        int counter = 0;
        int words = 0; //set to one to account for last word
        int sentences = 0;
        int syllables = 0;
        boolean stopWordsIncrement = false;
        boolean stopSyllablesIncrement = false;
        while( counter < text.length()) {
            char currentChar = text.charAt(counter);
            char nextChar;
            if (counter + 1 < text.length())
                nextChar = text.charAt(counter+1);
            else
                nextChar = 0;

            switch (currentChar) {
                case '.':
                case '!':
                case '?':
                case ':':
                case ';':
                    if (!stopWordsIncrement)
                        words++;

                    stopWordsIncrement = true;
                    if (nextChar == ' ' || nextChar == '\t' || nextChar == '\n')
                        sentences++;

                    stopSyllablesIncrement 	= false;
                    break;
                case ' ':
                    if (!stopWordsIncrement)
                        words++;

                    stopSyllablesIncrement 	= false;
                    break;
                case 'a':
                case 'i':
                case 'o':
                case 'u':
                case 'y':
                case 'e':
                    if (currentChar == 'e')
                        if ((nextChar < 'a' || nextChar > 'z') && (nextChar < 'A' || nextChar > 'Z'))
                            break;

                    if (!stopSyllablesIncrement)
                        syllables++;

                    stopSyllablesIncrement 	= true;
                    break;
                default:
                    stopSyllablesIncrement 	= false;
                    stopWordsIncrement 		= false;
                    break;
            }
            counter++;
        }

        if (!stopWordsIncrement)
            words++;

        if (!stopSyllablesIncrement)
            syllables++;
        if (counter != 0) {
            char lastChar = text.charAt(counter-1);
            if (lastChar != ' ' && lastChar != '\t' && lastChar != '\n')
                sentences++;
        }

        int[] result = {syllables, words, sentences};
        return result;
    }
}
