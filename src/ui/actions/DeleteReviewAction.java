package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import sun.misc.resources.Messages_es;
import ui.reviewpoint.ReviewPoint;
import ui.reviewtoolwindow.ReviewView;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:41 PM
 */
public class DeleteReviewAction extends AnAction  implements DumbAware {

    private static final Icon ICON = IconLoader.getIcon("/images/note_delete.png");

    private Review review;

    public DeleteReviewAction() {
    }

    public DeleteReviewAction(String title, Review review) {
        super(title, title, ICON);
        this.review = review;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if(project == null) return;
        ReviewManager instance = ReviewManager.getInstance(project);
        if(review == null) review = e.getData(Review.REVIEW_DATA_KEY);
        if(review != null) {
            if(Messages.showOkCancelDialog(project, "Are you sure you want to delete review?",
                                                    "Delete review", null) == Messages.OK) {
                instance.removeReview(review);
                review = null;
            }
        } else {
            VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
            if(file != null) {
                if(Messages.showOkCancelDialog(project, "Are you sure you want to delete all reviews in this file?",
                                                        "Delete reviews", null) == Messages.OK) {
                    instance.removeAll(file);
                }
            }

        }


    }
}
