package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldProvider;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.panels.VerticalBox;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
    private final EditorTextField reviewName;

    private final JPanel mainPanel = new JPanel(new BorderLayout());
    private final Box itemsPanel = new VerticalBox();

    private List<ReviewItemForm> reviewItemFormsList;

    private EditorTextField newReviewItemText;

    private Balloon balloon;

    private final Review review;
    private final boolean showNewItem;

    public EditReviewForm(final Review review, boolean addItem, boolean spellCheck) {
        this.review = review;
        this.showNewItem = addItem;

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
        resetItemsContent();

        if(addItem) {
            newReviewItemText = createInputField(spellCheck, false);
            newReviewItemText.setBackground(Color.WHITE);
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
            JPanel OKCancelPanel = new JPanel(new GridLayout(1,2));
            JButton OKButton = new JButton("OK");
            OKCancelPanel.add(OKButton);
            JButton cancelButton = new JButton("Cancel");
            OKCancelPanel.add(cancelButton);

           // mainPanel.add(OKCancelPanel, BorderLayout.SOUTH);

            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    balloon.dispose();
                    review.setActivated(false);
                }
            });

            OKButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveReview();
                }
            });
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
                balloon.hide();
            }
        });
    }

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

    private boolean saveItems() {
        for(ReviewItemForm form : reviewItemFormsList) {
            if(!form.onSave()) return false;
        }
        return true;
    }

    public void saveReview() {
        if(!saveItems()) {
            mainPanel.updateUI();
            return;
        }

        String name = reviewName.getText();

        if(showNewItem) {
            String text = newReviewItemText.getText().trim();
            if ("".equals(text)) {
                newReviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
                newReviewItemText.invalidate();
                return;
            }

            review.setName(name);
            if ("".equals(name)) {
               int nameLength = 6;
               name = (text.length() > nameLength) ? text.substring(0, nameLength) : text;
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
        review.setName(name);
        balloon.dispose();
        review.setActivated(false);
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


    public void setBalloon(Balloon balloon) {
        this.balloon = balloon;
    }

    private void resetItemsContent() {
        reviewItemFormsList = new ArrayList<ReviewItemForm>();
        itemsPanel.removeAll();

        for (ReviewItem reviewItem : review.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(review.getProject(), reviewItem);
            itemsPanel.add(itemForm.getContent());
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
            if(review.getName() == null || "".equals(review.getName())) {
                component = reviewName;
            } else {
                component = newReviewItemText;
            }
            IdeFocusManager.findInstanceByComponent(component).requestFocus(component, true);
        } else {
            reviewItemFormsList.get(reviewItemFormsList.size() - 1).requestFocus();
        }
    }
}
