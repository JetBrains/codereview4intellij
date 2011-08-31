package ui.forms;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.UIUtil;
import com.intellij.util.xmlb.XmlSerializer;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;
import reviewresult.persistent.ReviewItem;
import ui.actions.ReviewActionManager;

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
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private EditorTextField newReviewItemText;
    private List<String> tags = new ArrayList<String>();

    private final Review review;
    private final boolean addOrEditItem;
    private static final int TAG_LENGTH = 20;
    private static final int BALLOON_WIDTH = 250;
    private static final int BALLOON_HEIGHT = 400;
    private static final int MIN_BALLOON_WIDTH = 450;
    private static final int MIN_BALLOON_HEIGHT = 400;
    private TextFieldWithAutoCompletion tagsField;
    private JComboBox statusCombo;
    private static final long FADEOUT_TIME = 1000;
    private JPanel tagsPanel;

    public EditReviewForm(final Review review, boolean addOrEditItem, boolean spellCheck) {
        this.review = review;
        this.addOrEditItem = addOrEditItem;

        setupTags();
        setupItemsContent();

        if(addOrEditItem) {
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
        newReviewItemText = createInputField(spellCheck, false);
        final ReviewItem lastReviewItem = review.getLastReviewItem();
        if(lastReviewItem != null && lastReviewItem.isMine()) {
            newReviewItemText.setText(lastReviewItem.getText());
        }
        newReviewItemText.setBackground(Color.WHITE);
        newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, 12));
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

        statusCombo = new JComboBox(ReviewStatus.values());
        JPanel newReviewItemPanel = new JPanel(new BorderLayout());
        newReviewItemPanel.add(newReviewItemText);
        newReviewItemPanel.add(statusCombo, BorderLayout.SOUTH);
        final List<ReviewItem> reviewItems = review.getReviewItems();
        if(reviewItems.isEmpty() || (lastReviewItem != null && reviewItems.size() == 1 && lastReviewItem.isMine())) {
            mainPanel.add(newReviewItemPanel);
        }
        else {
            mainPanel.add(newReviewItemPanel, BorderLayout.SOUTH);
        }
    }

    private void setupTags() {
        JPanel mainTagsPanel = new JPanel(new BorderLayout());
        tagsPanel = new JPanel();
        for(final String tag : review.getReviewBean().getTags()) {
            final JLabel tagLabel = new JLabel(tag);
            //comp.setBorderPainted(false);
            if(tag.length() > TAG_LENGTH) {
                tagLabel.setToolTipText(tag);
                tagLabel.setText(tag.substring(0, TAG_LENGTH - 3) + "...");
            }
            tagsPanel.add(tagLabel);
            /*if(addOrEditItem) {
                final Icon icon = IconLoader.getIcon("/actions/delete.png");
                JButton deleteTagButton = new JButton(icon);
                deleteTagButton.setSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
                deleteTagButton.setBorderPainted(false);
                deleteTagButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        review.deleteTag(tag);
                        tags.remove(tag);
                    }
                });
                tagsPanel.add(deleteTagButton);
            }*/
        }

        final List<String> availableTags = ReviewManager.getInstance(review.getProject()).getAvailableTags();
        tagsField = new TextFieldWithAutoCompletion(review.getProject());
        tagsField.setVariants(availableTags.toArray(new String[availableTags.size()]));
        tagsField.setBackground(Color.WHITE);
        tagsPanel.setFocusable(true);
        tagsField.setRequestFocusEnabled(true);
        tagsField.setFont(new Font("Verdana", Font.PLAIN, 12));
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
                    tags.add(tag);
                    final JLabel comp = new JLabel(tag);
                    if(tag.length() > TAG_LENGTH) {
                        comp.setToolTipText(tag);
                        comp.setText(tag.substring(0, TAG_LENGTH - 3) + "...");
                    }
                    tagsPanel.add(comp);
                    tagsField.setText("");
                    setupTagsPanelSize();
                    tagsPanel.revalidate();
                    tagsPanel.repaint();
                }
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        mainTagsPanel.add(tagsPanel);
        if(addOrEditItem){
            mainTagsPanel.add(tagsField, BorderLayout.SOUTH);
        }
        mainPanel.add(mainTagsPanel, BorderLayout.NORTH);

    }

    private void setupTagsPanelSize() {
        Dimension layoutSize = tagsPanel.getLayout().preferredLayoutSize(tagsPanel);
        int width = mainPanel.getPreferredSize().width;
        int tagsPanelHeight =  layoutSize.height * (int)(((float)layoutSize.width/width) + 1);
        tagsPanel.setPreferredSize(new Dimension(mainPanel.getSize().width,tagsPanelHeight));
    }

    private void setupOKCancel() {
        JPanel OKCancelPanel = new JPanel(new GridLayout(1,2));
        JButton OKButton = new JButton("OK");
        OKCancelPanel.add(OKButton);
        JButton cancelButton = new JButton("Cancel");
        OKCancelPanel.add(cancelButton);

        mainPanel.add(OKCancelPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReviewActionManager.getInstance().disposeActiveBalloon();
            }
        });

        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveReview();
            }
        });
    }

    private EditorTextField createInputField(boolean checkSpelling, boolean oneLine) {
        final EnumSet<EditorCustomization.Feature> enabledFeatures = EnumSet.of(EditorCustomization.Feature.SOFT_WRAP);
        Set<EditorCustomization.Feature> disabledFeatures = EnumSet.of( EditorCustomization.Feature.HORIZONTAL_SCROLLBAR);
        if (checkSpelling) {
          enabledFeatures.add(EditorCustomization.Feature.SPELL_CHECK);
        }
        else {
          disabledFeatures.add(EditorCustomization.Feature.SPELL_CHECK);
        }
        EditorTextFieldProvider service = ServiceManager.getService(review.getProject(), EditorTextFieldProvider.class);
        return service.getEditorField(FileTypes.PLAIN_TEXT.getLanguage(),
                                      review.getProject(),
                                      enabledFeatures,
                                      disabledFeatures);
    }


    public void saveReview() {
        if(!tags.isEmpty()) {
            ReviewManager.getInstance(review.getProject()).addTags(tags);
            review.addTags(tags);
        }

        if(addOrEditItem) {
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
                review.addReviewItem(new ReviewItem(text, (ReviewStatus)statusCombo.getSelectedItem()));
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
        if (addOrEditItem && review.getReviewItems().size() == 1) {
                ReviewManager.getInstance(review.getProject()).placeReview(review);
        } else {
            ReviewManager.getInstance(review.getProject()).changeReview(review);
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
        styleSheet.addRule("html body div.review_item span.highlight {background-color:FFFF00}");
        htmlEditorKit.setStyleSheet(styleSheet);
        itemsText.setEditorKit(htmlEditorKit);
        String result = "";

        final List<ReviewItem> reviewItems = review.getReviewItems();
        if(reviewItems.isEmpty())return;
        for (int i = 0; i < reviewItems.size(); i++) {
            ReviewItem reviewItem = reviewItems.get(i);
            if(!(i == reviewItems.size() - 1 && reviewItem.isMine() && addOrEditItem)) {
                final ReviewManager instance = ReviewManager.getInstance(review.getProject());
                result += instance.getHTMLReport(XmlSerializer.serialize(reviewItem));
            }
        }
        if("".equals(result)) return;
        itemsText.setText(result);
        itemsText.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            protected void hyperlinkActivated(HyperlinkEvent hyperlinkEvent) {
                BrowserUtil.launchBrowser(hyperlinkEvent.getURL().toString());
            }
        });



        JScrollPane pane = ScrollPaneFactory.createScrollPane(itemsText);
        //pane.setPreferredSize(preferredSize);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        mainPanel.add(pane);
    }

    public void requestFocus() {
        if(addOrEditItem) {
            Component component;
            //component = newReviewItemText;
            component = tagsField;
            IdeFocusManager.findInstanceByComponent(component).requestFocus(component, true);
        }
    }
}
