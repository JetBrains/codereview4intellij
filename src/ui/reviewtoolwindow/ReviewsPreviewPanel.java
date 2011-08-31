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
class ReviewsPreviewPanel extends JPanel {

    private EditReviewForm previewReviewForm;

    public ReviewsPreviewPanel() {
        super(new BorderLayout());
        add(new JLabel("Select element to preview from a tree"));
    }

    public void update(Review review) {
        removeAll();
        if(review.isValid()) {
            previewReviewForm = new EditReviewForm(review, false, false);
            add(previewReviewForm.getContent());

        } else {
            add(new JLabel("This element became invalid"));
        }
        revalidate();
        repaint();
    }

    /*public void updateSelection() {
        if(previewReviewForm != null) {
            previewReviewForm.updateSelection();
        }
    }*/
}
