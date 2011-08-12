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

    private ReviewPanel reviewPanel;

    public void initToolWindow(Project project, ToolWindow toolWindow ) {
        reviewPanel = new ReviewPanel(project);
        Content allReviewsContent= ContentFactory.SERVICE.getInstance().createContent(reviewPanel, "Reviews" ,false);
        toolWindow.getContentManager().addContent(allReviewsContent);
    }


    //todo if it nesseccary? mb killit
    public void updateUI() {
        if(reviewPanel == null)return;
        reviewPanel.updateUI();
    }

    public static void showTwoCommentsOnOnewLineMessage(Review oldReview) {
        String[] values = {"OK", "Cancel"};
        if(Messages.showDialog( "This line already contains one comment. " +
                                    "Would you like to add your comment to existing?",
                "Two comments one one line",values, 0, Messages.getWarningIcon()) == 0) {
            ReviewActionManager.getInstance(oldReview).addToExistingComments(FileEditorManager.getInstance(oldReview.getProject()).getSelectedTextEditor());
        }
    }
}
