package components.reviewpoint;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:54 PM
 */
public class ReviewPoint {
    private OpenFileDescriptor target;
    private static final Icon TICK = IconLoader.getIcon("/gutter/check.png");
    private RangeHighlighter myHighlighter = null;
    private String url;
    private int lineNumber;

    public ReviewPoint(Project project, VirtualFile file, int lineNumber) {
        target = new OpenFileDescriptor(project, file, lineNumber, -1, true);
        if (lineNumber >= 0) {
            Document document = FileDocumentManager.getInstance().getDocument(target.getFile());
            MarkupModelEx markup = (MarkupModelEx) document.getMarkupModel(project);
            //TODO properly initialize highlighter
            myHighlighter = markup.addPersistentLineHighlighter(lineNumber, HighlighterLayer.ERROR + 1, null);
        }
       this.lineNumber = lineNumber;
       this.url = file.getUrl();
    }

    public String getUrl() {
        return url;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewPoint that = (ReviewPoint) o;

        if (lineNumber != that.lineNumber) return false;
        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + lineNumber;
        return result;
    }
}
