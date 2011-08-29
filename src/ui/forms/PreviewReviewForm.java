package ui.forms;

import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.persistent.ReviewItem;
import ui.reviewtoolwindow.Searcher;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 8/17/11
 * Time: 7:04 PM
 */
public class PreviewReviewForm {
    private final JPanel mainPanel = new JPanel(new BorderLayout());
    final private static int TAG_LENGTH  = 20;
    private List<PreviewItemForm> reviewItemFormsList = new ArrayList<PreviewItemForm>();

    public PreviewReviewForm(Review review) {
        setupTagPanel(review.getReviewBean().getTags());
        setupContextPanel(review);
        JPanel panel = new JPanel(new GridLayout(0, 1));
        for(ReviewItem reviewItem : review.getReviewItems()) {
            final Searcher instance = Searcher.getInstance(review.getProject());
            if(instance.containsReviewItem(reviewItem)) {
                final PreviewItemForm previewItemForm = new PreviewItemForm(instance, reviewItem);
                panel.add(previewItemForm.getContents());
                reviewItemFormsList.add(previewItemForm);
            }
        }
        JScrollPane pane = ScrollPaneFactory.createScrollPane(panel);
        mainPanel.add(pane);
    }

    private void setupContextPanel(Review review) {
        JTextArea contextField = new JTextArea();

        final String lineBefore = review.getReviewBean().getContext().getLineBefore();
        final String line = review.getReviewBean().getContext().getLine();
        final String lineAfter = review.getReviewBean().getContext().getLineAfter();
        contextField.setText(lineBefore + line + lineAfter);
        contextField.setEditable(false);
        final Highlighter highlighter;
        highlighter = new BasicTextUI.BasicHighlighter();
        contextField.setHighlighter(highlighter);
        if(highlighter.getHighlights().length > 0) {
                highlighter.removeAllHighlights();
        }

        final int beforeLength = lineBefore.length();
        final int lineLength = review.getReviewBean().getContext().getLine().length();
        try {
            highlighter.addHighlight(beforeLength,
                                     beforeLength + lineLength,
                                     new DefaultHighlighter.DefaultHighlightPainter(new Color(224,255,204)));
        } catch (BadLocationException e) {
            ///
        }
       // panel.add(contextField);
        mainPanel.add(contextField, BorderLayout.SOUTH);
    }

    public void updateSelection() {
        for(PreviewItemForm form : reviewItemFormsList) {
            form.updateSelection();
        }
    }

    public JComponent getContent() {
        return ScrollPaneFactory.createScrollPane(mainPanel,
                                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                                    JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    }

    private void setupTagPanel(List<String> tags) {
        JPanel tagsPanel = new JPanel(new GridLayout(-1, 1));
        for(String tag : tags) {
            final JLabel comp = new JLabel(tag);
            if(tag.length() > TAG_LENGTH) {
                comp.setToolTipText(tag);
                comp.setText(tag.substring(0,TAG_LENGTH - 3) + "...");
            }

            tagsPanel.add(comp);
        }
        mainPanel.add(tagsPanel, BorderLayout.NORTH);
    }
}
