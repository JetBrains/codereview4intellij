package ui.reviewtoolwindow;

import reviewresult.Review;
import ui.forms.EditReviewForm;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 8/8/11
 * Time: 11:32 AM
 */
public class ReviewsPreviewPanel extends JPanel {

    private EditReviewForm editReviewForm;

    public ReviewsPreviewPanel() {
        super(new BorderLayout());
        add(new JLabel("Select element to preview from a tree"));
    }

    public void update(Review review) {
        removeAll();
        if(review.isValid()) {
            editReviewForm = new EditReviewForm(review, false);
            //JScrollPane previewScrollPane = ScrollPaneFactory.createScrollPane();
            /*previewScrollPane.*/add(editReviewForm.getItemsContent());
            //add(previewScrollPane);
            //this.revalidate();
            //this.repaint();
        } else {
            add(new JLabel("This element became invalid"));
        }
        revalidate();
    }

    public void updateSelection() {
        editReviewForm.updateSelection();
    }
}
