package components;

import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import components.reviewpoint.ReviewPoint;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;

/**
 * User: Alisa.Afonina
 * Date: 7/12/11
 * Time: 4:06 PM
 */
public class AddReviewAction extends AnAction {
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        ReviewManager reviewManager = ReviewManager.getInstance(project);

        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        int offset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        int lineNumber = document.getLineNumber(offset);
        int start = document.getLineStartOffset(lineNumber);
        int end = document.getLineEndOffset(lineNumber);
        CharSequence text = document.getCharsSequence().subSequence(start, end);

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;

        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return;

        reviewManager.addReview(text, ReviewStatus.COMMENT, new ReviewPoint(project, virtualFile, lineNumber));

    }
}
