package ui.reviewpoint;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.persistent.ReviewItem;
import reviewresult.ReviewManager;
import ui.actions.DeleteReviewAction;
import ui.actions.EditReviewAction;
import ui.actions.ReviewActionManager;
import ui.actions.ShowReviewAction;
import ui.reviewtoolwindow.ReviewView;

import javax.swing.*;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:54 PM
 */
public class ReviewPoint {
    private Review review;
    private GutterIconRenderer gutterIconRenderer;
    private RangeHighlighter highlighter = null;

    public ReviewPoint(Review review) {
        this.review = review;
    }

    public void updateUI() {
        final Project project = review.getProject();
        final ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        if(review.isValid()) {
            if(highlighter == null) {
                Document document = FileDocumentManager.getInstance().getDocument(review.getElement().getFile());
                if(project == null) return;
                if(document == null) return;
                MarkupModelEx markup = (MarkupModelEx) document.getMarkupModel(project);
                int line = review.getLine();
                if(line < 0) return;
                highlighter = markup.addPersistentLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
                if(highlighter == null) return;
                gutterIconRenderer = new ReviewGutterIconRenderer();
                highlighter.setGutterIconRenderer(gutterIconRenderer);
                document.addDocumentListener(new DocumentAdapter(){
                    @Override
                    public void documentChanged(DocumentEvent event) {

                        int newStart = highlighter.getStartOffset();
                        int newEnd = highlighter.getEndOffset();
                        if(newStart >= review.getReviewBean().getEnd() || !highlighter.isValid()) {
                            review.setValid(false);
                        }
                        else {
                            review.getReviewBean().setStart(newStart);
                            review.getReviewBean().setEnd(newEnd);
                        }
                        reviewView.updateUI();
                    }
                },  project);
                return;
            }
            if(highlighter.isValid()) {

                reviewView.updateUI();
                return;
            }
        }
        if(review.getReviewBean().isDeleted() && highlighter != null) {
            highlighter.dispose();
        }
        reviewView.updateUI();
        ReviewManager.getInstance(review.getProject()).logInvalidReview(review);
    }

    public GutterIconRenderer getGutterIconRenderer() {
        return gutterIconRenderer;
    }

    public Review getReview() {
        return review;
    }


    private class ReviewGutterIconRenderer extends GutterIconRenderer {
        private final Icon icon = IconLoader.getIcon("/images/note.png");
        private boolean activated = false;
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
        public AnAction getClickAction() {
            activated = !activated;
            if(activated) {
                if(ReviewPoint.this.getReview().isActivated()) {
                    return null;
                } else {
                    List<ReviewItem> reviewItems = review.getReviewItems();
                    if(!reviewItems.get(reviewItems.size() - 1).getAuthor().equals(System.getProperty("user.name"))) {
                        return new EditReviewAction("Add review", ReviewPoint.this);
                    }
                    else {
                        return new ShowReviewAction("View review", ReviewPoint.this);
                    }
                }

            } else {
                return  new AnAction() {
                    @Override
                    public void actionPerformed(AnActionEvent e) {
                        ReviewActionManager.disposeActiveBalloon();
                        ReviewPoint.this.getReview().setActivated(false);
                    }
                };
            }
        }

        @Override
        public int hashCode() {
            return getIcon().hashCode();
        }

        @Nullable
        public ActionGroup getPopupMenuActions() {
            DefaultActionGroup group = new DefaultActionGroup();
            group.add(new EditReviewAction("Add review", ReviewPoint.this));
            group.add(new DeleteReviewAction("Delete review", ReviewPoint.this.getReview()));
            return group;
        }
    }
}
