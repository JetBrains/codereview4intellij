package ui.reviewtoolwindow;

import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.OccurenceNavigatorSupport;
import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.ide.macro.ModuleNameMacro;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SmartExpander;
import com.intellij.ui.treeStructure.*;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.forms.EditReviewForm;
import ui.reviewpoint.ReviewPoint;
import ui.reviewtoolwindow.nodes.FileNode;
import ui.reviewtoolwindow.nodes.ModuleNode;
import ui.reviewtoolwindow.nodes.ReviewNode;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.*;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 2:39 PM
 */
public class ReviewPanel extends  SimpleToolWindowPanel implements DataProvider, OccurenceNavigator, Disposable {
    public static final String ACTION_GROUP = "TreeReviewItemActions";

    private Project project;
    private JPanel mainPanel;
    private SimpleTree reviewTree;
    private AbstractTreeBuilder reviewTreeBuilder;

    private JScrollPane previewScrollPane;
    private JPanel previewPanel = new JPanel();

    private boolean isShowPreview;
    private boolean searchEnabled;

    @Nullable
    private OccurenceNavigatorSupport reviewNavigatorSupport;
    private JTextField searchLine = new JTextField();
    private SimpleTreeStructure reviewTreeStructure;
    private boolean groupByModule;
    private EditReviewForm editReviewForm;
    private boolean groupByFile;


