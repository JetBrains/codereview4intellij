package utils;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Alisa.Afonina
 * Date: 8/1/11
 * Time: 3:01 PM
 */
public class Util extends AbstractProjectComponent implements DumbAware {
    private Map<String, String> issue2URL = new HashMap<String, String>();
    protected Util(Project project) {
        super(project);
        issue2URL.put("PLUGIN-", "http://codereview4intellij.myjetbrains.com/youtrack/issue/");
    }

    public static Util getInstance(@NotNull Project project) {
        return project.getComponent(Util.class);
    }

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

    public static int find(String originalText, String originalPattern, boolean caseSensitive) {
        if(originalText == null || originalPattern == null) return -1;
        String text = caseSensitive ? originalText : originalText.toLowerCase();
        String pattern = caseSensitive ? originalPattern : originalPattern.toLowerCase();
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

    @Nullable
    public String getCheckSum(String filePath) {
        String text = getFileContents(filePath);
        try {
            if(text == null) return null;
            byte[] textBytes = text.getBytes("UTF-8");
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            final BigInteger bigInteger = new BigInteger(1, messageDigest.digest(textBytes));
            return bigInteger.toString(16);

        } catch (NoSuchAlgorithmException e) {
            //
        } catch (UnsupportedEncodingException e) {
            //
        }
        return null;
    }

    @Nullable
    private String getFileContents(String filePath) {
        Document document = getDocument(filePath);
        if(document == null) return null;
        return document.getText();
    }

    @Nullable
    public Document getDocument(String filePath) {
        VirtualFile file = getVirtualFile(filePath);
        if(file == null) return null;
        return FileDocumentManager.getInstance().getDocument(file);
    }

    @Nullable
    public VirtualFile getVirtualFile(String filePath) {
        VirtualFile baseDir = myProject.getBaseDir();
        if(baseDir == null)  {return null;}
        return baseDir.findFileByRelativePath(filePath);
    }

    @Nullable
    public OpenFileDescriptor getOpenFileDescriptor(String filePath, int offset) {
        final VirtualFile virtualFile = getVirtualFile(filePath);
        if(virtualFile == null) return null;
        return new OpenFileDescriptor(myProject, virtualFile, offset);
    }


    public String getHTMLContents(String text) {
        String[] parts = text.split("\\s");
        String result = text;
        for(String part : parts) {
            for(String prefix: issue2URL.keySet()) {
                if(part.startsWith(prefix)) {
                    String issueNumber = getFirstInt(part.substring(prefix.length()));
                    final CharSequence url = "<a href=\"" + issue2URL.get(prefix);
                    final String target = prefix + issueNumber;
                    result = result.replace(target, url + target + "\">" + target + "</a>");
                }
            }
            try {
                URL url = new URL(part);
                result = result.replace(part, "<a href=\"" + url + "\">"+ url + "</a>");
            } catch (MalformedURLException ignored) {}
        }
        return result;
    }

    private String getFirstInt(String string) {
        String number = "";
        for(int i = 0; i < string.length(); ++i) {
            final char digit = string.charAt(i);
            if(Character.isDigit(digit)) {
                number += digit;
            } else {
                break;
            }
        }
        return number;
    }
}


