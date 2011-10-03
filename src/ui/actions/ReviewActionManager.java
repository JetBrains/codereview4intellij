package ui.actions;


import utils.ReviewsBundle;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewManager;
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
            editReviewForm = new EditReviewForm(review, false, true, true);
            JComponent content = editReviewForm.getContent();
            Point docPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(review.getEnd()));
            final Point balloonPoint = new Point(docPoint.x, docPoint.y + editor.getLineHeight());
            activeBalloon.dispose();
            activeBalloon = new BalloonWithSelection(review, editor, balloonPoint, content,
                                                     ReviewsBundle.message("reviews.addComment"));
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
        editReviewForm = new EditReviewForm(review, true, false, true);
        JComponent content = editReviewForm.getContent();
        activeBalloon.dispose();
        activeBalloon = new BalloonWithSelection(review, editor, point, content,
                                                 ReviewsBundle.message("reviews.addComment"));
        activeBalloon.showBalloon(editor.getContentComponent());
        editReviewForm.requestFocus();
    }

     public void showExistingComments(final Editor editor, Review review) {
        ReviewPoint reviewPoint = ReviewPointManager.getInstance(review.getProject()).findReviewPoint(review);
        if(reviewPoint == null) return;
        editReviewForm = new EditReviewForm(reviewPoint.getReview(), false, false, true);
        JComponent content = editReviewForm.getContent();
        Point docPoint = editor.visualPositionToXY(editor.offsetToVisualPosition(review.getEnd()));
        final Point balloonPoint = new Point(docPoint.x, docPoint.y + editor.getLineHeight());
        activeBalloon.dispose();
        activeBalloon = new BalloonWithSelection(review, editor, balloonPoint, content,
                                                 ReviewsBundle.message("reviews.review"));
        activeBalloon.showBalloon(editor.getContentComponent());
        editReviewForm.requestFocus();
    }


    public BalloonWithSelection getActiveBalloon() {
        return activeBalloon;
    }

    @Nullable
    public Review getReviewForAction(AnActionEvent e) {
        Project project = e.getProject();
        if(project == null) return null;
        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if(editor != null) {
            Document document = editor.getDocument();
            int offset;
            if(editor.getSelectionModel().hasSelection()) {
               offset  = editor.getSelectionModel().getSelectionStart();
            } else {
                offset = editor.getCaretModel().getOffset();
            }
            if(offset < 0) return null;
            int line = document.getLineNumber(offset);
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            VirtualFile baseDir = project.getBaseDir();
            if(baseDir == null)  {return null;}
            if(virtualFile == null) {return null;}
            String relativePath = VfsUtil.getRelativePath(virtualFile, baseDir, '/');
            return ReviewManager.getInstance(project).getReviewInLine(relativePath, line);
        }
        return null;
    }

    public AnAction getReviewPointClickAction(final Review review) {
        final BalloonWithSelection activeBalloon = getActiveBalloon();

            if(!activeBalloon.isValid() ||  !review.isActivated() || activeBalloon.isClosed()) {

                review.setActivated(true);
                if(review.isLastReviewItemMine()) {
                    return new ShowReviewAction(ReviewsBundle.message("reviews.viewReview"));
                } else {
                    return new AddReviewItemAction(ReviewsBundle.message("reviews.addReview"));
                }
            }

            if(activeBalloon.compare(review) && (activeBalloon.isClosed() || review.isActivated())) {
                return new AnAction() {
                    @Override
                    public void actionPerformed(AnActionEvent e) {
                        review.setActivated(false);
                        activeBalloon.dispose();
                    }
                };
            }
        return null;
    }
}
