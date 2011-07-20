package ui.reviewtoolwindow;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.DataProvider;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.reviewtoolwindow.nodes.DirectoryNode;
import ui.reviewtoolwindow.nodes.FileNode;
import ui.reviewtoolwindow.nodes.ReviewItemNode;
import ui.reviewtoolwindow.nodes.ReviewNode;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 2:39 PM
 */
public class ReviewPanel extends  SimpleToolWindowPanel implements DataProvider, OccurenceNavigator {
    public static final String ACTION_GROUP = "TreeReviewItemActions";
    private Tree reviewTree;
    private Project project;

    public ReviewPanel(ReviewManager reviewManager) {
        super(false);
        AbstractTreeStructureBase reviewTreeStructure = createTreeStructure(reviewManager);
        final DefaultTreeModel model = new DefaultTreeModel(new DefaultMutableTreeNode());
        reviewTree = new Tree(model);
        AbstractTreeBuilder reviewTreeBuilder = new AbstractTreeBuilder(reviewTree, model, reviewTreeStructure, null);
        reviewTree.invalidate();
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
            if (!(element instanceof ReviewNode)) {
                return null;
            }
            Review review = ((ReviewNode) element).getReview();
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(review.getUrl());
            return new OpenFileDescriptor(project,virtualFile, review.getStart());
        }
     return null;
    }

    @Override
    public boolean hasNextOccurence() {
        TreePath selectionPath = reviewTree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();

        Object userObject = node.getUserObject();
        int childrenSize = node.getChildCount();
        int rowCount = reviewTree.getRowCount();
        int rowForPath = reviewTree.getRowForPath(selectionPath);

        if (userObject instanceof NodeDescriptor) {
            Object element = ((NodeDescriptor) userObject).getElement();

            if (element instanceof ReviewItemNode) {
                TreePath parentPath = selectionPath.getParentPath();
                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
                int rowForParentPath = reviewTree.getRowForPath(parentPath);
                return rowCount > rowForParentPath + parent.getChildCount();
            }
            if(element instanceof ReviewNode) {
                return rowCount > rowForPath + childrenSize + 1;
            }

            else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasPreviousOccurence() {
        TreePath selectionPath = reviewTree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        Object userObject = node.getUserObject();
        return userObject instanceof NodeDescriptor && !isFirst(node);
    }

    private boolean isFirst(final TreeNode node) {
      final TreeNode parent = node.getParent();
      return parent == null || parent.getIndex(node) == 0 && isFirst(parent);
    }

    @Override
    public OccurenceInfo goNextOccurence() {
         TreePath selectionPath = reviewTree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        return goOccurence(getNextReview(node));
    }

    private DefaultMutableTreeNode getNextReview(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof NodeDescriptor) {
            Object element = ((NodeDescriptor) userObject).getElement();

            if(element instanceof FileNode){
                    return (DefaultMutableTreeNode) node.getFirstChild();
            }

            if(element instanceof DirectoryNode) {
                        return getNextReview((DefaultMutableTreeNode)node.getFirstChild());
            }
            if(element instanceof ReviewItemNode) {
                return getNextReview((DefaultMutableTreeNode) node.getParent());
            }

            if(element instanceof ReviewNode) {
                DefaultMutableTreeNode sibling = node.getNextSibling();

                if(sibling != null) {
                    Object siblingNodeDescriptor = sibling.getUserObject();

                    if(siblingNodeDescriptor instanceof NodeDescriptor) {
                        if(((NodeDescriptor) siblingNodeDescriptor).getElement() instanceof ReviewNode) {
                            return sibling;
                        } else {
                            return getNextReview(sibling);
                        }
                    }
                }
                else {
                    DefaultMutableTreeNode nextNode = node.getLastLeaf().getNextNode();
                    return getNextReview(nextNode);
                }

            }
        }
        return null;
    }

    private DefaultMutableTreeNode getPrevReview(DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof NodeDescriptor) {
            Object element = ((NodeDescriptor) userObject).getElement();
            DefaultMutableTreeNode sibling = node.getPreviousSibling();
            if(sibling != null) {
                Object siblingNodeDescriptor = sibling.getUserObject();
                if(element instanceof FileNode){
                    if(siblingNodeDescriptor instanceof NodeDescriptor) {
                        if(((NodeDescriptor) siblingNodeDescriptor).getElement() instanceof FileNode) {
                            return (DefaultMutableTreeNode) sibling.getLastChild();
                        } else {
                            return getPrevReview(sibling);
                        }
                    }
                }

                if(element instanceof DirectoryNode) {
                    if(siblingNodeDescriptor instanceof NodeDescriptor) {
                            return (DefaultMutableTreeNode) sibling.getLastLeaf().getParent();
                    }
                }

                if(element instanceof ReviewNode) {
                    if(siblingNodeDescriptor instanceof NodeDescriptor) {
                        if(((NodeDescriptor) siblingNodeDescriptor).getElement() instanceof ReviewNode) {
                            return sibling;
                        } else {
                            return getPrevReview(sibling);
                        }
                    }
                }
            } else {
                DefaultMutableTreeNode prevNode = (DefaultMutableTreeNode) node.getParent();
                return getPrevReview(prevNode);
            }

            if(element instanceof ReviewItemNode) {
                return getPrevReview((DefaultMutableTreeNode) node.getParent());
            }


        }
        return null;
    }

    @Override
    public OccurenceInfo goPreviousOccurence() {
        TreePath selectionPath = reviewTree.getSelectionPath();
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
        return goOccurence(getPrevReview(node));
    }

    private OccurenceInfo goOccurence( DefaultMutableTreeNode reviewNodeDescriptor) {
        Object reviewNode = reviewNodeDescriptor.getUserObject();
        if(reviewNode instanceof ReviewNode) {
            TreeUtil.selectNode(reviewTree, reviewNodeDescriptor);
            Review review = ((ReviewNode) reviewNode).getReview();
            return new OccurenceInfo(review.getElement(), -1, -1);
        }
        return null;
    }

    @Override
    public String getNextOccurenceActionName() {
        return IdeActions.ACTION_NEXT_OCCURENCE ;
    }

    @Override
    public String getPreviousOccurenceActionName() {
        return IdeActions.ACTION_PREVIOUS_OCCURENCE;

    }
}