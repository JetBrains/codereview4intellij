package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import ui.gutterpoint.ReviewPoint;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 8/2/11
 * Time: 5:32 PM
 */
public class ShowReviewAction extends AnAction {
     private static final Icon ICON = IconLoader.getIcon("/images/note_edit.png");

    private ReviewPoint reviewPoint;

    public ShowReviewAction(String title, ReviewPoint reviewPoint) {
        super(title, title, ICON);
        this.reviewPoint = reviewPoint;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) return;
        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (editor != null) {
            ReviewActionManager.getInstance(reviewPoint.getReview()).showExistingComments(editor);
        }
    }
}
