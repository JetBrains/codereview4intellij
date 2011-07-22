package ui.forms;

import reviewresult.ReviewItem;
import reviewresult.ReviewStatus;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Date;

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

    public ReviewItemForm(ReviewItem data) {
        data.setAuthor(System.getProperty("user.name"));
        authorTextField.setText(data.getAuthor());
        dateTextField.setText(data.getDate().toString());
        String text = data.getText();
        if(text != null) {
            reviewItemText.setText(text);
        }
        reviewItemText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewItemText.setBorder(BorderFactory.createEmptyBorder());
            }
        });
    }


    public JPanel getContent() {
        return reviewItemContent;
    }

    public ReviewItem getReviewItem() {
        return new ReviewItem(reviewItemText.getText(), ReviewStatus.COMMENT);
    }

    public void setEmptyComment() {
        reviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
        reviewItemContent.invalidate();
    }
}
