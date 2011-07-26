package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.reviewpoint.ReviewPoint;
import ui.reviewtoolwindow.ReviewView;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:41 PM
 */
public class DeleteReviewsAction extends AnAction{

    private ReviewPoint reviewPoint;

    public DeleteReviewsAction(String title, ReviewPoint reviewPoint) {
        super(title);
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
