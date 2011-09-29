package ui.forms;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import ui.forms.SaveReviewsForm;

import javax.swing.*;

/**
 * User: Alisa.Afonina
 * Date: 9/29/11
 * Time: 11:54 AM
 */
public class SaveReviewsFormWrapper extends DialogWrapper{
    private Project project;
    private SaveReviewsForm saveReviewsForm;

    public SaveReviewsFormWrapper(Project project) {
        super(project);
        this.project = project;
        saveReviewsForm = new SaveReviewsForm(project);
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        return saveReviewsForm.getContents();
    }

    @Override
    protected void doOKAction() {
        if(saveReviewsForm.fileExists()) {
            if(Messages.showOkCancelDialog("This file already exists." +
                    " Would you like to overwrite it?",
                    "File Already Exists",
                    Messages.getWarningIcon()) == Messages.OK) {
                super.doOKAction();
            }
        } else {
            super.doOKAction();
        }
    }

    public VirtualFile getFile() {
        return saveReviewsForm.getFile();
    }

    public boolean isXMLFormat() {
        return saveReviewsForm.isXMLFormat();
    }
}
