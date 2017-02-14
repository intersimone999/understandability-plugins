package it.unimol.understandability.core;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.MethodSignature;
import org.apache.xmlbeans.InterfaceExtension;

/**
 * Created by simone on 09/12/16.
 */
public class PsiUtils {
    private static Logger LOG = Logger.getInstance(PsiUtils.class);
    public static String getSignature(PsiClass thisClass, PsiMethodCallExpression methodCallExpression) {
        PsiExpression qualifierExpression = methodCallExpression.getMethodExpression().getQualifierExpression();
        String calledMethodName = methodCallExpression.getMethodExpression().getLastChild().getText();
        PsiType[] arguments = methodCallExpression.getArgumentList().getExpressionTypes();

        String result;

        if (qualifierExpression != null) {
            if (qualifierExpression.getType() != null)
                result = qualifierExpression.getType().getCanonicalText();
            else
                result = qualifierExpression.getText();
        } else
            result = thisClass.getQualifiedName();

        result += "." + calledMethodName;
        result += "(";
        int i = 0;
        for (PsiType type : arguments) {
            if (type != null)
                result += type.getCanonicalText();
            else
                result += "?";
            if (i != arguments.length-1)
                result += ",";
            i++;
        }
        result += ")";

        return result;
    }

    public static String getSignature(PsiMethod pMethod) {
        PsiClass theClass = pMethod.getContainingClass();
        String result = theClass.getQualifiedName();
        result += "." + pMethod.getName();
        result += "(";

        PsiParameter[] parameters = pMethod.getParameterList().getParameters();
        for (int i = 0; i < parameters.length; i++) {
            result += parameters[i].getType().getCanonicalText();

            if (i != parameters.length-1)
                result += ",";
        }
        result += ")";

        return result;
    }

    public static String toString(PsiElement element) {
        if (element instanceof PsiClass) {
            String qualifiedName = ((PsiClass) element).getQualifiedName();
            if (qualifiedName != null)
                return qualifiedName;
            else
                return element.toString();
        } else if (element instanceof PsiMethod) {
            PsiMethod m = (PsiMethod) element;
            return m.getContainingClass().getQualifiedName() + "." + toString(m.getSignature(PsiSubstitutor.EMPTY));
        } else {
            return element.toString();
        }
    }

    private static String toString(MethodSignature signature) {
        String result = signature.getName() + "(";
        PsiType[] types = signature.getParameterTypes();

        if (types.length > 0)
            result += types[0].getCanonicalText();

        for (int i = 1; i < types.length; i++) {
            result += ", " + types[i].getCanonicalText();
        }

        result += ")";

        return result;
    }

    public static PsiFileSystemItem virtualFileToPsi(Project project, VirtualFile root) {
        if (root.isDirectory()) {
            return PsiManager.getInstance(project).findDirectory(root);
        } else {
            return PsiManager.getInstance(project).findFile(root);
        }
    }

    public static void logWarning(Class pClass, Object o) {
        Logger.getInstance(pClass).warn(o.toString());
    }

    public static boolean isSourceElement(PsiClass pClass) {
        return pClass != null &&
                pClass.getContainingFile() != null &&
                pClass.getContainingFile().getVirtualFile() != null &&
                pClass.getContainingFile().getVirtualFile().getCanonicalPath() != null &&
                !pClass.getContainingFile().getVirtualFile().getCanonicalPath().endsWith(".class");
    }

    public static boolean isSourceElement(PsiMethod pMethod) {
        if (pMethod == null ||
                pMethod.getContainingFile() == null ||
                pMethod.getContainingFile().getVirtualFile() == null ||
                pMethod.getContainingFile().getVirtualFile().getCanonicalPath() == null)
            return false;

        return !pMethod.getContainingFile().getVirtualFile().getCanonicalPath().endsWith(".class");
    }
}
