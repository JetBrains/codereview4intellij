package ui.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.PositionTracker;
import reviewresult.Review;
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
    private static Balloon activeBalloon = null;
    private BalloonBuilder balloonBuilder;
    private EditReviewForm editReviewForm;
    private static ReviewActionManager instance;
    private Review review;


    private ReviewActionManager() {}

    public static ReviewActionManager getInstance(Review review) {
        if(instance == null) {
            instance = new ReviewActionManager();
        }
        instance.review = review;
        return instance;
    }

    public void addToExistingComments(final Editor editor, ReviewPoint reviewPoint) {
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            editReviewForm = new EditReviewForm(reviewPoint.getReview());
            JComponent content = editReviewForm.getContent();
            balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(content, "Add Comment");
            Balloon balloon = balloonBuilder.createBalloon();
            editReviewForm.setBalloon(balloon);
            activeBalloon = balloon;

            final Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            balloon.show(new PositionTracker<Balloon>(editor.getContentComponent()){
                @Override
                public RelativePoint recalculateLocation(Balloon object) {
                    if(editor.getScrollingModel().getVisibleArea().contains(point)) {
                        if(object.isDisposed()) activeBalloon = balloonBuilder.createBalloon();
                        object = activeBalloon;
                        editReviewForm.setBalloon(activeBalloon);
                        return new RelativePoint(gutterComponent, centerIconPoint);
                    }
                    else {
                        object.hide();
                        return null;
                    }
                }
            }, Balloon.Position.atRight);
            ActionCallback callback = IdeFocusManager.getInstance(review.getProject()).requestFocus(editReviewForm.getItemTextField(), true);
        }
    }

    public void addNewComment(final Editor editor) {
        final Point point = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
        editReviewForm = new EditReviewForm(review);
        JComponent content = editReviewForm.getContent();
        balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(content, "Add Comment");
        balloonBuilder.setHideOnClickOutside(true);
        activeBalloon = balloonBuilder.createBalloon();
        activeBalloon.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                review.setActivated(false);
            }
        });
        editReviewForm.setBalloon(activeBalloon);
        activeBalloon.show(
                new PositionTracker<Balloon>(editor.getContentComponent()){
                @Override
                public RelativePoint recalculateLocation(Balloon object) {
                    if(editor.getScrollingModel().getVisibleArea().contains(point)) {
                        if(object.isDisposed()) activeBalloon = balloonBuilder.createBalloon();
                        object = activeBalloon;
                        editReviewForm.setBalloon(activeBalloon);
                        return new RelativePoint(editor.getContentComponent(), point);
                    }
                    else {
                        object.hide();
                        return null;
                    }
                }
            }, Balloon.Position.atRight);

        ActionCallback callback = IdeFocusManager.getInstance(review.getProject()).requestFocus(editReviewForm.getNameTextField(), true);
        /*editor.getScrollingModel().addVisibleAreaListener(new VisibleAreaListener() {
            @Override
            public void visibleAreaChanged(VisibleAreaEvent e) {
                if(e.getNewRectangle().contains(point)) {
                    if(activeBalloon != null)  activeBalloon.hide();
                    if(review.isActivated() && (activeBalloon == null  || activeBalloon.isDisposed()))
                    {
                        activeBalloon = balloonBuilder.createBalloon();
                        editReviewForm.setBalloon(activeBalloon);
                        activeBalloon.show(new RelativePoint(editor.getContentComponent(), point), Balloon.Position.atRight);
                    }
                    else {
                      if(activeBalloon != null) {
                          activeBalloon.dispose();
                      }
                    }
                }
                else {
                    if(activeBalloon != null) activeBalloon.hide();
                    activeBalloon = null;
                }

            }
        });     */
    }

    public static void disposeActiveBalloon() {
        activeBalloon.dispose();
    }
}
