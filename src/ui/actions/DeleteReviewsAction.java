package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import reviewresult.ReviewManager;
import ui.reviewpoint.ReviewPoint;
import ui.reviewtoolwindow.ReviewView;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:41 PM
 */
public class DeleteReviewsAction extends AnAction  implements DumbAware {

  private static final Icon ICON = IconLoader.getIcon("/images/note_delete.png");

    private ReviewPoint reviewPoint;

  public DeleteReviewsAction(String title, ReviewPoint reviewPoint) {
        super(title, title, ICON);
        this.reviewPoint = reviewPoint;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = reviewPoint.getReview().getProject();
        ReviewManager.getInstance(project).removeReview(reviewPoint);
        ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        reviewView.updateUI();
    }
}
