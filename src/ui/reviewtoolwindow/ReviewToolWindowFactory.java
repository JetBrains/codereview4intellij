package ui.reviewtoolwindow;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 8:12 PM
 */
public class ReviewToolWindowFactory implements ToolWindowFactory, DumbAware{
    public void createToolWindowContent(Project project, ToolWindow toolWindow) {
        ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        reviewView.initToolWindow(project, toolWindow);
    }
}

