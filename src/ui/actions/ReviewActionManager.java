package ui.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.ActionCallback;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.BalloonImpl;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.PositionTracker;
import reviewresult.Review;
import ui.forms.EditReviewForm;
import ui.reviewpoint.ReviewPoint;
import ui.reviewpoint.ReviewPointManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:14 PM
 */
public class ReviewActionManager implements DumbAware {
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

    public void addToExistingComments(final Editor editor) {
        ReviewPoint reviewPoint = ReviewPointManager.getInstance(review.getProject()).findReviewPoint(review);
        if(reviewPoint == null) return;
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            editReviewForm = new EditReviewForm(reviewPoint.getReview(), true);
            JComponent content = editReviewForm.getContent();
            final Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            showBalloon(editor, centerIconPoint, content, gutterComponent);
        }
    }

    public void addNewComment(final Editor editor) {
        final Point point = editor.visualPositionToXY(editor.getCaretModel().getVisualPosition());
        editReviewForm = new EditReviewForm(review, true);
        JComponent content = editReviewForm.getContent();
        showBalloon(editor, point, content, editor.getContentComponent());
    }

    private void showBalloon(final Editor editor, final Point point, JComponent content, final JComponent contentComponent) {
        if(!review.isValid()) return;

        content.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "saveReview");
        content.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitReview");
        content.getActionMap().put("saveReview", new AbstractAction() {
        @Override
            public void actionPerformed(ActionEvent e) {
                editReviewForm.saveReview();
            }
        });
        content.getActionMap().put("exitReview", new AbstractAction() {
        @Override
            public void actionPerformed(ActionEvent e) {
                activeBalloon.hide();
            }
        });

        balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(content, "Add Comment");
        balloonBuilder.setHideOnClickOutside(true);
        balloonBuilder.setHideOnKeyOutside(true);
        activeBalloon = balloonBuilder.createBalloon();
        activeBalloon.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                review.setActivated(false);
            }
        });
        editReviewForm.setBalloon(activeBalloon);
        if(activeBalloon == null) return;
        activeBalloon.show(
                new PositionTracker<Balloon>(editor.getContentComponent()){
                @Override
                public RelativePoint recalculateLocation(Balloon object) {
                    if(editor.getScrollingModel().getVisibleArea().contains(point)) {
                        if(object.isDisposed()) activeBalloon = balloonBuilder.createBalloon();
                        object = activeBalloon;
                        editReviewForm.setBalloon(activeBalloon);
                        return new RelativePoint(contentComponent, point);
                    }
                    else {
                        object.hide();
                        return null;
                    }
                }
            }, Balloon.Position.atRight);

        ActionCallback callback = IdeFocusManager.getInstance(review.getProject()).requestFocus(editReviewForm.getNameTextField(), true);
    }

    public static void disposeActiveBalloon() {
        if(activeBalloon != null) {
            activeBalloon.dispose();
        }
    }

     public void showExistingComments(final Editor editor) {

        ReviewPoint reviewPoint = ReviewPointManager.getInstance(review.getProject()).findReviewPoint(review);
        if(reviewPoint == null) return;
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            editReviewForm = new EditReviewForm(reviewPoint.getReview(), false);
            JComponent content = editReviewForm.getItemsContent(true);
            final Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            showBalloon(editor, centerIconPoint, content, gutterComponent);
        }
    }
}
