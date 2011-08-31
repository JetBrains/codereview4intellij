package ui.reviewtoolwindow;

import com.intellij.ide.DataManager;
import com.intellij.ide.OccurenceNavigator;
import com.intellij.ide.OccurenceNavigatorSupport;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.TextFieldWithAutoCompletion;
import com.intellij.ui.treeStructure.PatchedDefaultMutableTreeNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import com.intellij.util.OpenSourceUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.intellij.util.ui.tree.TreeUtil;
import com.sun.jna.Structure;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.*;
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
import java.util.Comparator;
import java.util.Date;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 2:39 PM
 */
public class ReviewPanel extends  SimpleToolWindowPanel implements DataProvider, OccurenceNavigator, Disposable, DumbAware {
    private static final String ACTION_GROUP = "TreeReviewItemActions";

    private final Project project;
    private SimpleTree reviewTree;
    private AbstractTreeBuilder reviewTreeBuilder;
    private final ReviewsPreviewPanel previewPanel = new ReviewsPreviewPanel();

    @Nullable
    private OccurenceNavigatorSupport reviewNavigatorSupport;
    private final TextFieldWithAutoCompletion searchLine;
    private ReviewTreeStructure reviewTreeStructure;

    private final ReviewToolWindowSettings settings;

    public ReviewPanel(final Project project) {
        super(false);
        this.project = project;
        settings = new ReviewToolWindowSettings(project);
        initTree();
        JPanel mainPanel = new JPanel(new BorderLayout());
        searchLine = new TextFieldWithAutoCompletion(project);
        mainPanel.add(new ReviewToolWindowActionManager(this, settings).createLeftMenu(), BorderLayout.WEST);

        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
        mainPanel.add(scrollPane);

        previewPanel.setVisible(settings.isShowPreviewEnabled() && settings.isEnabled());


        searchLine.setVisible(settings.isSearchEnabled() && settings.isEnabled());
        final String[] variants = Searcher.getInstance(project).getFilterKeywords();
        searchLine.setVariants(variants);
        searchLine.setRequestFocusEnabled(true);
        searchLine.setOneLineMode(true);

        searchLine.addDocumentListener(new DocumentAdapter() {
            @Override
            public void documentChanged(DocumentEvent e) {
                if(e.getNewFragment().toString().endsWith("\n")) {
                    final Searcher instance = Searcher.getInstance(project);
                    instance.createFilter(searchLine.getText());
                    reviewTreeBuilder.getUi().doUpdateFromRoot();
                    if (instance.getFilteredFileNames().isEmpty()) {
                        settings.setEnabled(false);
                        previewPanel.setVisible(false);
                    } else {
                        settings.setEnabled(true);
                    }
                    if (settings.isShowPreviewEnabled()) {
                        showPreview();
                        //previewPanel.updateSelection();
                    }
                }
            }
        });
        searchLine.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    final Searcher instance = Searcher.getInstance(project);
                    instance.createFilter(searchLine.getText());
                    reviewTreeBuilder.getUi().doUpdateFromRoot();
                    if (instance.getFilteredFileNames().isEmpty()) {
                        settings.setEnabled(false);
                        previewPanel.setVisible(false);
                    } else {
                        settings.setEnabled(true);
                    }
                    if (settings.isShowPreviewEnabled() && settings.isEnabled()) {
                        showPreview();
                        //previewPanel.updateSelection();
                    }
                }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        searchLine.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    searchLine.getCaretModel().moveToOffset(searchLine.getText().length());
                    searchLine.setText(searchLine.getText() + "\"");
            }
        } , KeyStroke.getKeyStroke(KeyEvent.VK_QUOTEDBL, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        searchLine.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                searchLine.getCaretModel().moveToOffset(searchLine.getText().length());
            }
        });
        mainPanel.add(searchLine, BorderLayout.NORTH);
        Splitter pane = new Splitter();
        pane.setFirstComponent(mainPanel);
        pane.setSecondComponent(previewPanel);
        pane.setHonorComponentsMinimumSize(true);
        //pane.setProportion(.7f);
        setContent(pane);
        setProvideQuickActions(true);
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

    public void rebuidTree() {
        Comparator<NodeDescriptor> comparator = getComparator();
        reviewTreeBuilder.setNodeDescriptorComparator(comparator);
        reviewTreeBuilder.getUi().doUpdateFromRoot();
    }

    private Comparator<NodeDescriptor> getComparator() {
        Comparator<NodeDescriptor> comparator = null;
        if(settings.isSortByAuthor()){
            comparator = new Comparator<NodeDescriptor>() {
                @Override
                public int compare(NodeDescriptor o1, NodeDescriptor o2) {
                    if(o1.getElement() instanceof ReviewNode && o1.getElement() instanceof ReviewNode) {
                        Review r1 = ((Review)((ReviewNode) o1.getElement()).getObject());
                        Review r2 = ((Review)((ReviewNode) o2.getElement()).getObject());
                        final String firstAuthor = r1.getReviewBean().getReviewItems().get(0).getAuthor();
                        final String secondAuthor = r2.getReviewBean().getReviewItems().get(0).getAuthor();
                        return firstAuthor.compareTo(secondAuthor);
                    }
                    return -1;
                }
            };
        }

        if(settings.isSortByOffset()){
            comparator = new Comparator<NodeDescriptor>() {
                @Override
                public int compare(NodeDescriptor o1, NodeDescriptor o2) {
                     if(o1.getElement() instanceof ReviewNode && o1.getElement() instanceof ReviewNode) {
                        Review r1 = ((Review)((ReviewNode) o1.getElement()).getObject());
                        Review r2 = ((Review)((ReviewNode) o2.getElement()).getObject());
                        if(r1.getFilePath().equals(r2.getFilePath())) {
                            final int firstOffset = r1.getStart();
                            final int secondOffset = r2.getStart();
                            return firstOffset == secondOffset? 0 : firstOffset > secondOffset? 1 : -1;
                        }
                     }
                    return -1;
                }
            };
        }

        if(settings.isSortByLastCommenter()){
            comparator = new Comparator<NodeDescriptor>() {
                @Override
                public int compare(NodeDescriptor o1, NodeDescriptor o2) {
                     if(o1.getElement() instanceof ReviewNode && o1.getElement() instanceof ReviewNode) {
                        Review r1 = ((Review)((ReviewNode) o1.getElement()).getObject());
                        Review r2 = ((Review)((ReviewNode) o2.getElement()).getObject());
                        final String firstLastAuthor = r1.getLastCommenter();
                        final String secondLastAuthor = r2.getLastCommenter();
                        return firstLastAuthor.compareTo(secondLastAuthor);
                     }
                    return -1;
                }
            };
        }

        if(settings.isSortByAuthor()){
            comparator = new Comparator<NodeDescriptor>() {
                @Override
                public int compare(NodeDescriptor o1, NodeDescriptor o2) {
                     if(o1.getElement() instanceof ReviewNode && o1.getElement() instanceof ReviewNode) {
                        Review r1 = ((Review)((ReviewNode) o1.getElement()).getObject());
                        Review r2 = ((Review)((ReviewNode) o2.getElement()).getObject());
                        final String firstAuthor = r1.getReviewBean().getReviewItems().get(0).getAuthor();
                        final String secondAuthor = r2.getReviewBean().getReviewItems().get(0).getAuthor();
                        return firstAuthor.compareTo(secondAuthor);
                     }
                    return -1;
                }
            };
        }

        if(settings.isSortByDate()) {
            comparator = new Comparator<NodeDescriptor>() {
                @Override
                public int compare(NodeDescriptor o1, NodeDescriptor o2) {
                     if(o1.getElement() instanceof ReviewNode && o1.getElement() instanceof ReviewNode) {
                        Review r1 = ((Review)((ReviewNode) o1.getElement()).getObject());
                        Review r2 = ((Review)((ReviewNode) o2.getElement()).getObject());
                        final Date firstDate = r1.getFirstDate();
                        final Date secondDate = r2.getFirstDate();
                        return firstDate.compareTo(secondDate);
                     }
                    return -1;
                }
            };
        }
        return comparator;
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
            final Review review = (Review) ((ReviewNode) node).getObject();
            final Editor editor = FileEditorManager.getInstance(project).getSelectedTextEditor();
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ReviewActionManager.getInstance().showExistingComments(editor, review);
                }
            });
        }
    }

    private void createTreeStructure() {
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
                    return ((FileNode) element).getObject();
                }
            }
            if(Review.REVIEW_DATA_KEY.is(dataId)) {
                if (element instanceof ReviewNode) {
                    return ((ReviewNode) element).getObject();
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


            if(settings.isSearchEnabled())
                IdeFocusManager.getInstance(project).requestFocus(searchLine, true);

            searchLine.setVisible(settings.isSearchEnabled() && settings.isEnabled());
            searchLine.setText(Searcher.getInstance(project).getFilter());

            if(reviewTreeBuilder == null) return;
            reviewTreeBuilder.getUi().doUpdateFromRoot();

            if(settings.isShowPreviewEnabled() && settings.isEnabled()) {
                Set<String> fileNames = ReviewManager.getInstance(project).getFileNames();
                Set<String> filteredFileNames = Searcher.getInstance(project).getFilteredFileNames();
                if(fileNames == null) return;

                boolean visible = !(fileNames.isEmpty() && filteredFileNames.isEmpty());
                previewPanel.setVisible(visible);
                showPreview();
            } else {
                previewPanel.setVisible(false);
            }
        }
        super.updateUI();
    }

    public void showPreview() {
        showPreview(reviewTree.getSelectedNode());
    }

    private void showPreview(SimpleNode element) {
        Review review = null;
        if (element instanceof RootNode || element instanceof ModuleNode || element instanceof FileNode) {
            if(element.getChildCount() > 0)
                showPreview(element.getChildAt(0));
            else return;
        }
        if (element instanceof ReviewNode) {
            review = (Review) ((ReviewNode) element).getObject();
        }
        if(review == null) return;
        previewPanel.update(review);
        //updateUI();
    }

    @Override
    public void dispose() {
        reviewTreeBuilder.dispose();
        settings.saveState();
    }

    public class ReviewsListener implements ReviewsChangedListener, DumbAware {

        @Override
        public void reviewAdded(Review review) {
            update(reviewTreeStructure.addReview(review));
            if(!settings.isEnabled())settings.setEnabled(true);

        }

        @Override
        public void reviewDeleted(Review review) {
            reviewTreeStructure.removeReview(review);
            reviewTreeBuilder.getUi().doUpdateFromRoot();
            if(((PlainNode)reviewTreeStructure.getRootElement()).getChildren().length == 0) {
                settings.setEnabled(false);
                updateUI();
            }
        }

        @Override
        public void reviewChanged(Review newReview) {
            update(reviewTreeStructure.getNode(newReview));
            showPreview();
        }

        private void update(PlainNode node) {
            if(node == null || node.equals(reviewTreeStructure.getRootElement())) {
                 reviewTreeBuilder.getUi().doUpdateFromRoot();

            } else {
                DefaultMutableTreeNode nodeToUpdate = TreeUtil.findNodeWithObject(
                                                            reviewTreeBuilder.getRootNode(),
                                                            node
                                                      );
                if(nodeToUpdate == null) {
                    reviewTreeBuilder.getUi().doUpdateFromRoot();
                    return;
                }
                reviewTreeBuilder.addSubtreeToUpdate(nodeToUpdate);
                reviewTreeBuilder.queueUpdate();
            }
        }
    }
}