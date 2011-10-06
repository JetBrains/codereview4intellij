package ui.forms;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: Alisa.Afonina
 * Date: 9/29/11
 * Time: 11:54 AM
 */
public class SaveReviewsFormWrapper extends DialogWrapper{
    private SaveReviewsForm saveReviewsForm;

    public SaveReviewsFormWrapper(Project project) {
        super(project);
        saveReviewsForm = new SaveReviewsForm(project);
        setTitle("Export Reviews");
        init();
        this.setOKActionEnabled(false);
        saveReviewsForm.setKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                setOKActionEnabled(!"".equals(((JTextField)e.getComponent()).getText()));
            }
        });
    }

    @Override
    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return saveReviewsForm.getPreferredFocusedComponent();
    }

    @Override
    protected JComponent createCenterPanel() {
        return saveReviewsForm.getContents();
    }

    @Override
    protected void doOKAction() {
        if(isOKActionEnabled()) {
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
    }

    public VirtualFile getFile() {
        return saveReviewsForm.getFile();
    }

    public boolean isXMLFormat() {
        return saveReviewsForm.isXMLFormat();
    }
}