    public ReviewPanel(final Project project) {
        super(false);
        this.project = project;
        ReviewManager.getInstance(project).createFilter("");
        createTreeStructure();
        final DefaultTreeModel model = new DefaultTreeModel(new PatchedDefaultMutableTreeNode());
        reviewTree = new SimpleTree(model);
        SmartExpander.installOn(reviewTree);
        reviewTreeBuilder = new SimpleTreeBuilder(reviewTree, model, reviewTreeStructure, null);
        TreeUtil.expandAll(reviewTree);
        reviewTree.revalidate();
        EditSourceOnDoubleClickHandler.install(reviewTree);
        reviewTree.setRootVisible(false);
        reviewTree.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                    SimpleNode node = reviewTree.getSelectedNode();

                    if (node instanceof Navigatable)
                        ((Navigatable) node).navigate(true);
                }
            }
        });
        reviewTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                SimpleNode node = reviewTree.getSelectedNode();
                showPreview(node);
            }
        });
        reviewNavigatorSupport = new OccurenceNavigatorSupport(reviewTree) {
        protected Navigatable createDescriptorForNode(DefaultMutableTreeNode node) {
          if (node.getChildCount() > 0)  {return null;}
          return getNavigatableForNode(node);
        }

          @Override
        public String getNextOccurenceActionName() {
            return IdeActions.ACTION_NEXT_OCCURENCE ;
        }

        @Override
        public String getPreviousOccurenceActionName() {
            return IdeActions.ACTION_PREVIOUS_OCCURENCE;

        }
      };
        PopupHandler.installPopupHandler(reviewTree, ACTION_GROUP, ActionPlaces.TODO_VIEW_POPUP);
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
        previewScrollPane = ScrollPaneFactory.createScrollPane(previewPanel);
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(createLeftMenu(), BorderLayout.WEST);
        previewScrollPane.setVisible(false);
        mainPanel.add(scrollPane);
        mainPanel.add(previewScrollPane, BorderLayout.EAST);
        searchLine.setVisible(searchEnabled);
        searchLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReviewManager.getInstance(project).createFilter(searchLine.getText());
                createTreeStructure();
                updateUI();
                editReviewForm.updateSelection();
            }
        });
        mainPanel.add(searchLine, BorderLayout.NORTH);
        setContent(mainPanel);
    }

    private JPanel createLeftMenu() {
        JPanel toolBar = new JPanel(new GridLayout());

        DefaultActionGroup leftGroup = new DefaultActionGroup();
        leftGroup.add(new PreviousOccurenceToolbarAction(this));
        leftGroup.add(new NextOccurenceToolbarAction(this));
        leftGroup.add(new PreviewAction());
        leftGroup.add(new GroupByModuleAction());
        leftGroup.add(new GroupByFileAction());
        leftGroup.add(new SearchAction());
        toolBar.add(
            ActionManager.getInstance().createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, leftGroup, false).getComponent());

    return toolBar;
    }

    public void createTreeStructure() {

        SimpleNode rootNode = new SimpleNode() {
            @Override
            public SimpleNode[] getChildren() {

                if(ReviewManager.getInstance(project).getFileNames().isEmpty()) {
                    return new SimpleNode[] {};
                }
                else {
                    List<SimpleNode> roots = new ArrayList<SimpleNode>();

                    if(groupByModule) {
                            for(Module module : ModuleManager.getInstance(project).getModules()) {
                                roots.add(new ModuleNode(project, module));
                            }
                        } else {
                            if(groupByFile) {
                                for(VirtualFile contentRoot : ProjectRootManager.getInstance(project).getContentRoots()) {
                                    roots.add(new FileNode(project, contentRoot));
                                }
                            } else {
                                for(String content : ReviewManager.getInstance(project).getFileNames()) {
                                    for(Review review : ReviewManager.getInstance(project).getFilteredReviews(content))
                                    roots.add(new ReviewNode(project, review));
                                }
                            }

                    }
                    return roots.toArray(new SimpleNode[roots.size()]);
                }
            }
        };
        reviewTreeStructure = new ReviewTreeStructure(project, rootNode);

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
                return element;
            }
            Review review = ((ReviewNode) element).getReview();
            return review.getElement();
        }
     return null;
    }

    @Nullable
    private static Navigatable getNavigatableForNode(@NotNull DefaultMutableTreeNode node) {
        Object userObject = node.getUserObject();
        if (userObject instanceof Navigatable) {
            final Navigatable navigatable = (Navigatable)userObject;
            return navigatable.canNavigate() ? navigatable : null;
        }
        return null;
    }

    public boolean hasNextOccurence() {
        return reviewNavigatorSupport != null && reviewNavigatorSupport.hasNextOccurence();
    }

    public boolean hasPreviousOccurence() {
      return reviewNavigatorSupport != null && reviewNavigatorSupport.hasPreviousOccurence();
    }

    public OccurenceInfo goNextOccurence() {
      return reviewNavigatorSupport != null ? reviewNavigatorSupport.goNextOccurence() : null;
    }

    public OccurenceInfo goPreviousOccurence() {
      return reviewNavigatorSupport != null ? reviewNavigatorSupport.goPreviousOccurence() : null;
    }

    public String getNextOccurenceActionName() {
      return reviewNavigatorSupport != null ? reviewNavigatorSupport.getNextOccurenceActionName() : "";
    }

    public String getPreviousOccurenceActionName() {
      return reviewNavigatorSupport != null ? reviewNavigatorSupport.getPreviousOccurenceActionName() : "";
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if(reviewTreeBuilder == null) return;
        reviewTreeBuilder.getUi().doUpdateFromRoot();
        TreeUtil.selectFirstNode(reviewTree);
        showPreview(reviewTree.getSelectedNode());
        previewScrollPane.setVisible(!ReviewManager.getInstance(project).getFileNames().isEmpty()
                                    && isShowPreview);
    }

    public void showPreview(SimpleNode element) {

        Review review = null;
        if (element instanceof FileNode) {
            if(element.getChildCount() > 0)
                showPreview(element.getChildAt(0));
            else return;
        }
        if (element instanceof ReviewNode) {
            review = ((ReviewNode) element).getReview();
        }
        if(review == null) return;
        previewPanel.setVisible(isShowPreview);
        previewPanel.removeAll();
        ReviewPoint point = ReviewManager.getInstance(project).findReviewPoint(review);
        if(point == null) return;
        editReviewForm = new EditReviewForm(review);
        previewPanel.add(editReviewForm.getItemsContent());
        previewPanel.updateUI();

    }

    @Override
    public void dispose() {
        reviewTreeBuilder.dispose();
    }

    public SimpleTree getReviewTree() {
        return reviewTree;
    }

    public AbstractTreeBuilder getReviewTreeBuilder() {
        return reviewTreeBuilder;
    }

    private final class PreviewAction extends ToggleAction  implements DumbAware {

        public PreviewAction() {
            super("Preview reviews", null, IconLoader.getIcon("/actions/preview.png"));
        }



        @Override
        public boolean isSelected(AnActionEvent e) {
            return isShowPreview;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            isShowPreview = state;
            if(reviewTree.isSelectionEmpty()) return;
            showPreview(reviewTree.getSelectedNode());
            previewScrollPane.setVisible(state);
            mainPanel.updateUI();
        }


    }

    private final class GroupByModuleAction extends ToggleAction  implements DumbAware {

        private GroupByModuleAction() {
             super("Group reviews by module", null, IconLoader.getIcon("/actions/modul.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return groupByModule;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            groupByModule = !groupByModule;
            if(reviewTreeStructure == null) {
                createTreeStructure();
            }
            else {
                updateTreeStructure();
            }
            updateUI();
        }
    }

    private void updateTreeStructure() {
        if(groupByModule) {
            SimpleNode[] filenodes = (SimpleNode[])reviewTreeStructure.getChildElements(reviewTreeStructure.getRootElement());
            Map<String, ModuleNode> moduleNodes = new HashMap<String, ModuleNode>();
            for(SimpleNode node : filenodes) {
                if(node instanceof FileNode) {
                    Module module = ModuleUtil.findModuleForFile(((FileNode) node).getFile(), project);
                    String moduleName = module.getName();
                    if(moduleNodes.containsKey(moduleName)) {
                        moduleNodes.get(moduleName).addChild(node);
                    }
                    else {
                        moduleNodes.put(moduleName, new ModuleNode(project, module,  node));
                    }
                }
            }
        }
    }

    private final class SearchAction extends ToggleAction  implements DumbAware {

        private SearchAction() {
             super("Search in reviews", null, IconLoader.getIcon("/actions/find.png"));
        }

        @Override
    public boolean isSelected(AnActionEvent e) {
        return searchEnabled;
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        searchEnabled = !searchEnabled;
        searchLine.setVisible(searchEnabled);
        if(!searchEnabled) {
            ReviewManager.getInstance(project).emptyFilter();
            searchLine.setText("");
            updateUI();
        }
    }
}

    private final class GroupByFileAction extends ToggleAction  implements DumbAware {

        private GroupByFileAction() {
             super("Group reviews by file", null, IconLoader.getIcon("/fileTypes/unknown.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return groupByFile;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            groupByFile = !groupByFile;
            if(reviewTreeStructure == null) {
                createTreeStructure();
            }
            else {
                updateTreeStructure();
            }
            updateUI();
        }
    }
}