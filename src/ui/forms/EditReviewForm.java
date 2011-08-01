package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.ReviewItem;
import reviewresult.ReviewManager;
import ui.reviewtoolwindow.ReviewView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.ArrayList;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 2:27 PM
 */
public class EditReviewForm {
    private JTextField reviewName;

    private JButton OKButton;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JPanel itemsPanel;

    private List<ReviewItemForm> reviewItemFormsList;
    private ReviewItemForm reviewItemForm;
    private Balloon balloon;
    private JPanel panel;
    private final Review review;
    private JPanel content;

    public EditReviewForm(final Review review) {
        this.review = review;
        review.setActivated(true);
        resetItemsContent(true);
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                balloon.dispose();
                review.setActivated(false);
            }
        });
        JScrollPane itemsScrollPane = ScrollPaneFactory.createScrollPane(panel);
        itemsScrollPane.setMaximumSize(itemsScrollPane.getPreferredSize());
        itemsPanel.add(itemsScrollPane);
        OKButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ReviewItem item = reviewItemForm.getReviewItem();
                String text = item.getText().trim();
                if("".equals(text)) {
                            reviewItemForm.setEmptyComment();
                            return;
                }

                String name = reviewName.getText();
                if("".equals(name)) {
                    int nameLength = 6;
                    String forcedName = (text.length() > nameLength) ? text.substring(0, nameLength):text;
                    review.setName(forcedName);
                }
                else {
                    review.setName(name);
                }
                if(review.getReviewItems().isEmpty()) {
                    ReviewManager.getInstance(review.getProject()).createReviewPoint(review);
                    //ReviewManager.getInstance(newReview.getProject()).addReview(newReview);
                }
                review.addReviewItem(item);
                balloon.dispose();
                review.setActivated(false);
                ReviewView reviewView = ServiceManager.getService(review.getProject(), ReviewView.class);
                reviewView.updateUI();
            }
        });
    }

    public JComponent getContent() {
        mainPanel.setFocusable(true);

        return mainPanel;
    }


    public void setBalloon(Balloon balloon) {
        this.balloon = balloon;
    }

    public JPanel getItemsContent() {
        resetItemsContent(false);
        return panel;
    }

    private void resetItemsContent(boolean editable) {
        reviewItemFormsList = new ArrayList<ReviewItemForm>();
        itemsPanel.setLayout(new GridLayout(-1, 1));
        panel = new JPanel(new GridLayout(-1, 1));
        if(review.getName() != null) {
            reviewName.setText(review.getName());
        }
        for (ReviewItem reviewItem : review.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(reviewItem, true);
            panel.add(itemForm.getContent(editable));
            reviewItemFormsList.add(itemForm);
        }
        ReviewItem reviewItem = new ReviewItem();
        reviewItemForm = new ReviewItemForm(reviewItem, true);
        content = reviewItemForm.getContent(true);
        panel.add(content);
    }

    public Component getItemTextField() {
        return reviewItemForm.getItemTextField();
    }

    public Component getNameTextField() {
        return reviewName;
    }
    public void updateSelection() {
        for(ReviewItemForm form : reviewItemFormsList) {
            form.updateSelection();
        }
    }
}
