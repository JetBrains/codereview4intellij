package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.awt.RelativePoint;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.forms.EditReviewForm;
import ui.reviewpoint.ReviewPoint;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:06 PM
 */
public class AddReviewAction extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if(project == null) return;

        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null) return;

        Document document = editor.getDocument();

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;

        PsiElement context = psiFile.getContext();
        final VirtualFile virtualFile = (context == null) ? psiFile.getVirtualFile() : context.getContainingFile().getVirtualFile();
        if (virtualFile == null) return;

        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        int line = document.getLineNumber(offset);
        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);

        Review review = new Review(project, null, start, end, virtualFile);
        ReviewManager instance = ReviewManager.getInstance(project);
        if(review.isValid()) {
            ReviewPoint reviewPoint = instance.findReviewPoint(review);
            if(reviewPoint != null) {
                ReviewActionManager.getInstance(reviewPoint.getReview()).addToExistingComments(editor, reviewPoint);
            } else {
                 ReviewActionManager.getInstance(review).addNewComment(editor);
            }
        }
        else {
            instance.logInvalidReview(review);
        }
    }




}
