package ui.reviewtoolwindow;

import com.intellij.ide.DataManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.OccurenceNavigatorSupport;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.*;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewChangedTopics;
import reviewresult.ReviewManager;
import reviewresult.ReviewsChangedListener;
import ui.actions.ReviewActionManager;
import ui.reviewtoolwindow.nodes.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 2:39 PM
 */
public class ReviewPanel extends  SimpleToolWindowPanel implements DataProvider, OccurenceNavigator, Disposable, DumbAware {
    public static final String ACTION_GROUP = "TreeReviewItemActions";

    private Project project;
    private SimpleTree reviewTree;
    private AbstractTreeBuilder reviewTreeBuilder;

    private ReviewsPreviewPanel previewPanel = new ReviewsPreviewPanel();

    @Nullable
    private OccurenceNavigatorSupport reviewNavigatorSupport;
    private JTextField searchLine = new JTextField();
    private ReviewTreeStructure reviewTreeStructure;

    private ReviewToolWindowSettings settings;

    public ReviewPanel(final Project project) {
        super(false);
        this.project = project;
        settings = new ReviewToolWindowSettings(project);
        initTree();
        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(new ReviewToolWindowActionManager(this, settings).createLeftMenu(), BorderLayout.WEST);

        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
        mainPanel.add(scrollPane);

        previewPanel.setVisible(settings.isShowPreviewEnabled());
        mainPanel.add(previewPanel, BorderLayout.EAST);

        searchLine.setVisible(settings.isSearchEnabled());
        searchLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Searcher.getInstance(project).createFilter(searchLine.getText());
                //createTreeStructure();
                //updateUI();
                reviewTreeBuilder.getUi().doUpdateFromRoot();
                if(settings.isShowPreviewEnabled()) {
                    previewPanel.updateSelection();
                }
            }
        });
        searchLine.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                searchLine.setCaretPosition(searchLine.getText().length());
            }
        });
        mainPanel.add(searchLine, BorderLayout.NORTH);

        setContent(mainPanel);

        MessageBusConnection connection = project.getMessageBus().connect(project);
        connection.subscribe(ReviewChangedTopics.REVIEW_STATUS, new ReviewsListener());
    }

    private void initTree() {
        createTreeStructure();

        PatchedDefaultMutableTreeNode root = new PatchedDefaultMutableTreeNode();
        final DefaultTreeModel model = new DefaultTreeModel(root);
        reviewTree = new SimpleTree(model);

        reviewTreeBuilder = new SimpleTreeBuilder(reviewTree, model, reviewTreeStructure, null);


        reviewTree.revalidate();

        TreeUtil.expandAll(reviewTree);
        reviewTree.setRootVisible(false);
        PopupHandler.installPopupHandler(reviewTree, ACTION_GROUP, ActionPlaces.TODO_VIEW_POPUP);



        reviewTree.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                    openReview();
                }
            });

        reviewTree.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (KeyEvent.VK_ENTER == e.getKeyCode()) {
                   openReview();
                }
            }
        });

        reviewTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if(settings.isShowPreviewEnabled()) {
                    showPreview();
                }
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
    }

    private void openReview() {
        DataContext dataContext = DataManager.getInstance().getDataContext(reviewTree);
        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) return;
        OpenSourceUtil.openSourcesFrom(dataContext, true);
        SimpleNode selectedNode = reviewTree.getSelectedNode();
        if(selectedNode == null) return;
        SimpleNode node = (SimpleNode) selectedNode.getElement();
        if(node instanceof ReviewNode) {
            final Review review = ((ReviewNode) node).getReview();
            final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ReviewActionManager.getInstance(review).showExistingComments(editor);
                }
            });
        }
    }

    public void createTreeStructure() {
        reviewTreeStructure = new ReviewTreeStructure(project, settings);
    }

    @Override
    public Object getData(@NonNls String dataId) {
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
            if (PlatformDataKeys.NAVIGATABLE.is(dataId)){
                    return element;
            }
            if(PlatformDataKeys.VIRTUAL_FILE.is(dataId)) {
                if (element instanceof FileNode) {
                    return ((FileNode) element).getFile();
                }
            }
            if(Review.REVIEW_DATA_KEY.is(dataId)) {
                if (element instanceof ReviewNode) {
                    return ((ReviewNode) element).getReview();
                }
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
        if(settings != null) {

            if(reviewTreeBuilder == null) return;
            reviewTreeBuilder.getUi().doUpdateFromRoot();

            if(settings.isSearchEnabled())
                IdeFocusManager.getInstance(project).requestFocus(searchLine, true);

            searchLine.setVisible(settings.isSearchEnabled());
            searchLine.setText(Searcher.getInstance(project).getFilter());

            if(settings.isShowPreviewEnabled()) {
                Set<String> fileNames = ReviewManager.getInstance(project).getFileNames();
                Set<String> filteredFileNames = Searcher.getInstance(project).getFilteredFileNames();
                if(fileNames == null) return;

                boolean visible = !(fileNames.isEmpty() && filteredFileNames.isEmpty());
                previewPanel.setVisible(visible);
            } else {
                previewPanel.setVisible(false);
            }
        }
        super.updateUI();
    }

    public void showPreview() {
        showPreview(reviewTree.getSelectedNode());
    }

    public void showPreview(SimpleNode element) {
        Review review = null;
        if (element instanceof RootNode || element instanceof ModuleNode || element instanceof FileNode) {
            if(element.getChildCount() > 0)
                showPreview(element.getChildAt(0));
            else return;
        }
        if (element instanceof ReviewNode) {
            review = ((ReviewNode) element).getReview();
        }
        if(review == null) return;
        previewPanel.update(review);
        updateUI();
    }

    @Override
    public void dispose() {
        reviewTreeBuilder.dispose();
        settings.saveState();
    }

    public void updateStructure() {
        reviewTreeBuilder.getUi().doUpdateFromRoot();
    }

    public class ReviewsListener implements ReviewsChangedListener, DumbAware {

        @Override
        public void reviewAdded(Review review) {
            update(reviewTreeStructure.addReview(review));
        }

        @Override
        public void reviewDeleted(Review review) {
            reviewTreeStructure.removeReview(review);
            reviewTreeBuilder.getUi().doUpdateFromRoot();
            if(((PlainNode)reviewTreeStructure.getRootElement()).getChildren().length == 0) {
                settings.reset();
                updateUI();
            }
        }

        @Override
        public void reviewChanged(Review newReview) {
            update(reviewTreeStructure.getNode(newReview));

        }

        private void update(PlainNode node) {
            if(node.equals(reviewTreeStructure.getRootElement())) {
                 reviewTreeBuilder.getUi().doUpdateFromRoot();

            } else {
                DefaultMutableTreeNode nodeToUpdate = TreeUtil.findNodeWithObject(
                                                            reviewTreeBuilder.getRootNode(),
                                                            node
                                                      );
                if(nodeToUpdate == null) return;
                reviewTreeBuilder.addSubtreeToUpdate(nodeToUpdate);
                reviewTreeBuilder.queueUpdate();
            }
        }
    }
}