package ui.forms;

import com.intellij.openapi.wm.IdeFocusManager;
import reviewresult.ReviewItem;
import reviewresult.ReviewStatus;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private ReviewItem reviewItem;

    public ReviewItemForm(ReviewItem data, boolean focused) {
        reviewItem = data;
        data.setAuthor(System.getProperty("user.name"));
        authorTextField.setText(data.getAuthor());
        dateTextField.setText(data.getDate().toString());
        String text = data.getText();
        if(text != null) {
            reviewItemText.setText(text);
        }
        //IdeFocusManager.getInstance().requestFocus()
        reviewItemText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewItemText.setBorder(BorderFactory.createEmptyBorder());
                reviewItem.setText(reviewItemText.getText() + e.getKeyChar());
            }
        });
        /*if(focused) {
            reviewItemText.requestFocusInWindow();
        } */
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
