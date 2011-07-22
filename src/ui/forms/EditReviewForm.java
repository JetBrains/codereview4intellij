package ui.forms;

import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.ReviewItem;
import reviewresult.ReviewManager;
import ui.reviewpoint.ReviewPoint;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

    private Review newReview;
    private ReviewItemForm reviewItemForm;
    private Balloon balloon;

    public EditReviewForm(final Review review) {
        this.newReview = review;
        itemsPanel.setLayout(new GridLayout(-1, 1));
        JPanel panel = new JPanel(new GridLayout(-1, 1));


        if(review.getName() != null) {
            reviewName.setText(review.getName());
            reviewName.setEnabled(false);
            reviewName.setEditable(false);
        }
        for (ReviewItem reviewItem : review.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(reviewItem);
            panel.add(itemForm.getContent());
        }
        ReviewItem reviewItem = new ReviewItem();

        reviewItemForm = new ReviewItemForm(reviewItem);
        panel.add(reviewItemForm.getContent());
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                balloon.dispose();
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
                if(newReview.getName() == null) {
                    String name = reviewName.getText();
                    if("".equals(name)) {
                        int nameLength = 6;
                        String forcedName = (text.length() > nameLength) ? text.substring(0, nameLength):text;
                        newReview.setName(forcedName);
                    }
                    else {
                        newReview.setName(name);
                    }
                }
                if(review.getReviewItems().isEmpty()) {
                    ReviewPoint reviewPoint = ReviewManager.getInstance(review.getProject()).findOrCreateReviewPoint(review);
                }
                newReview.addReviewItem(item);
                ReviewManager.getInstance(review.getProject()).addReview(newReview);
                balloon.dispose();
            }
        });
    }

    public JComponent getContent() {
        return mainPanel;
    }


    public void setBalloon(Balloon balloon) {
        this.balloon = balloon;
    }
}
