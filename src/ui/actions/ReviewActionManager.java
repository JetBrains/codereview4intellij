package ui.actions;


import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.DumbAware;
import reviewresult.Review;
import ui.forms.EditReviewForm;
import ui.gutterpoint.BalloonWithSelection;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:14 PM
 */
public class ReviewActionManager implements DumbAware {
    private BalloonWithSelection activeBalloon = new BalloonWithSelection();
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
            final Review review = reviewPoint.getReview();
            editReviewForm = new EditReviewForm(review, true, true);
            JComponent content = editReviewForm.getContent();
            Point docPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(review.getEnd()));
            final Point balloonPoint = new Point(docPoint.x, docPoint.y + editor.getLineHeight());
            activeBalloon.dispose();
            activeBalloon = new BalloonWithSelection(review, editor, balloonPoint, content, "Add Comment");
            activeBalloon.showBalloon(editor.getContentComponent());
            editReviewForm.requestFocus();
        }
    }

    public void disposeActiveBalloon() {
        activeBalloon.dispose();
    }

    public void addNewComment(final Editor editor, Review review) {
        Point point = editor.visualPositionToXY(editor.offsetToVisualPosition(review.getEnd()));
        point = new Point(point.x, point.y + editor.getLineHeight());
        editReviewForm = new EditReviewForm(review, true, true);
        JComponent content = editReviewForm.getContent();
        activeBalloon.dispose();
        activeBalloon = new BalloonWithSelection(review, editor, point, content, "Add Comment");
        activeBalloon.showBalloon(editor.getContentComponent());
        editReviewForm.requestFocus();
    }

     public void showExistingComments(final Editor editor, Review review) {
        ReviewPoint reviewPoint = ReviewPointManager.getInstance(review.getProject()).findReviewPoint(review);
        if(reviewPoint == null) return;
        final EditorGutterComponentEx gutterComponent = ((EditorEx)editor).getGutterComponentEx();
        final Point point = gutterComponent.getPoint(reviewPoint.getGutterIconRenderer());
        if (point != null) {
            editReviewForm = new EditReviewForm(reviewPoint.getReview(), false, true);
            JComponent content = editReviewForm.getContent();
            Point docPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(review.getEnd()));
            final Point balloonPoint = new Point(docPoint.x, docPoint.y + editor.getLineHeight());
            activeBalloon.dispose();
            activeBalloon = new BalloonWithSelection(review, editor, balloonPoint, content, "Review");
            activeBalloon.showBalloon(editor.getContentComponent());
            editReviewForm.requestFocus();
        }
    }


    public BalloonWithSelection getActiveBalloon() {
        return activeBalloon;
    }
}
