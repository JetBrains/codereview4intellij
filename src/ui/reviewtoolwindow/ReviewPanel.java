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
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.pom.Navigatable;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.PatchedDefaultMutableTreeNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
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
import ui.reviewtoolwindow.filter.Searcher;
import ui.reviewtoolwindow.filter.SmartTextFieldWithAutoComplete;
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
    private JPanel searchPanel;
    private ReviewTreeStructure reviewTreeStructure;

    private final ReviewToolWindowSettings settings;

    public ReviewPanel(final Project project) {
        super(false);
        this.project = project;
        settings = new ReviewToolWindowSettings(project);
        initTree();
        JPanel mainPanel = new JPanel(new BorderLayout());

        mainPanel.add(new ReviewToolWindowActionManager(this, settings).createLeftMenu(), BorderLayout.WEST);

        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(reviewTree);
        mainPanel.add(scrollPane);

        previewPanel.setVisible(settings.isShowPreviewEnabled() && settings.isEnabled());
        setupSearchLine(project, mainPanel);
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

    private void setupSearchLine(final Project project, JPanel mainPanel) {
        searchPanel = new JPanel( new BorderLayout());
        final SmartTextFieldWithAutoComplete searchLine = new SmartTextFieldWithAutoComplete(project);
        searchPanel.setVisible(settings.isSearchEnabled() && settings.isEnabled());
        searchPanel.setFocusable(true);
        searchPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                IdeFocusManager.
                        findInstanceByComponent(searchLine).
                            requestFocus(searchLine, true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                settings.setSearchEnabled(false);
                final Searcher searcher = Searcher.getInstance(project);
                searcher.emptyFilter();
                final boolean enabled = !searcher.getFilteredFileNames().isEmpty();
                settings.setEnabled(enabled);
                updateUI();
            }
        });

        searchLine.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final Searcher searcher = Searcher.getInstance(project);
                searcher.createFilter(searchLine.extractSuffix());

                reviewTreeBuilder.getUi().doUpdateFromRoot();
                final boolean enabled = !searcher.getFilteredFileNames().isEmpty();
                if(!enabled) {
                    settings.setEnabled(enabled);
                    previewPanel.setVisible(enabled);
                }
                if (settings.isShowPreviewEnabled() && settings.isEnabled()) {
                    updateUI();
                }
                String filtersText = searcher.getAdditionalFilterText();
                searchLine.setInactivePrefix(filtersText);
                searchLine.setText(filtersText + searcher.getFilter());
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        searchLine.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settings.setSearchEnabled(false);
                updateUI();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        searchLine.addDocumentListener(new DocumentAdapter() {
            @Override
            public void beforeDocumentChange(DocumentEvent event) {
                if("".equals(searchLine.getText().trim())) {
                    Searcher.getInstance(project).emptyFilter();
                    updateUI();
                }
            }
        });
        searchLine.setCenterByHeight(true);

        final Icon icon = IconLoader.getIcon("/actions/close.png");
        final Icon hoveredIcon = IconLoader.getIcon("/actions/closeHovered.png");
        final JButton closeSearchButton = new JButton(icon);
        closeSearchButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
               closeSearchButton.setIcon(hoveredIcon);
            }

            @Override
            public void mouseExited(MouseEvent e) {
               closeSearchButton.setIcon(icon);
            }
        });
        closeSearchButton.setBorder(BorderFactory.createEmptyBorder());
        closeSearchButton.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
        closeSearchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                settings.setSearchEnabled(false);
                updateUI();
            }
        });

        final JCheckBox caseSensitive = new JCheckBox("Case Sensitive");

        caseSensitive.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Searcher.getInstance(project).setCaseSensitive(caseSensitive.isSelected());
            }
        });

        JPanel linePanel = new JPanel(new BorderLayout());
        linePanel.add(searchLine);
        linePanel.add(caseSensitive, BorderLayout.EAST);
        searchPanel.add(linePanel);
        searchPanel.add(closeSearchButton, BorderLayout.EAST);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
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
                if(e.getKeyCode() == KeyEvent.VK_F  && e.getModifiers() == KeyEvent.CTRL_MASK) {
                    settings.setSearchEnabled(true);
                    updateUI();
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
                        final String firstAuthor = r1.getFirstCommenter();
                        final String secondAuthor = r2.getFirstCommenter();
                        if(secondAuthor != null && firstAuthor != null)
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

        if(settings.isSortByDate()) {
            comparator = new Comparator<NodeDescriptor>() {
                @Override
                public int compare(NodeDescriptor o1, NodeDescriptor o2) {
                     if(o1.getElement() instanceof ReviewNode && o1.getElement() instanceof ReviewNode) {
                        Review r1 = ((Review)((ReviewNode) o1.getElement()).getObject());
                        Review r2 = ((Review)((ReviewNode) o2.getElement()).getObject());
                        final Date firstDate = r1.getDateOfCreation();
                        final Date secondDate = r2.getDateOfCreation();
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
        SimpleNode selectedNode = reviewTree.getSelectedNode();
        if(selectedNode == null) return;
        SimpleNode node = (SimpleNode) selectedNode.getElement();
        if(node instanceof ReviewNode) {
            final Review review = (Review) ((ReviewNode) node).getObject();
            OpenSourceUtil.openSourcesFrom(dataContext, true);
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
            //if(settings.isSearchEnabled())
              //  IdeFocusManager.getInstance(project).requestFocus(searchLine, true);

            searchPanel.setVisible(settings.isSearchEnabled() && settings.isEnabled());
            //searchLine.setText(Searcher.getInstance(project).getFilter());

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