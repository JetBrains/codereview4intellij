package ui.actions;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.PositionTracker;
import reviewresult.Review;
import ui.forms.EditReviewForm;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;

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
    private Balloon activeBalloon = null;
    private BalloonBuilder balloonBuilder;
    private EditReviewForm editReviewForm;
    private static ReviewActionManager instance;
    private Review review;


    private ReviewActionManager() {}

    public static ReviewActionManager getInstance(Review review) {
        if(instance == null) {
            instance = new ReviewActionManager();
        }
        instance.setReview(review);
        return instance;
    }

    public void addToExistingComments(final Editor editor) {
        ReviewPoint reviewPoint = ReviewPointManager.getInstance(getReview().getProject()).findReviewPoint(getReview());
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
        editReviewForm = new EditReviewForm(getReview(), true);
        JComponent content = editReviewForm.getContent();
        showBalloon(editor, point, content, editor.getContentComponent());
    }

    private void showBalloon(final Editor editor, final Point point, JComponent content, final JComponent contentComponent) {
        if(!getReview().isValid()) return;

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
                getActiveBalloon().hide();
            }
        });

        balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(content, "Add Comment");
        balloonBuilder.setHideOnClickOutside(true);
        balloonBuilder.setHideOnKeyOutside(true);
        setActiveBalloon(balloonBuilder.createBalloon());
        getActiveBalloon().addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                getReview().setActivated(false);
            }
        });
        editReviewForm.setBalloon(getActiveBalloon());
        if(getActiveBalloon() == null) return;
        getActiveBalloon().show(
                new PositionTracker<Balloon>(editor.getContentComponent()) {
                    @Override
                    public RelativePoint recalculateLocation(Balloon object) {
                        if (editor.getScrollingModel().getVisibleArea().contains(point)) {
                            if (object.isDisposed()) setActiveBalloon(balloonBuilder.createBalloon());
                            //object = getActiveBalloon();
                            editReviewForm.setBalloon(getActiveBalloon());
                            return new RelativePoint(contentComponent, point);
                        } else {
                            object.hide();
                            return null;
                        }
                    }
                }, Balloon.Position.atRight);

        /*ActionCallback callback = */IdeFocusManager.getInstance(getReview().getProject()).requestFocus(editReviewForm.getNameTextField(), true);
    }

    public static void disposeActiveBalloon() {
        if(instance.getActiveBalloon() != null) {
            instance.getActiveBalloon().dispose();
        }
    }

     public void showExistingComments(final Editor editor) {

        ReviewPoint reviewPoint = ReviewPointManager.getInstance(getReview().getProject()).findReviewPoint(getReview());
        if(reviewPoint == null) return;
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            editReviewForm = new EditReviewForm(reviewPoint.getReview(), false);
            JComponent content = editReviewForm.getItemsContent();
            final Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            showBalloon(editor, centerIconPoint, content, gutterComponent);
        }
    }

    private Review getReview() {
        return review;
    }

    private void setReview(Review review) {
        this.review = review;
    }

    private Balloon getActiveBalloon() {
        return activeBalloon;
    }

    private void setActiveBalloon(Balloon activeBalloon) {
        this.activeBalloon = activeBalloon;
    }
}
