package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import reviewresult.Review;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 5:03 PM
 */
public class AddReviewItemAction extends AnAction  implements DumbAware {
  private static final Icon ICON = IconLoader.getIcon("/images/note_edit.png");

    public AddReviewItemAction() {
    }

    public AddReviewItemAction(String title) {
        super(title, title, ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) return;
        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if(editor != null) {
            Review review = ActionManager.getInstance().getReviewForAction(e);
            if(review == null || !review.isValid()) {return;}
            ReviewPoint reviewPoint = ReviewPointManager.getInstance(project).findReviewPoint(review);
            ReviewActionManager.getInstance().addToExistingComments(editor, reviewPoint);
        }
    }
}
