package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import ui.forms.EditReviewForm;
import ui.reviewpoint.ReviewPoint;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 5:03 PM
 */
public class EditReviewAction extends AnAction  implements DumbAware {
    private ReviewPoint reviewPoint;
    public EditReviewAction(String title, ReviewPoint reviewPoint) {
        super(title);
        this.reviewPoint = reviewPoint;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) return;
        Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
        if (editor != null) {
            ReviewActionManager.getInstance(reviewPoint.getReview()).addToExistingComments(editor, reviewPoint);
        }
    }
}
