package ui.reviewtoolwindow;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import reviewresult.Review;
import ui.actions.ReviewActionManager;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:49 PM
 */
public class ReviewView {

    public void initToolWindow(Project project, ToolWindow toolWindow ) {
        ReviewPanel reviewPanel = new ReviewPanel(project);
        Content allReviewsContent= ContentFactory.SERVICE.getInstance().createContent(reviewPanel, "Reviews" ,false);
        toolWindow.getContentManager().addContent(allReviewsContent);
    }

    public static void showTwoCommentsOnOnewLineMessage(Review oldReview) {
        if(Messages.showYesNoDialog( "This line already contains one comment. " +
                                    "Would you like to add your comment to existing?",
                "Two comments one one line", Messages.getWarningIcon()) == Messages.YES) {
            ReviewActionManager.getInstance(oldReview).addToExistingComments(FileEditorManager.getInstance(oldReview.getProject()).getSelectedTextEditor());
        }
    }
}
