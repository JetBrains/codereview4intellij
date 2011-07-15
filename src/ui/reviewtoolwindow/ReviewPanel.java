package ui.reviewtoolwindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.psi.PsiFile;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.OpenSourceUtil;
import ui.reviewtoolwindow.nodes.DirectoryNode;
import ui.reviewtoolwindow.nodes.FileNode;
import reviewresult.ReviewManager;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 2:39 PM
 */
public class ReviewPanel extends /*JPanel*/ SimpleToolWindowPanel /*implements DataProvider*/ {

    private Tree reviewTree;
    private Project project;

    public ReviewPanel(ReviewManager reviewManager) {
        super(false);
        this.setLayout(new GridLayout(0,1));

        AbstractTreeStructureBase reviewTreeStructure = createTreeStructure(reviewManager);
        DefaultMutableTreeNode root = new DefaultMutableTreeNode();
        final DefaultTreeModel model = new DefaultTreeModel(root);
        reviewTree = new Tree(model);
        AbstractTreeBuilder reviewTreeBuilder = new /*ReviewTreeBuilder*/ AbstractTreeBuilder(reviewTree, model, reviewTreeStructure, null);
        reviewTree.invalidate();
        new TreeSpeedSearch(reviewTree);
        reviewTree.addKeyListener(
        new KeyAdapter() {
            public void keyPressed(KeyEvent e) {

                if (!e.isConsumed() && KeyEvent.VK_ENTER == e.getKeyCode()) {
                    TreePath path = reviewTree.getSelectionPath();
                    System.out.println("key pressed 1");
                    if (path == null) {
                        return;
                }
                System.out.println("key pressed 2");
                NodeDescriptor descriptor = (NodeDescriptor)((DefaultMutableTreeNode)path.getLastPathComponent()).getUserObject();
                /*if (!(descriptor instanceof ReviewItemNode)) {
                    return;
                }*/
                OpenSourceUtil.openSourcesFrom(ReviewPanel.this, false);
                }
            }
        }
        );
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
        setContent(scrollPane);
    }

    private AbstractTreeStructureBase createTreeStructure(ReviewManager reviewManager) {
        project = reviewManager.getProject();
        DirectoryNode rootNode = new DirectoryNode(project);
        for (PsiFile file : reviewManager.getFiles()) {
            rootNode.addChild(new FileNode(project, file));
        }
        return new ReviewTreeStructure(project, rootNode);
    }

/*
    @Override
    public Object getData(@NonNls String dataId) {

        if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
            System.out.println("here");
            TreePath path = reviewTree.getSelectionPath();
            if (path == null) {
                return null;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
            NodeDescriptor userObject = (NodeDescriptor)node.getUserObject();
            if (userObject == null) {
            return null;
            }
            Object element = userObject.getElement();
            if (*/
/*!(element instanceof FileNode || *//*
element instanceof ReviewNode) {
                return null;
            }
            Review review = ((ReviewNode) element).getReview();
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(review.getUrl());
            return new OpenFileDescriptor(project,virtualFile,
            review.getStart());
        }
     return null;
    }
*/
}