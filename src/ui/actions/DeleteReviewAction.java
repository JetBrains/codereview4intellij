package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.command.undo.UnexpectedUndoException;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import utils.ReviewsBundle;
import utils.Util;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:41 PM
 */
public class DeleteReviewAction extends AnAction  implements DumbAware, UndoableAction {
    private static final Icon ICON = IconLoader.getIcon("/images/note_delete.png");
    private Review review = null;
    private Project project;
    private String filepath;

    public DeleteReviewAction() {
        super(ReviewsBundle.message("reviews.deleteReviewEllipsis"),
              ReviewsBundle.message("reviews.deleteReviewEllipsis"),
              ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {

        final Project project = e.getData(PlatformDataKeys.PROJECT);
        //CommandProcessor.getInstance().executeCommand(project, , "", null);
        if(project == null) return;
      /*  new WriteCommandAction(project) {
             @Override
             protected void run(Result result) throws Throwable {
                UndoManager.getInstance(project).undoableActionPerformed(DeleteReviewAction.this);
             }
        }.execute();*/
        Review review = ReviewActionManager.getInstance().getReviewForAction(e);

        if(review == null) {
            review = e.getData(Review.REVIEW_DATA_KEY);
        }
        this.project = project;
        if(review != null) {
            if(Messages.showYesNoDialog(project, ReviewsBundle.message("reviews.deleteReviewQuestion"),
                                                 ReviewsBundle.message("reviews.deleteReview"),
                                                 Messages.getQuestionIcon()) == Messages.YES) {
                ReviewManager.getInstance(project).removeReview(review);
                this.review = review;
            }
        } else {
            VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
            if(file != null) {
                String fileType = "file";
                if(file.isDirectory())
                    fileType = "folder";
                if(Messages.showOkCancelDialog(project, ReviewsBundle.message("reviews.deleteReviewsQuestion", fileType),
                                                        ReviewsBundle.message("reviews.deleteReviews"),
                                                        null) == Messages.OK) {

                    filepath = Util.getFilePath(project, file);
                    ReviewManager.getInstance(project).removeAll(filepath);

                }
            }
        }
    }

    @Override
    public void undo() throws UnexpectedUndoException {
        if(review != null)
            ReviewManager.getInstance(project).undoReviewRemoval(review);
        if(filepath != null)
            ReviewManager.getInstance(project).undoMultipleReviewRemoval(filepath);
    }

    @Override
    public void redo() throws UnexpectedUndoException {
        if(review != null) {
            if(!review.isDeleted()) {
                ReviewManager.getInstance(project).removeReview(review);
            }
        }
        if(filepath != null && "".equals(filepath)) {
            ReviewManager.getInstance(project).removeAll(filepath);
        }
    }

    @Override
    public DocumentReference[] getAffectedDocuments() {
        return new DocumentReference[0];
    }

    @Override
    public boolean isGlobal() {
        return false;
    }
}
