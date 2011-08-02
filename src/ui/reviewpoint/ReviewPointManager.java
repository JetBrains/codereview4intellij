package ui.reviewpoint;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import reviewresult.Review;
import reviewresult.ReviewManager;

import java.util.HashMap;
import java.util.Map;

/**
 * User: Alisa.Afonina
 * Date: 8/2/11
 * Time: 11:30 AM
 */
public class ReviewPointManager extends AbstractProjectComponent {
    private Map<Review, ReviewPoint> reviewPoints = new HashMap<Review, ReviewPoint>();
    private StartupManagerEx startupManager;

    private static final Logger LOG = Logger.getInstance(ReviewPointManager.class.getName());

    public ReviewPointManager(Project project, final StartupManager startupManager) {
        super(project);
        this.startupManager = (StartupManagerEx)startupManager;
    }

    public static ReviewPointManager getInstance(Project project) {
        return project.getComponent(ReviewPointManager.class);
    }

    public Map<Review, ReviewPoint> getReviewPoints() {
        return reviewPoints;
    }

    public void updateUI() {
        for (ReviewPoint point : reviewPoints.values()) {
            updateUI(point);
        }
    }

    public void updateUI(final ReviewPoint point) {
        Runnable runnable = new Runnable() {
            public void run() {
                    point.updateUI();
            }
        };
        if (startupManager.startupActivityPassed()) {
          runnable.run();
        }
        else {
          startupManager.registerPostStartupActivity(runnable);
        }
    }

     public ReviewPoint findReviewPoint(Review review) {
        if(reviewPoints.containsKey(review)) {
            return reviewPoints.get(review);
        }
        return null;
    }

    public void createReviewPoint(Review review) {
        if(review.isValid()) {
            ReviewPoint point = makeReviewPoint(review);
            if(point != null) {
                updateUI(point);
            }
        } else {
            logInvalidReview(review);
        }
    }



    private ReviewPoint makeReviewPoint(Review review) {
        ReviewPoint point = new ReviewPoint(review);
        reviewPoints.put(review, point);
        //addReview(review);
        return point;
    }

    public void removePoint(Review review) {
        if(reviewPoints.containsKey(review)) {
            reviewPoints.get(review).release();
            reviewPoints.remove(review);
        }
    }

    public void loadReviewPoint(Review review) {
            if(findReviewPoint(review) == null) {
                    makeReviewPoint(review);
                }
    }

    public void logInvalidReview(Review review) {
        String message = "Review with start offset " + String.valueOf(review.getStart())
                    + " and file \"" + review.getReviewBean().getUrl() + "\" became invalid";
        LOG.warn(message);
    }
}
