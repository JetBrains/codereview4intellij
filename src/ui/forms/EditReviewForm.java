package ui.forms;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import ui.actions.ReviewActionManager;
import ui.reviewtoolwindow.filter.Searcher;
import utils.Util;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 2:27 PM
 */
public class EditReviewForm {
    public static final int SIZE = 13;
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private EditorTextField newReviewItemText;
    private List<String> tags = new ArrayList<String>();

    private final Review review;
    private boolean addItem;
    private boolean editItem;
    //private final boolean addOrEditItem;
    private static final int TAG_LENGTH = 20;
    private static final int BALLOON_WIDTH = 250;
    private static final int BALLOON_HEIGHT = 400;
    private static final int MIN_BALLOON_WIDTH = 450;
    private static final int MIN_BALLOON_HEIGHT = 400;
    private TextFieldWithAutoCompletion tagsField;
    //private JComboBox statusCombo;
    private static final long FADEOUT_TIME = 1000;
    private JPanel tagsPanel;

    public EditReviewForm(final Review review, boolean addItem, boolean editItem, boolean spellCheck) {
        this.review = review;
        this.addItem = addItem;
        this.editItem = editItem;

        this.tags.addAll(review.getTags());

        setupTags();
        setupItemsContent();

        if(addItem || editItem) {
            setupLastItem(spellCheck);
        }

        mainPanel.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK), "saveReview");
        mainPanel.getInputMap(JPanel.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exitReview");

        mainPanel.getActionMap().put("saveReview", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveReview();
            }
        });
        mainPanel.getActionMap().put("exitReview", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 ReviewActionManager.getInstance().disposeActiveBalloon();
            }
        });

        final Dimension preferredSize = mainPanel.getPreferredSize();
        if(preferredSize.getHeight() > BALLOON_HEIGHT) {
            preferredSize.height = BALLOON_HEIGHT;
        }

        if(preferredSize.getWidth() > BALLOON_WIDTH) {
            preferredSize.width = BALLOON_WIDTH;
        }

        if(preferredSize.getHeight() < MIN_BALLOON_HEIGHT) {
            preferredSize.height = MIN_BALLOON_HEIGHT;
        }

        if(preferredSize.getWidth() < MIN_BALLOON_WIDTH) {
            preferredSize.width = MIN_BALLOON_WIDTH;
        }

        mainPanel.setPreferredSize(preferredSize);
        setupTagsPanelSize();
    }

    private void setupLastItem(boolean spellCheck) {
        newReviewItemText = createInputField(spellCheck);
        final ReviewItem lastReviewItem = review.getLastReviewItem();
        if(lastReviewItem != null && lastReviewItem.isMine()) {
            newReviewItemText.setText(lastReviewItem.getText());
        }
        newReviewItemText.setBackground(Color.WHITE);
        newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, SIZE));
        final Editor editor = newReviewItemText.getEditor();
        newReviewItemText.setMaximumSize(newReviewItemText.getPreferredSize());
        if(editor != null) {
            editor.getSettings().setAdditionalPageAtBottom(true);
            editor.getComponent();
        }

        newReviewItemText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                newReviewItemText.setBorder(BorderFactory.createEmptyBorder());
                newReviewItemText.invalidate();
            }
        });

        //statusCombo = new JComboBox(ReviewStatus.values());
        JPanel newReviewItemPanel = new JPanel(new BorderLayout());
        newReviewItemPanel.add(newReviewItemText);
        //newReviewItemPanel.add(statusCombo, BorderLayout.SOUTH);
        //final List<ReviewItem> reviewItems = review.getReviewItems();
        mainPanel.add(newReviewItemPanel);
    }

    private void setupTags() {
        final JPanel mainTagsPanel = new JPanel(new BorderLayout());
        tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 5));
        final JLabel tagsLabel = new JLabel("Tags: ");
        tagsLabel.setFont(new Font("Verdana", Font.PLAIN, SIZE));
        tagsPanel.add(tagsLabel);
        if(tags.isEmpty()) tagsLabel.setVisible(false);

        for(final String tag : tags) {
            setupTagLabel(tag, tagsLabel);
        }

        tagsField = new TextFieldWithAutoCompletion(review.getProject());
        tagsField.setVariants(ReviewManager.getInstance(review.getProject()).getAvailableTags());
        tagsField.setBackground(Color.WHITE);
        tagsPanel.setFocusable(true);
        tagsField.setRequestFocusEnabled(true);
        tagsField.setFont(new Font("Verdana", Font.PLAIN, SIZE));
        tagsField.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String tag = tagsField.getText();
                if("".equals(tag.trim()) || tags.contains(tag)) {
                    BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().
                            createHtmlTextBalloonBuilder(
                                    "Tag already exists", MessageType.WARNING, null);
                    balloonBuilder.setFadeoutTime(FADEOUT_TIME);
                    balloonBuilder.setHideOnKeyOutside(true);
                    balloonBuilder.setHideOnClickOutside(true);
                    balloonBuilder.createBalloon().show(new RelativePoint(tagsPanel, tagsPanel.getLocation()), Balloon.Position.above);
                } else {
                    tagsLabel.setVisible(true);
                    tags.add(tag);
                    tagsField.setText("");
                    setupTagLabel(tag, tagsLabel);
                    setupTagsPanelSize();
                }
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainTagsPanel.add(tagsPanel);
        if(addItem || editItem){
            mainTagsPanel.add(tagsField, BorderLayout.SOUTH);
        }
        mainPanel.add(mainTagsPanel, BorderLayout.SOUTH);

    }

    private void setupTagLabel(final String tag, final JLabel tagsLabel) {
        final JPanel oneTagPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 1, 5));
        final JLabel tagLabel = new JLabel(tag);
        if(tag.length() > TAG_LENGTH) {
            tagLabel.setToolTipText(tag);
            tagLabel.setText(tag.substring(0, TAG_LENGTH - 3) + "...");
        }
        tagLabel.setFont(new Font("Verdana", Font.PLAIN, SIZE));
        oneTagPanel.add(tagLabel);
        if(addItem || editItem) {
            final Icon icon = IconLoader.getIcon("/actions/close.png");
            final Icon hoveredIcon = IconLoader.getIcon("/actions/closeHovered.png");
            final JButton deleteTagButton = new JButton(icon);
            deleteTagButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                   deleteTagButton.setIcon(hoveredIcon);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                   deleteTagButton.setIcon(icon);
                }
            });
            deleteTagButton.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
            deleteTagButton.setBorderPainted(false);
            deleteTagButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    tags.remove(tag);
                    if(tags.isEmpty())tagsLabel.setVisible(false);
                    tagsPanel.remove(oneTagPanel);
                    setupTagsPanelSize();
                }
            });
            oneTagPanel.add(deleteTagButton);
        }
        tagsPanel.add(oneTagPanel);
    }

    private void setupTagsPanelSize() {
        Dimension layoutSize = tagsPanel.getLayout().preferredLayoutSize(tagsPanel);
        int width = mainPanel.getPreferredSize().width;
        int tagsPanelHeight =  layoutSize.height * (int)(((float)layoutSize.width/width) + 1);
        tagsPanel.setPreferredSize(new Dimension(mainPanel.getSize().width,tagsPanelHeight));
        mainPanel.revalidate();
        mainPanel.repaint();

    }


    private EditorTextField createInputField(boolean checkSpelling) {
        final EnumSet<EditorCustomization.Feature> enabledFeatures =
                                                    EnumSet.of(EditorCustomization.Feature.SOFT_WRAP);
        Set<EditorCustomization.Feature> disabledFeatures =
                                                    EnumSet.of( EditorCustomization.
                                                                        Feature.HORIZONTAL_SCROLLBAR);
        if (checkSpelling) {
          enabledFeatures.add(EditorCustomization.Feature.SPELL_CHECK);
        }
        else {
          disabledFeatures.add(EditorCustomization.Feature.SPELL_CHECK);
        }
        EditorTextFieldProvider service = ServiceManager.
                                                getService(review.getProject(),
                                                        EditorTextFieldProvider.class);

        return service.getEditorField(FileTypes.PLAIN_TEXT.getLanguage(),
                                      review.getProject(),
                                      enabledFeatures,
                                      disabledFeatures);
    }

    public void saveReview() {
        final ReviewManager instance = ReviewManager.getInstance(review.getProject());
        instance.addTags(tags);
        review.setTags(tags);

        if(addItem || editItem) {
            String text = newReviewItemText.getText().trim();
            if ("".equals(text)) {
                newReviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
                newReviewItemText.invalidate();
                return;
            }
            final ReviewItem lastReviewItem = review.getLastReviewItem();

            if(lastReviewItem != null && lastReviewItem.isMine()) {
                lastReviewItem.setText(text);
                lastReviewItem.setDate(new Date());
            } else {
                review.addReviewItem(new ReviewItem(text));
            }
            final JScrollPane newItemScrollPane = ScrollPaneFactory.createScrollPane(new JPanel());
            newItemScrollPane.add(newReviewItemText);


            newReviewItemText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    newItemScrollPane.setPreferredSize(newReviewItemText.getPreferredSize());
                    newItemScrollPane.revalidate();
                }
            });
        }

        ReviewActionManager.getInstance().disposeActiveBalloon();
        if (review.getReviewItems().size() == 1) {
                if(addItem)
                    instance.placeReview(review);
                if(editItem)
                    instance.updateReview(review);
        } else {
            instance.changeReview(review);
        }
    }

    public JComponent getContent() {
        mainPanel.setFocusable(true);
        return mainPanel;
    }

    private void setupItemsContent() {
        JEditorPane itemsText = new JEditorPane();
        itemsText.setEditable(false);
        itemsText.setContentType(UIUtil.HTML_MIME);

        final HTMLEditorKit htmlEditorKit = UIUtil.getHTMLEditorKit();
        final StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
        styleSheet.addRule(" span.highlight {background-color:FFFF00}");
        //styleSheet.addRule("html body div.review_item {font-size: 12pt; }");
        htmlEditorKit.setStyleSheet(styleSheet);
        itemsText.setEditorKit(htmlEditorKit);
        String result = "";

        final List<ReviewItem> reviewItems = review.getReviewItems();
        if(reviewItems.isEmpty())return;
        for (int i = 0; i < reviewItems.size(); i++) {
            ReviewItem reviewItem = reviewItems.get(i);
            if(!(i == reviewItems.size() - 1 && reviewItem.isMine() && (addItem || editItem))) {
                result += reviewItem.getHtmlReport(Searcher.getInstance(review.getProject()));
            }
        }
        result = Util.getInstance(review.getProject()).getHTMLContents(result);
        if("".equals(result)) return;
        itemsText.setText(result);
        itemsText.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent hyperlinkEvent) {
                BrowserUtil.launchBrowser(hyperlinkEvent.getURL().toString());
            }
        });

        JScrollPane pane = ScrollPaneFactory.createScrollPane(itemsText);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        if(addItem || editItem) {
            mainPanel.add(pane, BorderLayout.NORTH);
        }
        else {
            mainPanel.add(pane);
        }
    }

    public void requestFocus() {
        if(addItem || editItem) {
            IdeFocusManager.
                        findInstanceByComponent(newReviewItemText).
                            requestFocus(newReviewItemText, true);
        }
    }

}
