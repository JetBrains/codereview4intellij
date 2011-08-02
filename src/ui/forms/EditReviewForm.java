package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.ReviewItem;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;
import ui.reviewpoint.ReviewPointManager;
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
    private JTextArea newReviewItemText;
    private JScrollPane newItemScrollPane;

    private List<ReviewItemForm> reviewItemFormsList;
    private Balloon balloon;
    private JPanel panel;
    private final Review review;

    public EditReviewForm(final Review review) {
        this.review = review;
        review.setActivated(true);
        reviewName.setFont(new Font("Verdana", Font.PLAIN, 14));
        resetItemsContent(true);
        JScrollPane itemsScrollPane = ScrollPaneFactory.createScrollPane(panel);
        itemsScrollPane.setMaximumSize(itemsScrollPane.getPreferredSize());
        itemsPanel.add(itemsScrollPane);
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
                //ReviewItem item = reviewItemForm.getReviewItem();
                String text = newReviewItemText.getText().trim();
                if("".equals(text)) {
                    newReviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
                    newReviewItemText.invalidate();
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
                    ReviewManager.getInstance(review.getProject()).addReview(review);
                    ReviewPointManager.getInstance(review.getProject()).createReviewPoint(review);
                }
                review.addReviewItem(new ReviewItem(text, ReviewStatus.COMMENT));
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
            reviewName.setFont(new Font("Verdana", Font.PLAIN, 14));
            reviewName.setText(review.getName());
        }
        for (ReviewItem reviewItem : review.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(reviewItem);
            panel.add(itemForm.getContent(editable));
            reviewItemFormsList.add(itemForm);
        }
    }

    public Component getItemTextField() {
        newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, 14));
        //newReviewItemText.
        return newReviewItemText;
    }

    public Component getNameTextField() {
        reviewName.setFont(new Font("Verdana", Font.PLAIN, 14));
        return reviewName;
    }
    public void updateSelection() {
        for(ReviewItemForm form : reviewItemFormsList) {
            form.updateSelection();
        }
    }

    private void createUIComponents() {
        newItemScrollPane = ScrollPaneFactory.createScrollPane();
        newItemScrollPane.setMaximumSize(newItemScrollPane.getPreferredSize());
    }
}
