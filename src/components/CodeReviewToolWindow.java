package components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import reviewresult.ReviewManager;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 8:12 PM
 */
public class CodeReviewToolWindow implements ToolWindowFactory {

    private JButton hideToolWindowButton = new JButton();
    private JLabel commentLabel = new JLabel("Enter comment");
    private JTextField addReview = new JTextField();
    private JPanel myToolWindowContent = new JPanel(new GridLayout(0,1));
    private JButton add = new JButton("Add");
    private ToolWindow codeReviewToolWindow;
    private ReviewManager reviewManager;


    public CodeReviewToolWindow() {
        myToolWindowContent.add(commentLabel);
        myToolWindowContent.add(addReview);
       /* myToolWindowContent.add(add);
        add.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reviewManager.addReview(addReview.getText(), ReviewStatus.COMMENT);
            }
        });*/
    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        codeReviewToolWindow = toolWindow;
        //commentLabel.setText("Enter comment");
        reviewManager = ReviewManager.getInstance(project);
        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(myToolWindowContent, "", false);
        toolWindow.getContentManager().addContent(content);

    }

}

