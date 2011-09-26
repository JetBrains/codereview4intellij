package ui.gutterpoint;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.VisibleAreaEvent;
import com.intellij.openapi.editor.event.VisibleAreaListener;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.ui.popup.*;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.PositionTracker;
import com.sun.org.apache.bcel.internal.generic.IFEQ;
import reviewresult.Review;

import javax.swing.*;
import java.awt.*;

/**
 * User: Alisa.Afonina
 * Date: 8/26/11
 * Time: 3:55 PM
 */
public class BalloonWithSelection{
    private Review review = null;
    private Balloon balloon = null;
    private Editor editor;
    private Point target;
    private String title;
    private boolean closed;
    private RangeHighlighter highlighter = null;
    private static final TextAttributes REVIEW_ATTRIBUTES =
                                new TextAttributes(null, new Color(224,255,204), null, null, Font.PLAIN);
    private JComponent balloonContent;

    public BalloonWithSelection() {}

    public BalloonWithSelection(final Review review, Editor editor, Point target, JComponent balloonContent, String title) {
        this.review = review;
        this.editor = editor;
        this.target = target;
        this.title = title;
        this.balloonContent = balloonContent;
        this.highlighter = editor.getMarkupModel().addRangeHighlighter(review.getStart(),
                                                                       review.getEnd(),
                                                                       HighlighterLayer.SELECTION - 12,
                                                                       REVIEW_ATTRIBUTES,
                                                                       HighlighterTargetArea.EXACT_RANGE);
        createBalloon();
    }

    private void createBalloon() {

        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().createDialogBalloonBuilder(balloonContent, title);
        balloonBuilder.setHideOnClickOutside(true);
        balloonBuilder.setHideOnKeyOutside(true);
        this.closed = false;
        balloon = balloonBuilder.createBalloon();
        this.balloon.addListener(new JBPopupAdapter() {
            @Override
            public void onClosed(LightweightWindowEvent event) {
                    BalloonWithSelection.this.closed = true;
                    if(highlighter.isValid()) {
                        highlighter.dispose();
                    }
            }
        });
    }


    public void dispose() {
        if(balloon != null && !balloon.isDisposed())
            balloon.dispose();
        closed = true;
        /*if(review != null)
            review.setActivated(false);*/
        if(highlighter != null) {
                    if(highlighter.isValid()) {
                        highlighter.dispose();
            }
        }
    }

    public boolean isValid() {
        return balloon != null && !balloon.wasFadedOut() && !balloon.isDisposed();
    }

    public void showBalloon( final JComponent contentComponent) {
        if(!review.isValid()) return;
        if(balloon == null) return;
       // this.review.setActivated(true);
        balloon.show(new ReviewPositionTracker(editor, contentComponent, target), Balloon.Position.below);
    }

    public boolean compare(Review review) {
        return this.review != null && review.equals(this.review);
    }

    public boolean isClosed() {
        return closed;
    }


    private class ReviewPositionTracker extends PositionTracker<Balloon> {

        private final Editor editor;
        private final JComponent component;
        private final Point point;

        public ReviewPositionTracker(Editor editor, JComponent component, Point point) {
            super(component);
            this.editor = editor;
            this.component = component;
            this.point = point;
            final Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
            if(point.x > visibleArea.getWidth()) {
                            point.x = (int) (visibleArea.getX() + visibleArea.getWidth()
                                                    - balloon.getPreferredSize().getWidth()/2);
            }
            /*if(point.y > visibleArea.getHeight()) {
                            point.y = (int) (visibleArea.getY() + visibleArea.getHeight()
                                                    - balloon.getPreferredSize().getHeight());
            }*/
        }

        @Override
        public RelativePoint recalculateLocation(final Balloon object) {
            final Rectangle visibleArea = editor.getScrollingModel().getVisibleArea();
            //final Rectangle visibleArea = editor.getScrollingModel().getVisibleAreaOnScrollingFinished();
            /*if(visibleArea.getBounds().getWidth() == 0 || visibleArea.getHeight() == 0) {
            }*/
            if (!visibleArea.contains(point)) {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        object.hide();
                        /*if(point.y > visibleArea.getHeight()) {
                            point.y = (int) (visibleArea.getY() + visibleArea.getHeight()
                                                    - balloon.getPreferredSize().getHeight());
                        }*/
                    }
                });

                final PositionTracker<Balloon> tracker = this;
                final VisibleAreaListener listener = new VisibleAreaListener() {
                   @Override
                   public void visibleAreaChanged(VisibleAreaEvent e) {
                           //final Rectangle newRectangle = e.getNewRectangle();
                           final Rectangle newRectangle = e.getEditor().getScrollingModel().getVisibleAreaOnScrollingFinished();
                           if(newRectangle.contains(point) && object.isDisposed() && !balloon.isDisposed()) {
                            final VisibleAreaListener listener = this;
                            SwingUtilities.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    //createBalloon();
                                    balloon.show(tracker, Balloon.Position.below);
                                    editor.getScrollingModel().removeVisibleAreaListener(listener);
                                }
                            });
                        }
                   }
                };
                editor.getScrollingModel().addVisibleAreaListener(listener);
            }
            return new RelativePoint(component, point);
           }
    }
}
