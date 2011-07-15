package ui.reviewpoint;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:54 PM
 */
public class ReviewPoint {
    private OpenFileDescriptor target;
    private RangeHighlighter highlighter = null;
    private String url;
    private int line;

    public ReviewPoint(Project project, VirtualFile file, int line) {
        target = new OpenFileDescriptor(project, file, line, -1, true);
        if (line >= 0) {
            Document document = FileDocumentManager.getInstance().getDocument(target.getFile());
            MarkupModelEx markup = (MarkupModelEx) document.getMarkupModel(project);
            highlighter = markup.addPersistentLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
            highlighter.setGutterIconRenderer(new ReviewGutterIconRenderer());
        }

       this.line = line;
       this.url = file.getUrl();
    }

    public String getUrl() {
        return url;
    }

    public int getLine() {
        return line;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewPoint that = (ReviewPoint) o;

        if (line != that.line) return false;
        if (!url.equals(that.url)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url.hashCode();
        result = 31 * result + line;
        return result;
    }

    class ReviewGutterIconRenderer extends GutterIconRenderer {
        private final Icon icon = IconLoader.getIcon("/actions/cleanHeavy.png");
        @NotNull
        @Override
        public Icon getIcon() {
            return icon;
        }

         @Override
         public boolean equals(Object o) {
           if (this == o) return true;
           if (o == null || getClass() != o.getClass()) return false;

           ReviewGutterIconRenderer that = (ReviewGutterIconRenderer) o;
           return icon.equals(that.getIcon());
         }

         @Override
          public int hashCode() {
           return getIcon().hashCode();
        }
    }
}
