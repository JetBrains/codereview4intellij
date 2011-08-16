package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;
import ui.reviewtoolwindow.ReviewView;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:06 PM
 */
public class AddReviewAction extends AnAction implements DumbAware{

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
        ReviewManager instance = ReviewManager.getInstance(project);
        VirtualFile baseDir = project.getBaseDir();
        if(baseDir == null) {return;}
        Review oldReview = instance.getReviewInLine(VfsUtil.getRelativePath(virtualFile, baseDir, '/'), line);
        if(oldReview != null){
            ReviewView.showTwoCommentsOnOnewLineMessage(oldReview);
            return;
        }
        Review review = new Review(project, null, start, end, virtualFile);

        if(review.isValid()) {
            ReviewPoint reviewPoint = ReviewPointManager.getInstance(project).findReviewPoint(review);
            if(reviewPoint != null) {
                ReviewActionManager.getInstance(reviewPoint.getReview()).addToExistingComments(editor);
            } else {
                 ReviewActionManager.getInstance(review).addNewComment(editor);
            }
        }
        else {
            instance.logInvalidReview(review);
        }
    }




}
