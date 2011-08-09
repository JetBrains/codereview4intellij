package ui.reviewtoolwindow;

import com.intellij.execution.impl.JarProgramPatcher;
import com.intellij.ui.ScrollPaneFactory;
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
    public ReviewsPreviewPanel() {
        super(new BorderLayout());
        add(new JLabel("Select element to preview from a tree"));
    }

    public void update(Review review) {
        removeAll();
        if(review.isValid()) {
            EditReviewForm editReviewForm = new EditReviewForm(review, false);
            JScrollPane previewScrollPane = ScrollPaneFactory.createScrollPane();
            /*previewScrollPane.*/add(editReviewForm.getItemsContent(false));
            //add(previewScrollPane);
            //this.revalidate();
            //this.repaint();
        } else {
            add(new JLabel("This element became invalid"));
        }
        revalidate();
    }
}
