package ui.forms;

import com.intellij.ui.ScrollPaneFactory;
import reviewresult.Review;
import reviewresult.persistent.ReviewItem;
import ui.reviewtoolwindow.Searcher;

import javax.swing.*;
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

    private List<PreviewItemForm> reviewItemFormsList = new ArrayList<PreviewItemForm>();


    public PreviewReviewForm(Review review) {
        JLabel nameLabel = new JLabel();
        nameLabel.setText(review.getName());
        mainPanel.add(nameLabel, BorderLayout.NORTH);
        JPanel panel = new JPanel(new BorderLayout());
        for(ReviewItem reviewItem : review.getReviewItems()) {
            final PreviewItemForm previewItemForm = new PreviewItemForm(Searcher.getInstance(review.getProject()), reviewItem);
            panel.add(previewItemForm.getContents());
            reviewItemFormsList.add(previewItemForm);
        }
        mainPanel.add(panel);
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
}
