package ui.gutterpoint;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.GutterDraggableObject;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import ui.actions.DeleteReviewAction;
import ui.actions.EditReviewAction;
import ui.actions.ReviewActionManager;
import ui.actions.ShowReviewAction;
import ui.reviewtoolwindow.ReviewView;

import javax.swing.*;
import java.awt.*;
import java.awt.dnd.DragSource;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:54 PM
 */
public class ReviewPoint{
    private final Review review;
    private GutterIconRenderer gutterIconRenderer;
    private RangeHighlighter highlighter = null;
    public static final DataKey<ReviewPoint> REVIEW_POINT_DATA_KEY = DataKey.create("ReviewPoint");

    public ReviewPoint(Review review) {
        this.review = review;
    }

    public void updateUI() {
        final Project project = review.getProject();
        if(project == null) return;
        final ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        if(review.isValid()) {
            if(highlighter == null) {
                OpenFileDescriptor element = review.getElement();
                if(element == null) return;
                Document document = FileDocumentManager.getInstance().getDocument(element.getFile());
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
                        if(!highlighter.isValid()) {
                            review.setValid(false);
                        }
                        else {
                            review.getReviewBean().setStart(newStart);
                            review.getReviewBean().setEnd(newEnd);
                        }
                        ReviewManager.getInstance(project).changeReview(review);
                    }
                },  project);
                return;
            }
            if(highlighter.isValid()) {
                return;
            }
        }
        if(review.getReviewBean().isDeleted() && highlighter != null) {
            highlighter.dispose();
        }
        ReviewManager.getInstance(review.getProject()).logInvalidReview(review);
    }

    public GutterIconRenderer getGutterIconRenderer() {
        return gutterIconRenderer;
    }

    public Review getReview() {
        return review;
    }

    private class ReviewGutterIconRenderer extends GutterIconRenderer{
        private final Icon icon = IconLoader.getIcon("/images/note.png");
        @NotNull
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public GutterDraggableObject getDraggableObject() {
            return new GutterDraggableObject() {
                public boolean copy(int line, VirtualFile file) {
                  return file != null && moveTo(file, line);
                }

                public Cursor getCursor(int line) {
                  return canMoveTo(line)? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop;
                }
            };
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
            if(!review.isActivated()) {
                review.setActivated(true);
                List<ReviewItem> reviewItems = review.getReviewItems();
                if(!reviewItems.get(reviewItems.size() - 1).getAuthor().equals(System.getProperty("user.name"))) {
                    return new EditReviewAction("Add review");
                }
                else {
                    return new ShowReviewAction("View review");
                }
            } else {
                return  new AnAction() {
                    @Override
                    public void actionPerformed(AnActionEvent e) {
                        ReviewActionManager.getInstance(review).disposeActiveBalloon();
                    }
                };
            }
        }

        @Override
        public String getTooltipText() {
            List<ReviewItem> reviewItems = review.getReviewItems();
            return "<b>Name:</b> " + review.getName() + "\n<b>Last edited by:</b> " + reviewItems.get(reviewItems.size()-1).getAuthor();
        }

        @Override
        public int hashCode() {
            return getIcon().hashCode();
        }

        @Nullable
        public ActionGroup getPopupMenuActions() {
            DefaultActionGroup group = new DefaultActionGroup();
            List<ReviewItem> reviewItems = review.getReviewItems();
            if(!reviewItems.get(reviewItems.size() - 1).getAuthor().equals(System.getProperty("user.name"))) {
                     group.add(new EditReviewAction("Add New Comment..."));
                }
                else {
                    group.add(new ShowReviewAction("Edit Last Comment... "));
                }
            group.add(new DeleteReviewAction("Delete Review..."));
            return group;
        }
    }

    private boolean canMoveTo(int line) {
        return line >= 0;
    }

    private boolean moveTo(VirtualFile virtualFile, int line) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if(document == null) return false;
        MarkupModelEx markup = (MarkupModelEx) document.getMarkupModel(review.getProject());
        if(line < 0) return false;
        RangeHighlighter newHighlighter = markup.addPersistentLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
        if(newHighlighter == null || !newHighlighter.isValid()) return false;
        newHighlighter.setGutterIconRenderer(highlighter.getGutterIconRenderer());
        highlighter.dispose();
        highlighter = newHighlighter;
        review.setLine(line);
        ReviewManager.getInstance(review.getProject()).changeReview(review);
        return true;
    }

}
