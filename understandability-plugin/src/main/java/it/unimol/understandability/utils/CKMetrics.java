package it.unimol.understandability.utils;

import com.intellij.psi.PsiMethod;
import it.unimol.readability.metric.code.CodeAnalyzer;
import it.unimol.readability.metric.code.CodeLanguages;
import it.unimol.readability.metric.filters.FilterManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by simone on 22/02/17.
 */
public class CKMetrics {
    public static int getLOC(PsiMethod method) {
        return method.getText().split("\n").length;
    }

    public static int getCyclomaticComplexity(PsiMethod method) {
        int mcCabe = 0;
        String code = method.getText();

        //Removes inline comments
        CodeAnalyzer analyzer = CodeAnalyzer.getInstance(CodeLanguages.JAVA);
        code = analyzer.deleteComments(code);

        String regex;
        Pattern pattern;
        Matcher matcher;

//        regex = "[^A-Za-z]return[^A-Za-z]";
//        pattern = Pattern.compile(regex);
//        matcher = pattern.matcher(code);
//
//        if (matcher.find()) {
//            mcCabe++;
//        }

        regex = "[^A-Za-z]if[^A-Za-z]";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

//        regex = "[^A-Za-z]else[^A-Za-z]";
//        pattern = Pattern.compile(regex);
//        matcher = pattern.matcher(code);
//
//        if (matcher.find()) {
//            mcCabe++;
//        }

        regex = "[^A-Za-z]case[^A-Za-z]";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

        regex = "[^A-Za-z]for[^A-Za-z]";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

        regex = "[^A-Za-z]while[^A-Za-z]";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

//        regex = "[^A-Za-z]break[^A-Za-z]";
//        pattern = Pattern.compile(regex);
//        matcher = pattern.matcher(code);
//
//        if (matcher.find()) {
//            mcCabe++;
//        }

        regex = "&&";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

        regex = "||";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

        regex = "[^A-Za-z]catch[^A-Za-z]";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

        regex = "[^A-Za-z]finally[^A-Za-z]";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(code);

        if (matcher.find()) {
            mcCabe++;
        }

//        regex = "throw";
//        pattern = Pattern.compile(regex);
//        matcher = pattern.matcher(code);
//
//        if (matcher.find()) {
//            mcCabe++;
//        }

        return mcCabe + 1;
    }
}
