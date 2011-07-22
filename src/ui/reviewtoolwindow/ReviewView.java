package ui.reviewtoolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import reviewresult.ReviewManager;


/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:49 PM
 */
public class ReviewView {
    private ReviewManager reviewManager;
    private ReviewPanel reviewPanel;
    public void initToolWindow(Project project, ToolWindow toolWindow ) {
        reviewPanel = new ReviewPanel(project);
        Content allReviewsContent= ContentFactory.SERVICE.getInstance().createContent(reviewPanel, "Reviews" ,false);
        toolWindow.getContentManager().addContent(allReviewsContent);
    }
}
