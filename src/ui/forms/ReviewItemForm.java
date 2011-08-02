package ui.forms;

import com.intellij.openapi.wm.IdeFocusManager;
import reviewresult.ReviewItem;
import reviewresult.ReviewStatus;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicTextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;
import java.util.jar.Attributes;

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

    private Highlighter highlighter;
    public ReviewItemForm(ReviewItem data) {
        reviewItem = data;
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
        updateSelection();
        reviewItemText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewItemText.setBorder(BorderFactory.createEmptyBorder());
                if(Character.isDigit(e.getKeyChar()) ||
                   Character.isLetter(e.getKeyChar()) ||
                   Character.isWhitespace(e.getKeyChar()) ) {
                        reviewItem.setText(reviewItemText.getText() + e.getKeyChar());
                }
            }
        });
    }

    public void updateSelection() {
        int searchStart = reviewItem.getSearchStart();
        if(searchStart >= 0) {
            if(highlighter.getHighlights().length > 0) {
                highlighter.removeAllHighlights();
            }
            if(highlighter != null) {
                try {
                    highlighter.addHighlight(searchStart,
                                                reviewItem.getSearchEnd(),
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
        reviewItemContent.invalidate();
    }

    public Component getItemTextField() {
        return reviewItemText;
    }
}
