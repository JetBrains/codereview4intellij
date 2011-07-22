package reviewresult;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.xdebugger.impl.breakpoints.XLineBreakpointImpl;
import org.jetbrains.annotations.NotNull;
import ui.reviewpoint.ReviewPoint;

import java.util.*;

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
public class ReviewManager extends AbstractProjectComponent implements PersistentStateComponent<ReviewManager.State> {
    private final StartupManagerEx startupManager;
    private static ReviewManager component = null;
    State state = new State();
    private Map<VirtualFile, List<Review>> reviews = new HashMap<VirtualFile, List<Review>>();
    private Map<Review, ReviewPoint> reviewPoints = new HashMap<Review, ReviewPoint>();


    public ReviewManager(Project project, final StartupManager startupManager) {
        super(project);
        this.startupManager = (StartupManagerEx)startupManager;
    }


    public static ReviewManager getInstance(@NotNull Project project) {
        return project.getComponent(ReviewManager.class);
    }

    public void addReview(Review newReview) {
        VirtualFile virtualFile = newReview.getVirtualFile();

        if(reviews.containsKey(virtualFile)) {
            List<Review> reviewList = reviews.get(virtualFile);
            boolean reviewExists = false;
            for (Review review : reviewList) {
                if(newReview.equals(review)) {
                    reviewExists = true;
                    break;
                }
            }
            if(!reviewExists) {
                reviewList.add(newReview);
                ReviewBean reviewBean = newReview.getReviewBean();
                if(!state.reviews.contains(reviewBean)) {
                    state.reviews.add(reviewBean);
                }
            }
        } else {
            ArrayList<Review> reviewsList = new ArrayList<Review>();
            reviewsList.add(newReview);
            reviews.put(virtualFile, reviewsList);
            ReviewBean reviewBean = newReview.getReviewBean();
            if(!state.reviews.contains(reviewBean)) {
                state.reviews.add(reviewBean);
            }
        }
    }

    public State getState() {
        return state;
    }

    public void loadState(State state) {
        this.state = state;
        reviews = new HashMap<VirtualFile, List<Review>>();
        for (ReviewBean reviewBean : state.reviews) {
            Review review = new Review(reviewBean, myProject);
            ReviewPoint point = new ReviewPoint(review);
            reviewPoints.put(review, point);
            addReview(review);
        }
        updateUI();
    }

    public void updateUI() {
        Runnable runnable = new Runnable() {
            public void run() {
                for (ReviewPoint point : reviewPoints.values()) {
                    point.updateUI();
//                    System.out.println("test");
                }
            }
        };
        if (startupManager.startupActivityPassed()) {
          runnable.run();
        }
        else {
          startupManager.registerPostStartupActivity(runnable);
        }
    }

    public List<Review> getReviews(VirtualFile virtualFile) {
        return reviews.get(virtualFile);
    }

    public Set<VirtualFile> getFiles() {
        return reviews.keySet();
    }

    public ReviewPoint findOrCreateReviewPoint(Review review) {
        if(reviewPoints.containsKey(review)) {
            return reviewPoints.get(review);
        }
        ReviewPoint point = new ReviewPoint(review);
        reviewPoints.put(review, point);
        updateUI();
        return point;

    }

    @Override
    public void projectOpened() {
    }

    @Override
    public void projectClosed() {

    }

    @Override
    public void initComponent() {

    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ReviewManager";
    }


    public static class State {
        @Tag("reviews")
        @AbstractCollection(surroundWithTag = false)
        public List<ReviewBean> reviews;
        public State() {
            reviews = new ArrayList<ReviewBean>();
        }
    }
}
