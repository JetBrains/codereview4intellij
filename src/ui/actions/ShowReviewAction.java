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

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 8/2/11
 * Time: 5:32 PM
 */
public class ShowReviewAction extends AnAction implements DumbAware{
    private static final Icon ICON = IconLoader.getIcon("/images/note_edit.png");

    @SuppressWarnings({"UnusedDeclaration"})
    public ShowReviewAction() {
    }

    public ShowReviewAction(String title) {
        super(title, title, ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) return;

        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if(editor == null) return;
        final ReviewActionManager instance = ReviewActionManager.getInstance();
        Review review = instance.getReviewForAction(e);
        if(review == null || !review.isValid()) {return;}
        instance.showExistingComments(editor, review);
    }
}
