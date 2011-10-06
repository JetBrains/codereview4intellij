package ui.actions;

import com.intellij.openapi.command.undo.BasicUndoableAction;
import com.intellij.openapi.command.undo.UnexpectedUndoException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import reviewresult.Review;
import reviewresult.ReviewManager;
import utils.Util;

/**
 * User: Alisa.Afonina
 * Date: 10/6/11
 * Time: 1:40 PM
 */
public class UndoReviewRemovalAction extends BasicUndoableAction {
    private Review review;
    private Project project;
    private String filepath;
    private int offset = -1;
    private String reviewTextWithComment = "";
    private Document document;

    public UndoReviewRemovalAction(Review review, Document document) {
        super(document);
        this.review = review;
    }

    public UndoReviewRemovalAction(Review review, int offset, String reviewTextWithComment, Document document) {
        super(document);
        this.review = review;
        this.offset = offset;
        this.reviewTextWithComment = reviewTextWithComment;
        this.document = document;
    }

    public UndoReviewRemovalAction(Project project, String filepath, Document document) {
        super(document);
        this.project = project;
        this.filepath = filepath;
    }

    @Override
        public void undo() throws UnexpectedUndoException {
            if(review != null) {
                if(!"".equals(reviewTextWithComment) && offset != -1) {
                    document.deleteString(offset, offset + reviewTextWithComment.length());
                }
                ReviewManager.getInstance(review.getProject()).undoReviewRemoval(review);
            }
            if(filepath != null)
                ReviewManager.getInstance(project).undoMultipleReviewRemoval(filepath);
            }

        @Override
        public void redo() throws UnexpectedUndoException {
            if(review != null && !review.isDeleted()) {
                ReviewManager.getInstance(review.getProject()).removeReview(review);
                if(!"".equals(reviewTextWithComment) && offset != -1) {
                    document.insertString(offset, reviewTextWithComment);
                }
            }
            if(filepath != null && "".equals(filepath)) {
                ReviewManager.getInstance(project).removeAll(filepath);
            }
        }
}
