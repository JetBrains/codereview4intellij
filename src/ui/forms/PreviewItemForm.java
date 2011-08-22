package ui.forms;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.text.DateFormatUtil;
import reviewresult.persistent.ReviewItem;
import ui.reviewtoolwindow.Searcher;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

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

        JLabel headerLabel = new JLabel();
        headerLabel.setText(reviewItem.getAuthor() + " wrote " + DateFormatUtil.formatPrettyDateTime(reviewItem.getDate()));
        JTextArea itemText = new JTextArea();
        itemText.setText(reviewItem.getText());
        itemText.setFont(new Font("Verdana", Font.PLAIN, 14));
        highlighter = new BasicTextUI.BasicHighlighter();
        itemText.setHighlighter(highlighter);
        itemText.setEditable(false);
        itemText.setLineWrap(true);
        itemText.setWrapStyleWord(true);
        mainPanel.add(headerLabel, BorderLayout.NORTH);
        mainPanel.add(itemText);
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
    }

    public JPanel getContents() {
        return mainPanel;
    }
}
