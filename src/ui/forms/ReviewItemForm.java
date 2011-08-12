package ui.forms;

import reviewresult.persistent.ReviewItem;
import ui.reviewtoolwindow.Searcher;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 3:07 PM
 */
public class ReviewItemForm {
    private JTextField authorTextField;
    private JTextField dateTextField;
    private JTextArea reviewItemText;
    private JPanel reviewItemContent;
    private ReviewItem reviewItem;
    private Searcher searcher;

    private Highlighter highlighter;
    public ReviewItemForm(ReviewItem data, Searcher searcher) {
        reviewItemContent.setFocusable(true);
        reviewItem = data;
        this.searcher = searcher;
        authorTextField.setText(data.getAuthor());
        dateTextField.setText(data.getDate().toString());
        String text = data.getText();
        reviewItemText.setBorder(BorderFactory.createEmptyBorder());
        reviewItemText.setFont(new Font("Verdana", Font.PLAIN, 14));
        if(text != null) {
            reviewItemText.setText(text);
        }
        highlighter = new BasicTextUI.BasicHighlighter();
        reviewItemText.setHighlighter(highlighter);
        reviewItemText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewItemText.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        reviewItemText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                reviewItemText.setCaretPosition(reviewItemText.getText().length());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if("".equals(reviewItemText.getText())) {
                    setEmptyComment();
                }

            }
        });
        updateSelection();
    }

    public boolean onExit() {
        String text = reviewItemText.getText();
        if("".equals(text)) {
            setEmptyComment();
            return false;
        }
        else {
            reviewItem.setText(text);
            return true;
        }
    }

    public void updateSelection() {
        int searchStart = searcher.getItemSearchResult(reviewItem).first;
        int searchEnd = searcher.getItemSearchResult(reviewItem).second;
        if(searchStart >= 0) {
            if(highlighter.getHighlights().length > 0) {
                highlighter.removeAllHighlights();
            }
            if(highlighter != null) {
                try {
                    highlighter.addHighlight(searchStart,
                                                searchEnd,
                                                new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW));
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public JPanel getContent(boolean editable) {
        reviewItemText.setEditable(editable);
        return reviewItemContent;
    }

    public ReviewItem getReviewItem() {
        return reviewItem;
    }

    public void setEmptyComment() {
        reviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
        reviewItemText.invalidate();
    }

    public Component getItemTextField() {
        return reviewItemText;
    }
}
