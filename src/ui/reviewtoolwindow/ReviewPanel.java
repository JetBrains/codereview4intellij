package ui.reviewtoolwindow;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vcs.update.AbstractTreeNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.file.PsiDirectoryFactory;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import org.jetbrains.annotations.NonNls;
import reviewresult.Review;
import ui.reviewtoolwindow.nodes.DirectoryNode;
import ui.reviewtoolwindow.nodes.FileNode;
import reviewresult.ReviewManager;
import ui.reviewtoolwindow.nodes.ReviewItemNode;
import ui.reviewtoolwindow.nodes.ReviewNode;

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
public class ReviewPanel extends  SimpleToolWindowPanel implements DataProvider/*, OccurenceNavigator*/ {
    public static final String ACTION_GROUP = "TreeReviewItemActions";
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
        EditSourceOnDoubleClickHandler.install(reviewTree);
        PopupHandler.installPopupHandler(reviewTree, ACTION_GROUP, ActionPlaces.TODO_VIEW_POPUP);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
        setContent(scrollPane);
    }

    private AbstractTreeStructureBase createTreeStructure(ReviewManager reviewManager) {
        project = reviewManager.getProject();
        VirtualFile virtualFile = project.getBaseDir();
        DirectoryNode rootNode = new DirectoryNode(project, virtualFile);
        return new ReviewTreeStructure(project, rootNode);
    }


    @Override
    public Object getData(@NonNls String dataId) {
        if (PlatformDataKeys.NAVIGATABLE.is(dataId)) {
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
            if (/*!(element instanceof FileNode || */!(element instanceof ReviewNode)) {
                return null;
            }
            Review review = ((ReviewNode) element).getReview();
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(review.getUrl());
            return new OpenFileDescriptor(project,virtualFile, review.getStart());
        }
     return null;
    }
/*
    public boolean hasReviewOccurence(Object node, boolean next) {
        if(node instanceof ReviewNode) {
            FileNode file = (FileNode) ((ReviewNode) node).getParent();
            int index = ((ReviewNode) node).getIndex();
            boolean result = (file.getChildren().size() > index) ? next : !next;
            return hasReviewOccurence(file, next) || result;
        }
        if(node instanceof ReviewItemNode) {
            return hasReviewOccurence(((ReviewItemNode) node).getParent(), next);
        }
        if(node instanceof FileNode){
            if
        }

    }

    @Override
    public boolean hasNextOccurence() {
        TreePath selectionPath = reviewTree.getSelectionPath();
        Object node = selectionPath.getLastPathComponent();
        hasReviewOccurence(node, true);

    }

    @Override
    public boolean hasPreviousOccurence() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OccurenceInfo goNextOccurence() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OccurenceInfo goPreviousOccurence() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getNextOccurenceActionName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getPreviousOccurenceActionName() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }*/
}