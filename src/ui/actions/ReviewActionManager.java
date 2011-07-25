package ui.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
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
 * Date: 7/25/11
 * Time: 1:14 PM
 */
public class ReviewActionManager {
     public static void addToExistingComments(Editor editor, ReviewPoint reviewPoint) {
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            EditReviewForm editReviewForm = new EditReviewForm(reviewPoint.getReview());
            BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(editReviewForm.getContent(), "Add Comment");
            Balloon balloon = balloonBuilder.createBalloon();
            editReviewForm.setBalloon(balloon);
            Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            balloon.show(new RelativePoint(gutterComponent, centerIconPoint), Balloon.Position.atRight);
        }
    }
}
