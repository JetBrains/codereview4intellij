package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.panels.VerticalBox;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;
import reviewresult.persistent.ReviewItem;
import ui.reviewtoolwindow.ReviewView;
import ui.reviewtoolwindow.Searcher;

import javax.help.NoMerge;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 2:27 PM
 */
public class EditReviewForm {
    private JTextField reviewName = new JTextField();

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel panel = new JPanel(new BorderLayout());
    private Box itemsPanel = new VerticalBox();

    private List<ReviewItemForm> reviewItemFormsList;
    private JTextArea newReviewItemText = new JTextArea(3, 2);

    private Balloon balloon;

    private final Review review;
    private boolean showNewItem;

    public EditReviewForm(final Review review, boolean showNewItem) {

        this.review = review;
        this.showNewItem = showNewItem;

        JPanel contentPanel = new JPanel(new BorderLayout());
        resetItemsContent(true);
        contentPanel.add(panel);
        reviewName.setVerifyInputWhenFocusTarget(true);
        reviewName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewName.setBorder(BorderFactory.createEmptyBorder());
            }
        });
        reviewName.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                reviewName.setCaretPosition(reviewName.getText().length());
            }
        });
        if(showNewItem) {
            JPanel newReviewItemPanel = new JPanel(new GridLayout(1, 1));

            newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, 27));


            reviewName.setFont(new Font("Verdana", Font.PLAIN, 27));

            newReviewItemPanel.add(newReviewItemText);
            newReviewItemText.requestFocus();

            newReviewItemText.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    newReviewItemText.setBorder(BorderFactory.createEmptyBorder());
                    newReviewItemText.invalidate();
                }
            });

            JScrollPane newItemScrollPane = ScrollPaneFactory.createScrollPane(newReviewItemPanel);

            contentPanel.add(newItemScrollPane, BorderLayout.SOUTH);

            JPanel OKCancelPanel = new JPanel(new GridLayout(1,2));
            JButton OKButton = new JButton("OK");
            OKCancelPanel.add(OKButton);
            JButton cancelButton = new JButton("Cancel");
            OKCancelPanel.add(cancelButton);

            mainPanel.add(contentPanel);
            mainPanel.add(OKCancelPanel, BorderLayout.SOUTH);

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

    }



    public boolean saveItems() {
        for(ReviewItemForm form : reviewItemFormsList) {
            if(!form.onExit()) return false;
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
            if (review.getReviewItems().isEmpty()) {
                ReviewManager.getInstance(review.getProject()).addReview(review);
            }
            review.addReviewItem(new ReviewItem(text, ReviewStatus.COMMENT));
        }
        review.setName(name);
        balloon.dispose();
        review.setActivated(false);
        ReviewView reviewView = ServiceManager.getService(review.getProject(), ReviewView.class);
        reviewView.updateUI();
    }

    public JComponent getContent() {
        mainPanel.setFocusable(true);
        return mainPanel;
    }


    public void setBalloon(Balloon balloon) {
        this.balloon = balloon;
    }

    public JPanel getItemsContent() {
        panel.setFocusable(true);
        itemsPanel.setFocusable(true);
        panel.setMaximumSize(panel.getPreferredSize());
        return panel;
    }

    private void resetItemsContent(boolean editable) {
        reviewItemFormsList = new ArrayList<ReviewItemForm>();
        panel =  new JPanel(new BorderLayout());
        itemsPanel.removeAll();
        if(review.getName() != null) {
            reviewName.setText(review.getName());
            reviewName.requestFocus();
        }

        for (ReviewItem reviewItem : review.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(reviewItem, Searcher.getInstance(review.getProject()));
            itemsPanel.add(itemForm.getContent(editable));
            reviewItemFormsList.add(itemForm);
            itemForm.getItemTextField().requestFocus();
        }

        if(!reviewItemFormsList.isEmpty()) {
            panel.add(reviewName, BorderLayout.NORTH);
            JScrollPane itemScrollPane = ScrollPaneFactory.createScrollPane(itemsPanel, JBScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            itemScrollPane.setMaximumSize(itemScrollPane.getPreferredSize());
            Box surroundingPanel = new VerticalBox();
            surroundingPanel.add(itemScrollPane);
            panel.add(surroundingPanel);
        } else {
            panel.add(reviewName);
        }
    }

    public Component getNameTextField() {
        return reviewName;
    }

    public void updateSelection() {
        for(ReviewItemForm form : reviewItemFormsList) {
            form.updateSelection();
        }
    }

    public Component getLastExistingTextField() {
        return reviewItemFormsList.get(reviewItemFormsList.size() - 1).getItemTextField();
    }

    public Component getNewItemTextField() {
        return newReviewItemText;
    }

}
