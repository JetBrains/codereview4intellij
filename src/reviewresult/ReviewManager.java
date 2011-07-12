package reviewresult;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import components.reviewpoint.ReviewPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:51 PM
 */

@State(
    name = "ReviewManager",
    storages = {
       @Storage(id = "default", file = "$PROJECT_FILE$"),
      @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/codeReview.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class ReviewManager implements PersistentStateComponent<ReviewManager.State> {
    private Map <ReviewPoint, Review> reviews = new HashMap<ReviewPoint, Review>();
    private static Project project;

    State state = new State();

    public static ReviewManager getInstance(Project newProject) {
        project = newProject;
        return ServiceManager.getService(newProject, ReviewManager.class);
    }

    public void addReview(CharSequence text, ReviewStatus status, ReviewPoint point) {
        Review review = null;
        if(reviews.containsKey(point)) {
            review = reviews.get(point);
            review.addReviewItem(text.toString(), status);
        } else {
            review = new Review(text.toString(), status, point.getUrl(), point.getLineNumber());
            reviews.put(point, review);
            state.reviews.add(review);
        }
    }
    public State getState() {
        return state;
    }

    public void loadState(State state) {
        this.state = state;
        reviews = new HashMap<ReviewPoint, Review>();
        for (Review review : state.reviews) {
            VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(review.getUrl());
            ReviewPoint point = new ReviewPoint(project,file, review.getLineNumber());
            if(!reviews.containsKey(point)) {
                reviews.put(point, review);
            }
        }
    }

    public static class State {
        public List <Review> reviews;

        public State() {
            reviews = new ArrayList<Review>();
        }
    }

}
