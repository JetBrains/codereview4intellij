package utils;

import com.intellij.openapi.util.Pair;

import java.security.PrivateKey;

/**
 * User: Alisa.Afonina
 * Date: 8/1/11
 * Time: 3:01 PM
 */
public class Util {
    private static int[] prefixFunction(String pattern) {
        int length = pattern.length();
        int[] p = new int[length];
        for (int i = 1; i < length; i++) {
            int k = p[i - 1];
            while (k > 0 && pattern.charAt(k) != pattern.charAt(i)) {
                k = p[k - 1];
            }
            p[i] = k + (pattern.charAt(k) == pattern.charAt(i) ? 1 : 0);
        }
        return p;
    }

    public static int find(String text, String pattern) {
        int patternLength = pattern.length();
        int textLength = text.length();
        int k = 0;
        if (patternLength == 0) return 0;
        int[] prefixFunction = prefixFunction(pattern);

        for (int i = 0; i < textLength; i++) {

            while (k > 0 && text.charAt(i) != pattern.charAt(k)) k = prefixFunction[k - 1];

            if (text.charAt(i) == pattern.charAt(k)) k++;

            if (k == patternLength) return i - patternLength + 1;
        }
        return -1;
    }
}

