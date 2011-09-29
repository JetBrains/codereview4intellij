package ui.forms;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;
import utils.ReviewsBundle;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * User: Alisa.Afonina
 * Date: 9/29/11
 * Time: 11:22 AM
 */
public class SaveReviewsForm {
    private JRadioButton htmlButton;
    private JRadioButton xmlButton;
    private JTextField fileName;
    private TextFieldWithBrowseButton folderField;
    private JPanel mainPanel;
    private JLabel myFolderLabel;

    private Project project;
    private FileTextField field;

    public SaveReviewsForm(Project project) {
        this.project = project;
        myFolderLabel.setLabelFor(folderField.getChildComponent());
        xmlButton.setSelected(true);

        xmlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFileExtension(".html", ".xml");
            }
        });

        htmlButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeFileExtension(".xml", ".html");
            }
        });


        fileName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                 setExtension(xmlButton.isSelected());
            }
        });

        fileName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setExtension(xmlButton.isSelected());
            }
        });

    }

private void setExtension(boolean isXML) {
        String text = fileName.getText();
        if(!"".equals(text)) {
            if(!text.contains(".xml") && !text.contains(".html")) {
                String extension = isXML ? ".xml":".html";
                fileName.setText(text + extension);
            }
        }
    }

    public boolean fileExists() {
        String text = fileName.getText();
        if(text != null && !"".equals(text)) {
            final VirtualFile selectedFile = field.getSelectedFile();
            if(selectedFile != null) {
                final VirtualFile child = selectedFile.findChild(text);
                if(child != null)
                    return true;
            }
        }
        return false;
    }

    private void changeFileExtension(String oldExt, String newExt) {
        final String text = fileName.getText();
        if(!"".equals(text)) {
            if(text.contains(oldExt)) {
                fileName.setText(text.replace(oldExt, newExt));
            } else {
                if(!text.contains(newExt)) {
                    fileName.setText(text + newExt);
                }
            }
        }
    }

    @Nullable
    public VirtualFile getFile() {
        final VirtualFile selectedFile = field.getSelectedFile();
        final String text = fileName.getText();
        if(text != null && !"".equals(text)) {
            if(selectedFile != null) {
                    return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
                        public VirtualFile compute() {
                            try {
                                return selectedFile.createChildData(this, text);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    });
            } else {
                final VirtualFile baseDir = project.getBaseDir();
                if(baseDir != null) {
                    return ApplicationManager.getApplication().runWriteAction(new Computable<VirtualFile>() {
                        public VirtualFile compute() {
                            try {
                                return baseDir.findOrCreateChildData(this, text);
                            } catch (IOException e) {
                                return null;
                            }
                        }
                    });

                }
            }
        }
        return null;
    }

    public boolean isXMLFormat() {
        return fileName.getText().contains(".xml");
    }

    private void createUIComponents() {
        field = FileChooserFactory.getInstance().createFileTextField(FileChooserDescriptorFactory.createSingleFolderDescriptor(), project);
        folderField = new TextFieldWithBrowseButton(field.getField());
        folderField.addBrowseFolderListener(ReviewsBundle.message("reviews.saveReviews"),
                ReviewsBundle.message("reviews.saveReviewIntoFile"),
                project, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        final VirtualFile selectedFile = field.getSelectedFile();
        if(selectedFile != null) {
            folderField.setText(selectedFile.getPresentableUrl());
        }
    }

    public JComponent getContents() {
        return mainPanel;
    }

    public JComponent getPreferredFocusedComponent() {
        return fileName;
    }

    public void setKeyListener(KeyAdapter keyAdapter) {
        fileName.addKeyListener(keyAdapter);
    }
}
