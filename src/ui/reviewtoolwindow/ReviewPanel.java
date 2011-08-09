package ui.reviewtoolwindow;

import com.intellij.ide.DataManager;
import com.intellij.ide.ExporterToTextFile;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.OccurenceNavigatorSupport;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.SmartExpander;
import com.intellij.ui.treeStructure.*;
import com.intellij.util.EditSourceOnDoubleClickHandler;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.messages.Topic;
import com.intellij.util.ui.tree.TreeUtil;
import com.intellij.util.xmlb.XmlSerializer;
import com.sun.xml.internal.ws.server.StatefulInstanceResolver;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewChangedTopics;
import reviewresult.ReviewManager;
import reviewresult.ReviewsChangedListener;
import reviewresult.persistent.ReviewsState;
import ui.actions.ReviewActionManager;
import ui.forms.EditReviewForm;
import ui.reviewtoolwindow.nodes.FileNode;
import ui.reviewtoolwindow.nodes.ModuleNode;
import ui.reviewtoolwindow.nodes.ReviewNode;
import ui.reviewtoolwindow.nodes.RootNode;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.EventListener;
import java.util.Set;
import java.util.TooManyListenersException;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 2:39 PM
 */
public class ReviewPanel extends  SimpleToolWindowPanel implements DataProvider, OccurenceNavigator, Disposable, DumbAware {
    public static final String ACTION_GROUP = "TreeReviewItemActions";

    private Project project;
    private JPanel mainPanel;
    private SimpleTree reviewTree;
    private AbstractTreeBuilder reviewTreeBuilder;

    //private JScrollPane previewScrollPane;
    private ReviewsPreviewPanel previewPanel = new ReviewsPreviewPanel();

    @Nullable
    private OccurenceNavigatorSupport reviewNavigatorSupport;
    private JTextField searchLine = new JTextField();
    private SimpleTreeStructure reviewTreeStructure;

    private ReviewToolWindowSettings settings;
    private EditReviewForm editReviewForm;



    public ReviewPanel(final Project project) {
        super(false);
        this.project = project;
        settings = new ReviewToolWindowSettings(project, this);
        Searcher.getInstance(project).createFilter("");

        initTree();
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);

        mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(settings.createLeftMenu(), BorderLayout.WEST);
        mainPanel.add(scrollPane);

        mainPanel.add(previewPanel, BorderLayout.EAST);
        previewPanel.setVisible(settings.isShowPreview());
        searchLine.setVisible(settings.isSearchEnabled());
        searchLine.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Searcher.getInstance(project).createFilter(searchLine.getText());
                createTreeStructure();
                updateUI();
                if(settings.isShowPreview()) {
                    editReviewForm.updateSelection();
                }
            }
        });
        mainPanel.add(searchLine, BorderLayout.NORTH);
        setContent(mainPanel);
        MessageBusConnection connection = project.getMessageBus().connect(project);
        connection.subscribe(ReviewChangedTopics.REVIEW_STATUS, new ReviewsListener());
    }

    private void initTree() {
        createTreeStructure();
        final DefaultTreeModel model = new DefaultTreeModel(new PatchedDefaultMutableTreeNode());
        reviewTree = new SimpleTree(model);
        SmartExpander.installOn(reviewTree);
        reviewTreeBuilder = new SimpleTreeBuilder(reviewTree, model, reviewTreeStructure, null);
        TreeUtil.expandAll(reviewTree);
        reviewTree.revalidate();
        reviewTree.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() != 2) return;
                if (ModalityState.current().dominates(ModalityState.NON_MODAL)) return;
                if (reviewTree.getPathForLocation(e.getX(), e.getY()) == null) return;
                DataContext dataContext = DataManager.getInstance().getDataContext(reviewTree);
                Project project = PlatformDataKeys.PROJECT.getData(dataContext);
                if (project == null) return;
                OpenSourceUtil.openSourcesFrom(dataContext, true);
                SimpleNode node = (SimpleNode)reviewTree.getSelectedNode().getElement();
                if(node instanceof ReviewNode) {
                    Review review = ((ReviewNode) node).getReview();
                    Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
                    ReviewActionManager.getInstance(review).showExistingComments(editor);
                }
                }
            });
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
        reviewTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getButton() == MouseEvent.BUTTON3) {

                }
            }
        });
        reviewTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                if(settings.isShowPreview()) {
                    SimpleNode node = reviewTree.getSelectedNode();
                    showPreview(node);
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
        PopupHandler.installPopupHandler(reviewTree, ACTION_GROUP, ActionPlaces.TODO_VIEW_POPUP);
        SmartExpander.installOn(reviewTree);
    }

    public void createTreeStructure() {
        reviewTreeStructure = new ReviewTreeStructure(project, new RootNode(project, settings), settings);
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
        super.updateUI();
        if(settings != null) {
            searchLine.setVisible(settings.isSearchEnabled());
            if(reviewTreeBuilder == null) return;
            reviewTreeBuilder.getUi().doUpdateFromRoot();
            Set<String> fileNames = ReviewManager.getInstance(project).getFileNames();
            Set<String> filteredFileNames = Searcher.getInstance(project).getFilteredFileNames();
            previewPanel.setVisible(!(fileNames.isEmpty() && filteredFileNames.isEmpty())
                    && settings.isShowPreview());
        }
    }

    public void showPreview(SimpleNode element) {
        Review review = null;
        if (element instanceof ModuleNode || element instanceof FileNode) {
            if(element.getChildCount() > 0)
                showPreview(element.getChildAt(0));
            else return;
        }
        if (element instanceof ReviewNode) {
            review = ((ReviewNode) element).getReview();
        }
        if(review == null) return;
        previewPanel.update(review);
    }

    @Override
    public void dispose() {
        reviewTreeBuilder.dispose();
    }

    public class ReviewsListener implements ReviewsChangedListener{

        @Override
        public void reviewAdded(Review review) {
            ((ReviewTreeStructure)reviewTreeStructure).addNode(review, settings);
            reviewTreeBuilder.getUi().doUpdateFromRoot();
        }

        @Override
        public void reviewDeleted(Review review) {
            ((ReviewTreeStructure)reviewTreeStructure).removeReview(review);
            reviewTreeBuilder.getUi().doUpdateFromRoot();
        }

        @Override
        public void reviewChanged(Review review) {
            reviewTreeBuilder.getUi().doUpdateFromRoot();
        }
    }

}