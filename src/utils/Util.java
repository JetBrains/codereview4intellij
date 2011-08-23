package utils;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.xmlb.annotations.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

/**
 * User: Alisa.Afonina
 * Date: 8/1/11
 * Time: 3:01 PM
 */
public class Util implements DumbAware {

    private Project project;
    private static Util instance;
    private Util(@NotNull final Project project) {
        this.project = project;
    }

    public static Util getInstance(@NotNull Project project) {
        if(instance == null) instance = new Util(project);
        return instance;
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

    public static int find(String text, String pattern) {
        if(text == null || pattern == null) return -1;
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
        VirtualFile baseDir = project.getBaseDir();
        if(baseDir == null)  {return null;}
        return baseDir.findFileByRelativePath(filePath);
    }

    @Nullable
    public OpenFileDescriptor getOpenFileDescriptor(String filePath, int offset) {
        final VirtualFile virtualFile = getVirtualFile(filePath);
        if(virtualFile == null) return null;
        return new OpenFileDescriptor(project, virtualFile, offset);
    }
}


