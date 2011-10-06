package ui.reviewtoolwindow;

import com.intellij.util.ui.UIUtil;
import reviewresult.Review;
import ui.forms.EditReviewForm;
import utils.ReviewsBundle;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 8/8/11
 * Time: 11:32 AM
 */
class ReviewsPreviewPanel extends JPanel {
    public ReviewsPreviewPanel() {
        super(new BorderLayout());
        add(new JLabel(ReviewsBundle.message("reviews.selectElementToPreview")));
    }

    public void update(Review review) {
        removeAll();
        if(review.isValid()) {
            EditReviewForm previewReviewForm = new EditReviewForm(review, false, false, false);
            add(previewReviewForm.getContent());
            setupContextPanel(review);

        } else {
            add(new JLabel(ReviewsBundle.message("reviews.elementBecameInvalid")));
        }
        revalidate();
        repaint();
    }


     private void setupContextPanel(Review review) {
        JEditorPane contextField = new JEditorPane();
        final HTMLEditorKit htmlEditorKit = UIUtil.getHTMLEditorKit();
        final StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
        styleSheet.addRule(ReviewsBundle.message("reviews.viewContextHighlightRule"));
        styleSheet.addRule("div.context {font-size:12pt}");
        htmlEditorKit.setStyleSheet(styleSheet);
        contextField.setEditorKit(htmlEditorKit);
        final String lineBefore = review.getLineBefore();
        final String line = review.getLine();
        final String lineAfter = review.getLineAfter();
        contextField.setText("<div class=context>" + lineBefore + line +  lineAfter + "</div>");
        contextField.setEditable(false);
        add(contextField, BorderLayout.SOUTH);
    }
}
