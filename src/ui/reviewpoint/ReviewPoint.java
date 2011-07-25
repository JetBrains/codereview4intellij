package ui.reviewpoint;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.ex.RangeHighlighterEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import ui.actions.DeleteReviewsAction;
import ui.actions.EditReviewAction;
import ui.reviewtoolwindow.ReviewView;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:54 PM
 */
public class ReviewPoint {
    private Review review;
    private GutterIconRenderer gutterIconRenderer;
    private RangeHighlighter highlighter;

    public ReviewPoint(Review review) {
        this.review = review;
    }

    public void updateUI() {
        Document document = FileDocumentManager.getInstance().getDocument(review.getElement().getFile());
        final Project project = review.getProject();
        if(project == null) return;
        if(document == null) return;
        MarkupModelEx markup = (MarkupModelEx) document.getMarkupModel(project);
        highlighter = markup.addPersistentLineHighlighter(review.getLine(), HighlighterLayer.ERROR + 1, null);
        if(highlighter == null) return;
        gutterIconRenderer = new ReviewGutterIconRenderer();
        highlighter.setGutterIconRenderer(gutterIconRenderer);
        document.addDocumentListener(new DocumentListener(){

            @Override
            public void beforeDocumentChange(DocumentEvent event) {
            }

            @Override
            public void documentChanged(DocumentEvent event) {
                review.getReviewBean().setStart(highlighter.getStartOffset());
                review.getReviewBean().setEnd(highlighter.getEndOffset());
                ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
                reviewView.updateUI();
            }
        },  project);
    }


    public void release() {
        highlighter.dispose();
    }
    public GutterIconRenderer getGutterIconRenderer() {
        return gutterIconRenderer;
    }

    public Review getReview() {
        return review;
    }

    private class ReviewGutterIconRenderer extends GutterIconRenderer {
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

        @Nullable
        public ActionGroup getPopupMenuActions() {
            DefaultActionGroup group = new DefaultActionGroup();
            group.add(new EditReviewAction("Add review", ReviewPoint.this));
            group.add(new DeleteReviewsAction("Delete review", ReviewPoint.this.review));
            return group;
        }
    }
}
