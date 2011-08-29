package ui.forms;

import com.intellij.util.text.DateFormatUtil;
import reviewresult.persistent.ReviewItem;
import ui.reviewtoolwindow.Searcher;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 8/17/11
 * Time: 7:05 PM
 */
public class PreviewItemForm {
    private JPanel mainPanel = new JPanel(new BorderLayout());
    private ReviewItem reviewItem;
    private Searcher searcher;

    private final Highlighter highlighter;

    public PreviewItemForm(Searcher searcher, ReviewItem reviewItem) {
        this.searcher = searcher;
        this.reviewItem = reviewItem;

        JLabel headerLabel = new JLabel(reviewItem.getAuthor()
                                        + " wrote "
                                        + DateFormatUtil.formatPrettyDateTime(reviewItem.getDate()));
        JTextArea itemText = new JTextArea(reviewItem.getText(), 3, 2);
        itemText.setFont(new Font("Verdana", Font.PLAIN, 14));
        highlighter = new BasicTextUI.BasicHighlighter();
        itemText.setHighlighter(highlighter);
        itemText.setEditable(false);
        itemText.setLineWrap(true);
        itemText.setWrapStyleWord(true);
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(itemText);
        //final TitledBorder titledBorder = BorderFactory.createTitledBorder(reviewItem.getStatus().name());
        //titledBorder.setTitleJustification(TitledBorder.RIGHT);
        //mainPanel.setBorder(titledBorder);
        updateSelection();
    }

     public void updateSelection() {
        int searchStart = searcher.getItemSearchResult(reviewItem).first;
        int searchEnd = searcher.getItemSearchResult(reviewItem).second;
        if(highlighter == null) return;
        if(highlighter.getHighlights().length > 0) {
                highlighter.removeAllHighlights();
        }
        if(searchStart >= 0) {
            try {
                highlighter.addHighlight(searchStart,
                                            searchEnd,
                                            new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
            } catch (BadLocationException e) {
               ///
            }
        }
       /* mainPanel.revalidate();
        mainPanel.repaint();*/
    }

    public JPanel getContents() {
        return mainPanel;
    }
}
