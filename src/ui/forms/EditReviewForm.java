package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.ReviewItem;
import reviewresult.ReviewManager;
import ui.reviewpoint.ReviewPoint;
import ui.reviewtoolwindow.ReviewView;

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
    private JPanel panel;

    public EditReviewForm(final Review review) {
        this.newReview = review;

    }

    public void setNewReview(Review newReview) {
        this.newReview = newReview;
    }

    public JComponent getContent() {
        itemsPanel.setLayout(new GridLayout(-1, 1));
        createItemsContent();
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

                String name = reviewName.getText();
                if("".equals(name)) {
                    int nameLength = 6;
                    String forcedName = (text.length() > nameLength) ? text.substring(0, nameLength):text;
                    newReview.setName(forcedName);
                }
                else {
                    newReview.setName(name);
                }
                if(newReview.getReviewItems().isEmpty()) {
                    ReviewManager.getInstance(newReview.getProject()).createReviewPoint(newReview);
                }
                newReview.addReviewItem(item);
                ReviewManager.getInstance(newReview.getProject()).addReview(newReview);
                balloon.dispose();
                ReviewView reviewView = ServiceManager.getService(newReview.getProject(), ReviewView.class);
                reviewView.updateUI();
            }
        });
        return mainPanel;
    }


    public void setBalloon(Balloon balloon) {
        this.balloon = balloon;
    }

    public JPanel getItemsContent() {
        createItemsContent();
        return panel;
    }

    private void createItemsContent() {
        panel = new JPanel(new GridLayout(-1, 1));
        if(newReview.getName() != null) {
            reviewName.setText(newReview.getName());
            //reviewName.setEnabled(false);
            //reviewName.setEditable(false);
        }
        for (ReviewItem reviewItem : newReview.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(reviewItem);
            panel.add(itemForm.getContent());
        }
    }
}
