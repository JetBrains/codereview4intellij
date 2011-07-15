package ui.reviewtoolwindow;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 8:12 PM
 */
public class CodeReviewToolWindowFactory implements ToolWindowFactory {

    /*private JButton hideToolWindowButton = new JButton();
    private JLabel commentLabel = new JLabel("Enter comment");
    private JTextField addReview = new JTextField();
    private JPanel myToolWindowContent = new JPanel(new GridLayout(0,1));
    private JButton add = new JButton("Add");
    private ToolWindow codeReviewToolWindow;


*/
    public CodeReviewToolWindowFactory() {
    }

    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        reviewView.initToolWindow(project, toolWindow);
    }
}

