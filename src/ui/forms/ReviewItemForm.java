package ui.forms;

import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileTypes.FileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.ui.EditorCustomization;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.EditorTextFieldProvider;
import com.intellij.util.text.DateFormatUtil;
import reviewresult.persistent.ReviewItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.EnumSet;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 3:07 PM
 */
public class ReviewItemForm {
    private final EditorTextField reviewItemText;
    private JPanel reviewItemContent = new JPanel(new BorderLayout());

    private Project project;
    private final ReviewItem reviewItem;



    public ReviewItemForm(Project project, ReviewItem reviewItem) {
        this.project = project;
        this.reviewItem = reviewItem;

        reviewItemContent.setFocusable(true);

        JLabel headerLabel = new JLabel();
        headerLabel.setText(reviewItem.getAuthor() + " wrote " + DateFormatUtil.formatPrettyDateTime(reviewItem.getDate()));

        reviewItemText = createInputField(true, false);
        reviewItemText.setBackground(Color.WHITE);
        /*final Editor editor = reviewItemText.getEditor();
        if(editor != null) {
            editor.getSettings().setAdditionalPageAtBottom(true);
            editor.getSettings().setAdditionalLinesCount(2);
        }*/
        String text = reviewItem.getText();
        reviewItemText.setFont(new Font("Verdana", Font.PLAIN, 14));
        if(text != null) {
            reviewItemText.setText(text);
        }
        reviewItemText.setMaximumSize(reviewItemText.getPreferredSize());
        reviewItemText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                reviewItemText.setBorder(BorderFactory.createEmptyBorder());
            }
        });

        reviewItemText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                //reviewItemText.setCaretPosition(reviewItemText.getText().length());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if("".equals(reviewItemText.getText())) {
                    setEmptyComment();
                }

            }
        });
        reviewItemContent.add(headerLabel);
        reviewItemContent.add(reviewItemText);
    }

    private EditorTextField createInputField(boolean checkSpelling, boolean oneLine) {
        final EnumSet<EditorCustomization.Feature> enabledFeatures = EnumSet.of(EditorCustomization.Feature.SOFT_WRAP);
        if(!oneLine)
            enabledFeatures.add(EditorCustomization.Feature.ADDITIONAL_PAGE_AT_BOTTOM);
        Set<EditorCustomization.Feature> disabledFeatures = EnumSet.of( EditorCustomization.Feature.HORIZONTAL_SCROLLBAR);
        if (checkSpelling) {
          enabledFeatures.add(EditorCustomization.Feature.SPELL_CHECK);
        }
        else {
          disabledFeatures.add(EditorCustomization.Feature.SPELL_CHECK);
        }
        EditorTextFieldProvider service = ServiceManager.getService(project, EditorTextFieldProvider.class);
        return service.getEditorField(FileTypes.PLAIN_TEXT.getLanguage(),
                                      project,
                                      enabledFeatures,
                                      disabledFeatures);
    }

    public boolean onSave() {
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


    public JPanel getContent() {
        return reviewItemContent;
    }

    private void setEmptyComment() {
        reviewItemText.setBorder(BorderFactory.createEtchedBorder(Color.RED, Color.WHITE));
        reviewItemText.invalidate();
    }


    public void requestFocus() {
        IdeFocusManager.findInstanceByComponent(reviewItemContent).requestFocus(reviewItemText, true);
    }
}
