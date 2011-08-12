package ui.gutterpoint;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import reviewresult.Review;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * User: Alisa.Afonina
 * Date: 8/2/11
 * Time: 11:30 AM
 */
public class ReviewPointManager extends AbstractProjectComponent implements DumbAware{
    private Map<Review, ReviewPoint> reviewPoints = new HashMap<Review, ReviewPoint>();
    private final StartupManagerEx startupManager;

    public ReviewPointManager(Project project, final StartupManager startupManager) {
        super(project);
        this.startupManager = (StartupManagerEx)startupManager;
    }

    public static ReviewPointManager getInstance(Project project) {
        return project.getComponent(ReviewPointManager.class);
    }

    public Map<Review, ReviewPoint> getReviewPoints() {
        return Collections.unmodifiableMap(reviewPoints);
    }

    public void updateUI() {
        for (ReviewPoint point : reviewPoints.values()) {
            //updateUI(point);
            point.updateUI();
        }
    }


    public ReviewPoint findReviewPoint(Review review) {
       // if(reviewPoints.containsKey(review)) {
            return reviewPoints.get(review);
        //}
        //return null;
    }

    private ReviewPoint makeReviewPoint(Review review) {
        ReviewPoint point = new ReviewPoint(review);
        reviewPoints.put(review, point);
        return point;
    }

    public void reloadReviewPoint(Review review) {
        ReviewPoint reviewPoint = findReviewPoint(review);
        if(reviewPoint == null) {
            if(review.isValid())
                makeReviewPoint(review);
        }
        updateUI();
        if(review.getReviewBean().isDeleted())
            reviewPoints.remove(review);
    }

    public void updateReviewPoint(Review review, Review newReview) {
        ReviewPoint reviewPoint = findReviewPoint(review);
        if(reviewPoint != null) {
            reviewPoints.remove(review);
            reviewPoints.put(newReview, reviewPoint);
            //reloadReviewPoint(newReview);
            //reviewPoint.updateUI();
            //updateUI();
        }
    }
}
