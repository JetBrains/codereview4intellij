package ui.reviewtoolwindow;

import reviewresult.Review;
import ui.forms.EditReviewForm;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 8/8/11
 * Time: 11:32 AM
 */
class ReviewsPreviewPanel extends JPanel {
    public ReviewsPreviewPanel() {
        super(new BorderLayout());
        add(new JLabel("Select element to preview from a tree"));
    }

    public void update(Review review) {
        removeAll();
        if(review.isValid()) {
            EditReviewForm previewReviewForm = new EditReviewForm(review, false, false, false);
            add(previewReviewForm.getContent());
            setupContextPanel(review);

        } else {
            add(new JLabel("This element became invalid"));
        }
        revalidate();
        repaint();
    }


     private void setupContextPanel(Review review) {
        JTextArea contextField = new JTextArea();

        final String lineBefore = review.getLineBefore();
        final String line = review.getLine();
        final String lineAfter = review.getLineAfter();
        contextField.setText(lineBefore + line + lineAfter);

        contextField.setEditable(false);

        final Highlighter highlighter;
        highlighter = new BasicTextUI.BasicHighlighter();
        contextField.setHighlighter(highlighter);

        if(highlighter.getHighlights().length > 0) {
                highlighter.removeAllHighlights();
        }

        final int beforeLength = lineBefore.length();
        final int lineLength = line.length();
        try {
            highlighter.addHighlight(beforeLength,
                                     beforeLength + lineLength,
                                     new DefaultHighlighter.DefaultHighlightPainter(new Color(224,255,204)));
        } catch (BadLocationException e) {
            // todo
        }

        add(contextField, BorderLayout.SOUTH);
    }
}
