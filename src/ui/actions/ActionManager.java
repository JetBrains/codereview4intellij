package ui.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import reviewresult.Review;
import reviewresult.ReviewManager;

/**
 * User: Alisa.Afonina
 * Date: 8/16/11
 * Time: 3:42 PM
 */
public class ActionManager implements DumbAware {
    private static ActionManager instance;

    private ActionManager() {}

    public static ActionManager getInstance() {
        if(instance == null) {
            instance = new ActionManager();
        }
        return instance;
    }

    public Review getReviewForAction(AnActionEvent e) {
        Project project = e.getProject();
        if(project == null) return null;
        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if(editor != null) {
            Document document = editor.getDocument();
            int offset = editor.getCaretModel().getOffset();
            int line = document.getLineNumber(offset);

            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            VirtualFile baseDir = project.getBaseDir();
            if(baseDir == null)  {return null;}
            String relativePath = VfsUtil.getRelativePath(virtualFile, baseDir, '/');
            return ReviewManager.getInstance(project).getReviewInLine(relativePath, line);
        }
        return null;
    }
}
