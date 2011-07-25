package ui.actions;

import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import reviewresult.Review;
import sun.reflect.generics.tree.Tree;
import ui.reviewtoolwindow.ReviewView;
import ui.reviewtoolwindow.nodes.ReviewNode;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

/**
 * User: Alisa.Afonina
 * Date: 7/25/11
 * Time: 4:24 PM
 */
public class PreviewAction extends ToggleAction {
    private static final Icon PREVIEW_ICON = IconLoader.getIcon("/actions/preview.png");
    private boolean state = false;

    public PreviewAction() {
        super("Preview reviews", null, PREVIEW_ICON);
    }

    public PreviewAction(final SimpleTree reviewTree) {
        super("Preview reviews", null, PREVIEW_ICON);
        SimpleTree reviewTree1 = reviewTree;
        if(reviewTree == null) return;
        reviewTree.addTreeSelectionListener(new TreeSelectionListener() {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            TreePath path = reviewTree.getSelectionPath();
            if (path == null) return;
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();

            NodeDescriptor userObject = (NodeDescriptor)node.getUserObject();
            if (userObject == null) return ;

            Object element = userObject.getElement();
            if (!(element instanceof ReviewNode)) return;

            Review review = ((ReviewNode) element).getReview();
            ReviewView reviewView = ServiceManager.getService(review.getProject(), ReviewView.class);
            }
        });
    }

    @Override
    public boolean isSelected(AnActionEvent e) {

        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        this.state = state;
    }
}
