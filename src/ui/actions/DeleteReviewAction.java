package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.*;
import com.intellij.openapi.editor.Document;
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
public class DeleteReviewAction extends AnAction  implements DumbAware/*, UndoableAction*/ {
    private static final Icon ICON = IconLoader.getIcon("/images/note_delete.png");

    public DeleteReviewAction() {
        super(ReviewsBundle.message("reviews.deleteReviewEllipsis"),
              ReviewsBundle.message("reviews.deleteReviewEllipsis"),
              ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(PlatformDataKeys.PROJECT);
        if(project == null) return;
        Review review = ReviewActionManager.getInstance().getReviewForAction(e);

        if(review == null) {
            review = e.getData(Review.REVIEW_DATA_KEY);
        }
        String filepath = null;
        if(review != null) {
            if(Messages.showYesNoDialog(project, ReviewsBundle.message("reviews.deleteReviewQuestion"),
                                                 ReviewsBundle.message("reviews.deleteReview"),
                                                 Messages.getQuestionIcon()) == Messages.YES) {
                ReviewManager.getInstance(project).removeReview(review);
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
        final Review finalReview = review;
        final String finalFilepath = filepath;

        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            public void run() {
                if (finalReview != null) {
                    final Document document = Util.getInstance(project).getDocument(finalReview.getFilePath());
                    UndoManager.getInstance(project).
                            undoableActionPerformed(
                                    new UndoReviewRemovalAction(finalReview, document));
                } else {
                    if (finalFilepath != null) {
                    final Document document = Util.getInstance(project).getDocument(finalFilepath);
                    UndoManager.getInstance(project).
                            undoableActionPerformed(
                                    new UndoReviewRemovalAction(project, finalFilepath, document));
                    }
                }
            }
        }, "add review", null);
    }
}
