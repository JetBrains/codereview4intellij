package ui.actions;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
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
            setFocus(editReviewForm.getNewItemTextField());
        }
    }

    private void setFocus(final Component component) {
        IdeFocusManager.getInstance(getReview().getProject()).doWhenFocusSettlesDown(new Runnable() {
            public void run() {
                IdeFocusManager.getInstance(getReview().getProject()).requestFocus(component, true);
            }
        });
    }

    public void addNewComment(final Editor editor) {
        final Point point = editor.logicalPositionToXY(editor.getCaretModel().getLogicalPosition());//editor.getContentComponent().getMousePosition();
        editReviewForm = new EditReviewForm(getReview(), true);
        JComponent content = editReviewForm.getContent();
        showBalloon(editor, point, content, editor.getContentComponent());
        setFocus(editReviewForm.getNameTextField());
    }

    private void showBalloon(final Editor editor, final Point point, JComponent balloonContent, final JComponent contentComponent) {
        if(!getReview().isValid()) return;
        hideBalloon();
        balloonContent.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "saveReview");
        balloonContent.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitReview");

        balloonContent.getActionMap().put("saveReview", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editReviewForm.saveReview();
            }
        });
        balloonContent.getActionMap().put("exitReview", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getActiveBalloon().hide();
            }
        });

        balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, "Add Comment");
        balloonBuilder.setHideOnClickOutside(true);
        balloonBuilder.setHideOnKeyOutside(true);
        setActiveBalloon(balloonBuilder.createBalloon());
        editReviewForm.setBalloon(getActiveBalloon());
        if(getActiveBalloon() == null) return;
        getActiveBalloon().show(new ReviewPositionTracker(editor, contentComponent, point) , Balloon.Position.atRight);
    }

    private void hideBalloon() {
        if(getActiveBalloon() != null)
            if(!getActiveBalloon().isDisposed())
                getActiveBalloon().hide();
    }

    public void disposeActiveBalloon() {
        if(activeBalloon != null) {
            review.setActivated(false);
            activeBalloon.dispose();
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
            setFocus(editReviewForm.getLastExistingTextField());
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

    private class ReviewPositionTracker extends PositionTracker<Balloon>{

        private Editor editor;
        private JComponent component;
        private Point point;

        public ReviewPositionTracker(Editor editor, JComponent component, Point point) {
            super(component);
            this.editor = editor;
            this.component = component;
            this.point = point;
        }

        @Override
        public RelativePoint recalculateLocation(final Balloon object) {
            if (!editor.getScrollingModel().getVisibleArea().contains(point)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        object.hide();
                    }
                });
                 if(!object.isDisposed()) {
                    final PositionTracker tracker = this;
                    final VisibleAreaListener listener = new VisibleAreaListener() {
                       @Override
                       public void visibleAreaChanged(VisibleAreaEvent e) {
                            if(e.getNewRectangle().contains(point) && object.isDisposed()) {
                                hideBalloon();
                                setActiveBalloon(balloonBuilder.createBalloon());
                                editReviewForm.setBalloon(getActiveBalloon());
                                getActiveBalloon().show(tracker, Balloon.Position.atRight);
                                editor.getScrollingModel().removeVisibleAreaListener(this);
                            }
                       }
                    };
                    editor.getScrollingModel().addVisibleAreaListener(listener);
                 }
            }
            return new RelativePoint(component, point);
        }

    }
}
