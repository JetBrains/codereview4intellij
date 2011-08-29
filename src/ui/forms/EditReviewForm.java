package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.*;
import com.intellij.ui.components.panels.VerticalBox;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import ui.actions.ReviewActionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 2:27 PM
 */
public class EditReviewForm {
    //private final EditorTextField reviewName;

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final Box itemsPanel = new VerticalBox();
    private final EditorComboBox tagsComboBox = new EditorComboBox("");
    private List<EditReviewItemForm> reviewItemFormsList;

    private EditorTextField newReviewItemText;

    private List<String> tags = new ArrayList<String>();

    private final Review review;
    private final boolean showNewItem;
    private static final int TAG_LENGTH = 20;
    private TextFieldWithAutoCompletion tagsField;

    public EditReviewForm(final Review review, boolean addItem, boolean spellCheck) {
        this.review = review;
        this.showNewItem = addItem;

        //setupReviewName(spellCheck);
        setupTags();
        setupItemsContent();

        if(addItem) {
            setupNewItem(spellCheck);
            //setupOKCancel();
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
    }

    private void setupNewItem(boolean spellCheck) {
        newReviewItemText = createInputField(spellCheck, false);
        newReviewItemText.setBackground(Color.WHITE);
        newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, 16));
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


        JPanel newReviewItemPanel = new JPanel(new GridLayout(1, 1));
        newReviewItemPanel.add(newReviewItemText);
        mainPanel.add(newReviewItemText, BorderLayout.SOUTH);
    }

    private void setupTags() {
        final JPanel tagsPanel = new JPanel(new GridLayout(0, 1));
        for(String tag : review.getReviewBean().getTags()) {
            final JButton comp = new JButton(tag);
            comp.setEnabled(false);
            comp.setBorderPainted(false);
            if(tag.length() > TAG_LENGTH) {
                comp.setToolTipText(tag);
                comp.setText(tag.substring(0,TAG_LENGTH - 3) + "...");
            }
            tagsPanel.add(comp);
        }
        final List<String> availableTags = ReviewManager.getInstance(review.getProject()).getAvailableTags();
        tagsField = new TextFieldWithAutoCompletion(review.getProject());
        tagsField.setVariants(availableTags.toArray(new String[availableTags.size()]));
        tagsField.setBackground(Color.WHITE);
        tagsPanel.setFocusable(true);
        tagsField.setRequestFocusEnabled(true);
        tagsField.registerKeyboardAction(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final String text = tagsField.getText();
                tags.add(text);
                tagsPanel.add(new JLabel(text));
                tagsField.setText("");
                tagsPanel.revalidate();
                tagsPanel.repaint();
                mainPanel.updateUI();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        if(showNewItem){
            tagsPanel.add(tagsField);
        }
        mainPanel.add(tagsPanel, BorderLayout.NORTH);

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

 /*   private void setupReviewName(boolean spellCheck) {
        reviewName = createInputField(spellCheck, true);
        reviewName.setBackground(Color.WHITE);
        reviewName.setOneLineMode(true);
        reviewName.setFont(new Font("Verdana", Font.PLAIN, 14));
        reviewName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewName.setBorder(BorderFactory.createEmptyBorder());
            }
        });
        if(review.getName() != null) {
            reviewName.setText(review.getName());
        }

        mainPanel.add(reviewName, BorderLayout.NORTH);
    }*/

    private EditorTextField createInputField(boolean checkSpelling, boolean oneLine) {
        final EnumSet<EditorCustomization.Feature> enabledFeatures = EnumSet.of(EditorCustomization.Feature.SOFT_WRAP);
        if(!oneLine)
            enabledFeatures.add(EditorCustomization.Feature.ADDITIONAL_PAGE_AT_BOTTOM);
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

    private boolean canSaveItems() {
        for(EditReviewItemForm form : reviewItemFormsList) {
            if(!form.canSave()) return false;
        }
        return true;
    }

    public void saveReview() {
        if(!canSaveItems()) {
            mainPanel.updateUI();
            return;
        }
        if(!tags.isEmpty()) {
            ReviewManager.getInstance(review.getProject()).addTags(tags);
            review.addTags(tags);
        }


        if(showNewItem) {
            String text = newReviewItemText.getText().trim();
            if ("".equals(text)) {
                newReviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
                newReviewItemText.invalidate();
                return;
            }

            review.addReviewItem(new ReviewItem(text));
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
        if (showNewItem && review.getReviewItems().size() == 1) {
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
        reviewItemFormsList = new ArrayList<EditReviewItemForm>();
        itemsPanel.removeAll();

        for (final ReviewItem reviewItem : review.getReviewItems()) {
            final EditReviewItemForm itemForm = new EditReviewItemForm(review.getProject(), reviewItem);
            final JPanel deleteItemsPanel = new JPanel(new BorderLayout());
            JButton deleteReviewItemButton = new JButton("x");
            deleteReviewItemButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(review.getReviewBean().getReviewItems().size() > 1) {
                        review.getReviewBean().removeReviewItem(reviewItem);
                        reviewItemFormsList.remove(itemForm);
                        itemsPanel.remove(deleteItemsPanel);
                        itemsPanel.revalidate();
                        itemsPanel.repaint();
                    } else {
                        ReviewActionManager.getInstance().disposeActiveBalloon();
                        ReviewManager.getInstance(review.getProject()).removeReview(review);
                    }
                }
            });
            if(review.getReviewBean().getReviewItems().size() > 1) {
                deleteItemsPanel.add(deleteReviewItemButton, BorderLayout.NORTH);
            }
            deleteItemsPanel.add(itemForm.getContent());
            itemsPanel.add(deleteItemsPanel);
            reviewItemFormsList.add(itemForm);
            itemForm.requestFocus();
        }

        if(!reviewItemFormsList.isEmpty()) {
            mainPanel.add(itemsPanel);
        }
    }

    public void requestFocus() {
        if(showNewItem) {
            Component component;
            //component = newReviewItemText;
            component = tagsField;
            IdeFocusManager.findInstanceByComponent(component).requestFocus(component, true);
        } else {
            reviewItemFormsList.get(reviewItemFormsList.size() - 1).requestFocus();
        }
    }
}
