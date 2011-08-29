package ui.gutterpoint;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupAdapter;
import com.intellij.openapi.ui.popup.LightweightWindowEvent;
import reviewresult.Review;

import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 8/26/11
 * Time: 3:55 PM
 */
public class BalloonWithSelection{
    final private Review review;
    private Balloon balloon;
    final private RangeHighlighter highlighter;
    private static final TextAttributes REVIEW_ATTRIBUTES = new TextAttributes(null, new Color(224,255,204), null, null, Font.PLAIN);

    public BalloonWithSelection(final Review review, Balloon balloon, Editor editor) {
        this.review = review;
        this.balloon = balloon;
        this.review.setActivated(true);
        this.highlighter = editor.getMarkupModel().addRangeHighlighter(review.getStart(),
                                                                          review.getEnd(),
                                                                          HighlighterLayer.SELECTION - 12,
                                                                          REVIEW_ATTRIBUTES,
                                                                          HighlighterTargetArea.EXACT_RANGE);
        this.balloon.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                    BalloonWithSelection.this.review.setActivated(false);
                    if(highlighter.isValid()) {
                        highlighter.dispose();
                    }
            }
        });
    }


    public void dispose() {
        review.setActivated(false);
        if(balloon != null && !balloon.isDisposed())
            balloon.dispose();
        if(highlighter != null) {
                    if(highlighter.isValid()) {
                        highlighter.dispose();
            }
        }
    }

    public Balloon getBalloon() {
        return balloon;
    }

    public boolean isValid() {
        return balloon != null && !balloon.wasFadedOut() && !balloon.isDisposed();
    }
}
