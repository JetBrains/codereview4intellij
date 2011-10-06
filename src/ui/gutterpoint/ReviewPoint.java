package ui.gutterpoint;

import com.intellij.lang.Commenter;
import com.intellij.lang.LanguageCommenters;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.markup.GutterDraggableObject;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import ui.actions.AddReviewItemAction;
import ui.actions.ConvertToTextCommentAction;
import ui.actions.DeleteReviewAction;
import ui.actions.ReviewActionManager;
import utils.ReviewsBundle;
import utils.Util;

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

    public ReviewPoint(Review review) {
        this.review = review;
    }

    public void updateUI() {
        final Project project = review.getProject();
        if(project == null) return;
        final Document document = Util.getInstance(review.getProject()).getDocument(review.getFilePath());
        if(document == null) return;
        if(review.isValid()) {
            if(highlighter == null) {

                int line = review.getLineNumber();
                if(line < 0) return;
                OpenFileDescriptor openFileDescriptor = Util.getInstance(project).
                                                        getOpenFileDescriptor(review.getFilePath(),
                                                                review.getStart());
                final Editor editor = FileEditorManagerEx.getInstance(project).
                                                          openTextEditor(openFileDescriptor, true);
                if(editor == null) return;

                MarkupModelEx markup = (MarkupModelEx) editor.getMarkupModel();
                //
                highlighter = markup.addPersistentLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
                if(highlighter == null) return;
                gutterIconRenderer = new ReviewGutterIconRenderer();
                highlighter.setGutterIconRenderer(gutterIconRenderer);
                DocumentAdapter listener = new DocumentAdapter() {
                    @Override
                    public void documentChanged(DocumentEvent event) {

                        int newStart = highlighter.getStartOffset();
                        int newEnd = highlighter.getEndOffset();
                        if (!highlighter.isValid()) {
                            review.setValid(false);
                        } else {
                            review.setStart(newStart);
                            review.setEnd(newEnd);
                        }
                        ReviewManager.getInstance(project).changeReview(review);
                    }
                };
                document.addDocumentListener(listener,  project);
                return;
            }
            if(highlighter.isValid()) {
                return;
            }
        }
        if(review.isDeleted() && highlighter != null) {
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
        public boolean isNavigateAction() {
            return true;
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
            return ReviewActionManager.getInstance().getReviewPointClickAction(review);
        }



        @Override
        public String getTooltipText() {
            List<ReviewItem> reviewItems = review.getReviewItems();
            return review.getPresentationInfo(false) + "\n<b>Last edited by:</b> " +
                   reviewItems.get(reviewItems.size()-1).getAuthor();
        }

        @Override
        public int hashCode() {
            return getIcon().hashCode();
        }

        @Nullable
        public ActionGroup getPopupMenuActions() {
            DefaultActionGroup group = new DefaultActionGroup();
            String title = ReviewsBundle.message("reviews.addNewCommentEllipsis");
            if( review.isLastReviewItemMine()) {
                title = ReviewsBundle.message("reviews.editLastCommentEllipsis");
            }
            group.add(new AddReviewItemAction(title));
            group.add(new DeleteReviewAction());

            final ConvertToTextCommentAction textCommentAction =
                    new ConvertToTextCommentAction(ReviewsBundle.message("reviews.convertReviewToText"));

            Project project = review.getProject();
            Document document = Util.getInstance(project).getDocument(review.getFilePath());
            if(document == null) return null;
            final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
            if(psiFile == null) return null;
            final Commenter commenter = LanguageCommenters.INSTANCE.forLanguage(psiFile.getLanguage());

            String commentPrefix = commenter.getBlockCommentPrefix();
            String lineCommentPrefix = commenter.getLineCommentPrefix();
            if(!(lineCommentPrefix == null && commentPrefix == null)) {
                group.add(textCommentAction);
            }
            return group;
        }
    }

    private boolean canMoveTo(int line) {
        return line >= 0;
    }

    private boolean moveTo(VirtualFile virtualFile, int line) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if(document == null) return false;
        Editor editor = FileEditorManager.getInstance(review.getProject()).getSelectedTextEditor();
        if(editor == null) return false;
        MarkupModelEx markup = (MarkupModelEx) editor.getMarkupModel();
        if(line < 0) return false;
        RangeHighlighter newHighlighter = markup.addPersistentLineHighlighter(line,
                                                                            HighlighterLayer.ERROR + 1, null);
        if(newHighlighter == null || !newHighlighter.isValid()) return false;
        newHighlighter.setGutterIconRenderer(highlighter.getGutterIconRenderer());
        highlighter.dispose();
        highlighter = newHighlighter;
        review.changeContext(document.getLineStartOffset(line), document.getLineEndOffset(line));
        ReviewManager.getInstance(review.getProject()).changeReview(review);
        return true;
    }


}
