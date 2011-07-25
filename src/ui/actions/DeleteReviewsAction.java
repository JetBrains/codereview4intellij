package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.CaretModel;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.reviewtoolwindow.ReviewView;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:41 PM
 */
public class DeleteReviewsAction extends AnAction{

    private Review review;

    public DeleteReviewsAction(String title, Review review) {
        super(title);
        this.review = review;
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
       /* Project project = e.getData(PlatformDataKeys.PROJECT);
        if(project == null) return;

        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null) return;

        Document document = editor.getDocument();

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;

        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return;

        CaretModel caretModel = editor.getCaretModel();
        int offset = caretModel.getOffset();
        int line = document.getLineNumber(offset);
        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);

        Review review = new Review(project, null, start, end, virtualFile);*/
        Project project = review.getProject();
        ReviewManager.getInstance(project).removeReview(review);
        ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        reviewView.updateUI();
    }
}
