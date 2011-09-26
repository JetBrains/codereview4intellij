package ui.forms;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diff.impl.util.LabeledEditor;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileTextField;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.IOException;

/**
 * User: Alisa.Afonina
 * Date: 8/22/11
 * Time: 3:07 PM
 */
public class ReviewSaveForm extends DialogWrapper{
    private JTextField fileName = new JTextField();
    private FileTextField field;
    private Project project;


    public ReviewSaveForm(Project project) {
        super(project);
        this.setTitle("Export Reviews To File");
        this.project = project;
        init();
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new GridLayout(0, 2, 5, 10));
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ButtonGroup formatGroup = new ButtonGroup();
        final JRadioButton xmlButton = new JRadioButton("XML");
        formatGroup.add(xmlButton);
        JRadioButton htmlButton = new JRadioButton("HTML");
        formatGroup.add(htmlButton);
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

        field = FileChooserFactory.getInstance().createFileTextField(FileChooserDescriptorFactory.createSingleFolderDescriptor(), myDisposable);
        TextFieldWithBrowseButton targetFolderTextField = new TextFieldWithBrowseButton(field.getField());
        targetFolderTextField.addBrowseFolderListener("Save Reviews",
                                                 "Save reviews into file",
                                                 project, FileChooserDescriptorFactory.createSingleFolderDescriptor());
        final VirtualFile selectedFile = field.getSelectedFile();
        if(selectedFile != null) {
            targetFolderTextField.setText(selectedFile.getPath());
        }

        fileName.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setExtension(xmlButton.isSelected());
            }
        });
        fileName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setExtension(xmlButton.isSelected());
            }
        });

        JPanel fileNamePanel = new JPanel(new BorderLayout(5, 10));
        JPanel folderPanel = new JPanel(new BorderLayout(5, 10));
        //JPanel wrapperFileNamePanel = new JPanel(new GridLayout(0, 1));

        //wrapperFileNamePanel.add(fileNamePanel);
        mainPanel.add(new JLabel("Select output format: "));
        buttonPanel.add(xmlButton);
        buttonPanel.add(htmlButton);
        mainPanel.add(buttonPanel);

        mainPanel.add(new JLabel("File:"));
        mainPanel.add(fileName);

        mainPanel.add(new JLabel("Target folder:"));
        mainPanel.add(targetFolderTextField);
        //LabeledComponent<JTextField> fileComponent = new LabeledComponent<JTextField>();
        //fileComponent.setComponent(fileName);
        //fileComponent.setLabelLocation(BorderLayout.WEST);
        //fileComponent.setText("File name:");
        //mainPanel.add(fileComponent);
        //mainPanel.add(wrapperFileNamePanel);
        //LabeledComponent<TextFieldWithBrowseButton> folderComponent = new LabeledComponent<TextFieldWithBrowseButton>();
        //folderComponent.setText("Target folder ");
        //folderComponent.setLabelLocation(BorderLayout.WEST);
        //folderComponent.setComponent(targetFolderTextField);
        //mainPanel.add(folderComponent);

        return mainPanel;
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

    private boolean fileExists() {
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

    @Override
    protected void doOKAction() {
        if(fileExists()) {
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
