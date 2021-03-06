package ui.reviewtoolwindow;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:49 PM
 */
public class ReviewView {
    public void initToolWindow(Project project, ToolWindow toolWindow ) {
        ReviewPanel reviewPanel = new ReviewPanel(project);
        Content allReviewsContent = ContentFactory.SERVICE.
                                            getInstance().createContent(
                                                                reviewPanel,
                                                                "Reviews",
                                                                false);
        toolWindow.getContentManager().addContent(allReviewsContent);
    }
}
