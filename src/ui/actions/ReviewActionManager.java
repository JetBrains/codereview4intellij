package ui.actions;


import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.PositionTracker;
import reviewresult.Review;
import reviewresult.persistent.ReviewItem;
import ui.forms.EditReviewForm;
import ui.gutterpoint.BalloonWithSelection;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:14 PM
 */
public class ReviewActionManager implements DumbAware {
    private BalloonWithSelection activeBalloon = null;
    private EditReviewForm editReviewForm;
    private static ReviewActionManager instance;

    private ReviewActionManager() {}

    public static ReviewActionManager getInstance() {
        if(instance == null) {
            instance = new ReviewActionManager();
        }
        return instance;
    }

    public void addToExistingComments(final Editor editor, ReviewPoint reviewPoint) {
        if(reviewPoint == null) return;
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            editReviewForm = new EditReviewForm(reviewPoint.getReview(), true, true);
            JComponent content = editReviewForm.getContent();
            final Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            showBalloon(editor, centerIconPoint, content, gutterComponent, "Add Comment", reviewPoint.getReview());
            editReviewForm.requestFocus();
        }
    }


    public void addNewComment(final Editor editor, Review review) {
        final Point point = editor.logicalPositionToXY(editor.getCaretModel().getLogicalPosition());
        editReviewForm = new EditReviewForm(review, true, true);
        JComponent content = editReviewForm.getContent();
        showBalloon(editor, point, content, editor.getContentComponent(), "Add Comment", review);
        editReviewForm.requestFocus();
    }

    private void showBalloon(final Editor editor, final Point point, JComponent balloonContent, final JComponent contentComponent, String title, Review review) {
        if(!review.isValid()) return;
        disposeActiveBalloon();
        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, title);
        balloonBuilder.setHideOnClickOutside(true);
        balloonBuilder.setHideOnKeyOutside(true);
        setActiveBalloon(new BalloonWithSelection(review, balloonBuilder.createBalloon(), editor));
        if(getActiveBalloon() == null) return;
        getActiveBalloon().getBalloon().show(new ReviewPositionTracker(editor, contentComponent, point) , Balloon.Position.atRight);
    }

    public void disposeActiveBalloon() {
        if(activeBalloon != null)
            activeBalloon.dispose();
    }

     public void showExistingComments(final Editor editor, Review review) {
        ReviewPoint reviewPoint = ReviewPointManager.getInstance(review.getProject()).findReviewPoint(review);
        if(reviewPoint == null) return;
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            final Icon icon = reviewPoint.getGutterIconRenderer().getIcon();
            editReviewForm = new EditReviewForm(reviewPoint.getReview(), false, true);
            JComponent content = editReviewForm.getContent();
            final Point centerIconPoint = new Point(point.x + icon.getIconWidth() / 2 + gutterComponent.getIconsAreaWidth(), point.y + icon.getIconHeight() / 2);
            showBalloon(editor, centerIconPoint, content, gutterComponent, "Edit Comment", review);
            editReviewForm.requestFocus();
        }
    }

    private BalloonWithSelection getActiveBalloon() {
        return activeBalloon;
    }

    private void setActiveBalloon(final BalloonWithSelection activeBalloon) {
        this.activeBalloon = activeBalloon;
    }

    public AnAction getGutterAction(final Review review) {
        if(activeBalloon == null || !activeBalloon.isValid() || !review.isActivated()) {
            List<ReviewItem> reviewItems = review.getReviewItems();
            if(!reviewItems.get(reviewItems.size() - 1).getAuthor().equals(System.getProperty("user.name"))) {
                return new AddReviewItemAction("Add review");
            }
            else {
                return new ShowReviewAction("View review");
            }
        } else {
            return new AnAction() {
                @Override
                public void actionPerformed(AnActionEvent e) {
                   disposeActiveBalloon();
                }
            };
        }
    }


    private class ReviewPositionTracker extends PositionTracker<Balloon>{

        private final Editor editor;
        private final JComponent component;
        private final Point point;

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
                        //disposeActiveBalloon();
                    }
                });
                 if(!object.isDisposed()) {
                    final PositionTracker<Balloon> tracker = this;
                    final VisibleAreaListener listener = new VisibleAreaListener() {
                       @Override
                       public void visibleAreaChanged(VisibleAreaEvent e) {
                            if(e.getNewRectangle().contains(point) && object.isDisposed()) {
                                /*disposeActiveBalloon();
                                setActiveBalloon(
                                        new BalloonWithSelection(
                                                balloonBuilder.createBalloon()
                                                getActiveBalloon().getHighlighter()));*/
                                getActiveBalloon().getBalloon().show(tracker, Balloon.Position.atRight);
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
