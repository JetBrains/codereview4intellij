package ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 1:41 PM
 */
public class DeleteReviewAction extends AnAction  implements DumbAware {
    private static final Icon ICON = IconLoader.getIcon("/images/note_delete.png");

    public DeleteReviewAction() {
    }

    public DeleteReviewAction(String title) {
        super(title, title, ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getData(PlatformDataKeys.PROJECT);
        if(project == null) return;
        //ReviewManager instance = ;
        Review review = ActionManager.getInstance().getReviewForAction(e);

        if(review == null) {
            review = e.getData(Review.REVIEW_DATA_KEY);
        }

        if(review != null) {
            if(Messages.showYesNoDialog(project, "Are you sure you want to delete this review?",
                                                "Delete Review", Messages.getQuestionIcon()) == Messages.YES) {
            ReviewManager.getInstance(project).removeReview(review);
            }
        } else {
            VirtualFile file = PlatformDataKeys.VIRTUAL_FILE.getData(e.getDataContext());
            if(file != null) {
                String fileType = "file";
                if(file.isDirectory())
                    fileType = "folder";
                if(Messages.showOkCancelDialog(project, "Are you sure you want to delete all reviews in this " + fileType + " ?",
                                                        "Delete Reviews", null) == Messages.OK) {

                    ReviewManager.getInstance(project).removeAll(file);
                }
            }
        }
    }
}
