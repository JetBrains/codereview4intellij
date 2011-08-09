package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.persistent.ReviewItem;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;
import ui.reviewpoint.ReviewPointManager;
import ui.reviewtoolwindow.ReviewView;
import ui.reviewtoolwindow.Searcher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;
import java.util.ArrayList;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 2:27 PM
 */
public class EditReviewForm {
    private JTextField reviewName = new JTextField();

    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel panel = new JPanel(new BorderLayout());
    private JPanel itemsPanel = new JPanel(new BorderLayout());

    private List<ReviewItemForm> reviewItemFormsList;
    private JTextArea newReviewItemText = new JTextArea();

    private Balloon balloon;

    private final Review review;

    public EditReviewForm(final Review review, boolean showNewItem) {

        this.review = review;
        review.setActivated(true);

        JPanel contentPanel = new JPanel(new GridLayout(2, 1));
        resetItemsContent(true);
        contentPanel.add(panel);
        reviewName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewName.setBorder(BorderFactory.createEmptyBorder());
                String reviewText = reviewName.getText();
                if(Character.isDigit(e.getKeyChar()) ||
                   Character.isLetter(e.getKeyChar()) ||
                   Character.isWhitespace(e.getKeyChar()) ) {
                        review.setName(reviewText + e.getKeyChar());
                }
                else {
                   review.setName(reviewText);
                }
            }
        });
        if(showNewItem) {
            JPanel newReviewItemPanel = new JPanel(new GridLayout(1, 1));

            newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, 27));
            reviewName.setFont(new Font("Verdana", Font.PLAIN, 27));

            newReviewItemPanel.add(newReviewItemText);
            JScrollPane newItemScrollPane = ScrollPaneFactory.createScrollPane(newReviewItemPanel);

            contentPanel.add(newItemScrollPane);

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

    public void saveReview() {
        String text = newReviewItemText.getText().trim();
        if ("".equals(text)) {
            newReviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
            newReviewItemText.invalidate();
            return;
        }

        String name = reviewName.getText();
        if ("".equals(name)) {
            int nameLength = 6;
            String forcedName = (text.length() > nameLength) ? text.substring(0, nameLength) : text;
            review.setName(forcedName);
        }

        if (review.getReviewItems().isEmpty()) {
            ReviewManager.getInstance(review.getProject()).addReview(review);
        }
        review.addReviewItem(new ReviewItem(text, ReviewStatus.COMMENT));
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

    public JPanel getItemsContent(boolean editable) {
        panel.setFocusable(true);
        return panel;
    }

    private void resetItemsContent(boolean editable) {
        reviewItemFormsList = new ArrayList<ReviewItemForm>();
        panel.removeAll();
        itemsPanel.removeAll();
        itemsPanel.setLayout(new GridLayout(-1, 1));
        //reviewName.setFont(new Font("Verdana", Font.PLAIN, 14));
        if(review.getName() != null) {
            //reviewName.setFont(new Font("Verdana", Font.PLAIN, 14));
            reviewName.setText(review.getName());
        }

        for (ReviewItem reviewItem : review.getReviewItems()) {
            ReviewItemForm itemForm = new ReviewItemForm(reviewItem, Searcher.getInstance(review.getProject()));
            itemsPanel.add(itemForm.getContent(editable));
            reviewItemFormsList.add(itemForm);
        }

        if(!reviewItemFormsList.isEmpty()) {
            panel.add(reviewName, BorderLayout.NORTH);
            JScrollPane itemScrollPane = ScrollPaneFactory.createScrollPane(itemsPanel);
            panel.add(itemScrollPane);
        } else {
            panel.add(reviewName);
        }
    }

    public Component getItemTextField() {
        //newReviewItemText.setFont(new Font("Verdana", Font.PLAIN, 14));
        return newReviewItemText;
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
