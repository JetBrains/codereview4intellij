package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
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
        int line = document.getLineNumber(offset);
        int start = document.getLineStartOffset(line);
        int end = document.getLineEndOffset(line);
        CharSequence text = document.getCharsSequence().subSequence(start, end);

        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;

        final VirtualFile virtualFile = psiFile.getVirtualFile();
        if (virtualFile == null) return;
        reviewManager.addReview(text.toString(), ReviewStatus.COMMENT, virtualFile, start, end);

    }
}
