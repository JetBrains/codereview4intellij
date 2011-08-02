package reviewresult;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import org.jetbrains.annotations.NotNull;
import ui.reviewpoint.ReviewPoint;
import ui.reviewpoint.ReviewPointManager;
import utils.Util;

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
public class ReviewManager extends AbstractProjectComponent implements PersistentStateComponent<ReviewManager.State>, VirtualFileListener {
    //private final StartupManagerEx startupManager;
    private State state = new State();
    private Map<String, List<Review>> reviews = new HashMap<String, List<Review>>();
    private Map<String, List<Review>> filteredReviews = new HashMap<String, List<Review>>();
    private Map<String, List<ReviewBean>> removed = new HashMap<String, List<ReviewBean>>();
    private boolean saveReviewsToPatch;

    private static final Logger LOG = Logger.getInstance(ReviewManager.class.getName());


    public ReviewManager(Project project, final StartupManager startupManager) {
        super(project);
        //this.startupManager = (StartupManagerEx)startupManager;
        VirtualFileManager.getInstance().addVirtualFileListener(this);
    }

    public static ReviewManager getInstance(@NotNull Project project) {
        return project.getComponent(ReviewManager.class);
    }

    public void addReview(Review newReview) {
        String url = newReview.getReviewBean().getUrl();

        if(reviews.containsKey(url)) {
            List<Review> reviewList = reviews.get(url);
            reviewList.add(newReview);
            ReviewBean reviewBean = newReview.getReviewBean();
            if(!state.reviews.contains(reviewBean)) {
                state.reviews.add(reviewBean);
            }
        } else {
            ArrayList<Review> reviewsList = new ArrayList<Review>();
            reviewsList.add(newReview);
            reviews.put(url, reviewsList);
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
        loadReviews(state, false);

    }

    public void loadReviewsForFile(State state) {
        loadReviews(state, true);
    }

    private void loadReviews(State state, boolean part) {
        if(!part) {
            reviews = new HashMap<String, List<Review>>();
        }
        List<ReviewBean> toRemove = new ArrayList<ReviewBean>();
        for (ReviewBean reviewBean : state.reviews) {
            Review review = new Review(reviewBean, myProject);
            if(review.isValid()) {
                addReview(review);
                ReviewPointManager.getInstance(myProject).loadReviewPoint(review);
            }
            else {
                toRemove.add(review.getReviewBean());
                logInvalidReview(review);
            }
        }
        if(!toRemove.isEmpty()) {
            state.reviews.removeAll(toRemove);
        }
        ReviewPointManager.getInstance(myProject).updateUI();
    }





    public List<Review> getReviews(VirtualFile virtualFile) {
        return reviews.get(virtualFile.getUrl());
    }

    public Set<String> getFileNames() {
        if(filteredReviews.isEmpty()) {
            return reviews.keySet();
        }
        return filteredReviews.keySet();
    }

    public void createFilter(String text) {
        if(text == null || "".equals(text)) {
            filteredReviews = reviews;
        }
        else {
            filteredReviews = new HashMap<String, List<Review>>();
            for(String url : reviews.keySet()) {
                for(Review review : reviews.get(url)) {
                    boolean contains = false;
                    for(ReviewItem item : review.getReviewItems()) {
                        int itemStart = Util.find(item.getText(), text);
                        int reviewStart = Util.find(review.getName(), text);
                        int itemEnd = -1;
                        int reviewEnd = -1;
                        if(itemStart != -1 || reviewStart != -1) {
                            contains = true;

                            if(itemStart >= 0) {
                                itemEnd = itemStart + text.length();
                            }
                            if(reviewStart >= 0) {
                                reviewEnd = reviewStart + text.length();
                            }
                        }
                        item.setSearchStart(itemStart);
                        item.setSearchEnd(itemEnd);
                        review.setSearchStart(reviewStart);
                        review.setSearchEnd(reviewEnd);
                    }
                    if(contains) {
                        if(filteredReviews.containsKey(url)) {
                            List<Review> reviewList = filteredReviews.get(url);
                            reviewList.add(review);
                        } else {
                            ArrayList<Review> reviewsList = new ArrayList<Review>();
                            reviewsList.add(review);
                            filteredReviews.put(url, reviewsList);
                        }
                    }
                }
            }
        }
    }

    public void emptyFilter() {
         for(String url : reviews.keySet()) {
            for(Review review : reviews.get(url)) {
                review.setSearchStart(-1);
                review.setSearchEnd(-1);
                for(ReviewItem item : review.getReviewItems()) {
                    item.setSearchStart(-1);
                    item.setSearchEnd(-1);
                }
            }
         }
         filteredReviews = reviews;
    }


    @Override
    public void projectOpened() {

    }

    @Override
    public void projectClosed() {}

    @Override
    public void initComponent() {}

    @Override
    public void disposeComponent() {}

    @NotNull
    @Override
    public String getComponentName() {
        return "ReviewManager";
    }

    public void clearAll() {
        state = new State();
        reviews = new HashMap<String, List<Review>>();
       // reviewPoints = new HashMap<Review, ReviewPoint>();
    }

    public void removeReview(Review review) {
            ReviewPointManager.getInstance(myProject).removePoint(review);
            ReviewBean reviewBean = review.getReviewBean();
            String url = reviewBean.getUrl();
            List<Review> reviewsList = reviews.get(url);
            reviewsList.remove(review);
            if(reviewsList.isEmpty()) reviews.remove(url);
            state.reviews.remove(reviewBean);
            List<ReviewBean> reviewBeans = removed.get(reviewBean.getUrl());
            if(reviewBeans == null) {
                ArrayList<ReviewBean> value = new ArrayList<ReviewBean>();
                value.add(reviewBean);
                removed.put(reviewBean.getUrl(), value);
                state.removed.add(reviewBean);
            } else {
                reviewBeans.add(reviewBean);
            }
    }

    public void logInvalidReview(Review review) {
        String message = "Review with start offset " + String.valueOf(review.getStart())
                    + " and file \"" + review.getReviewBean().getUrl() + "\" became invalid";
        LOG.warn(message);
    }

    @Override
    public void propertyChanged(VirtualFilePropertyEvent event) {}

    @Override
    public void contentsChanged(VirtualFileEvent event) {}

    @Override
    public void fileCreated(VirtualFileEvent event) {}

    @Override
    public void fileDeleted(VirtualFileEvent event) {}

    @Override
    public void fileMoved(VirtualFileMoveEvent event) {}

    @Override
    public void fileCopied(VirtualFileCopyEvent event) {}

    @Override
    public void beforePropertyChange(VirtualFilePropertyEvent event) {}

    @Override
    public void beforeContentsChange(VirtualFileEvent event) {}

    @Override
    public void beforeFileDeletion(VirtualFileEvent event) {
        VirtualFile oldFile = event.getFile();
        String url = oldFile.getUrl();
        if(reviews.containsKey(url)) {
            List<Review> reviewList = reviews.get(url);
            reviews.remove(url);
            for (Review review : reviewList) {
                ReviewPointManager.getInstance(myProject).removePoint(review);
                state.reviews.remove(review.getReviewBean());
            }
        }
    }

    @Override
    public void beforeFileMovement(VirtualFileMoveEvent event) {
        String  url  = event.getOldParent().getUrl() + "/" + event.getFileName();
        String  newUrl  = event.getNewParent().getUrl() + "/"  + event.getFileName();
        if(reviews.containsKey(url)) {
            List<Review> reviewList = reviews.get(url);
            reviews.remove(url);
            reviews.put(newUrl, reviewList);
            for (Review review : reviewList) {
                review.getReviewBean().setUrl(newUrl);
            }
        }
    }

    public void unloadReviewsForFile(State state) {
        for (ReviewBean reviewBean : state.reviews) {
            removeReview(new Review(reviewBean, myProject));
        }
    }

    public List<ReviewBean> getAddedForFile(VirtualFile file) {
        List<Review> reviewsPart = reviews.get(file.getUrl());
        if(reviewsPart != null && !reviewsPart.isEmpty()) {
            List<ReviewBean> reviewBeans = new ArrayList<ReviewBean>();
            for(Review review : reviewsPart) {
                reviewBeans.add(review.getReviewBean());
            }
            return reviewBeans;
        }
        return null;
    }

    public List<ReviewBean> getRemovedForFile(VirtualFile file) {
        return removed.get(file.getUrl());
    }

    public List<Review> getFilteredReviews(String fileName) {
        return filteredReviews.get(fileName);
    }

    public int getReviewCount(Collection<VirtualFile> virtualFiles) {
        int reviewCount = 0;
        for(VirtualFile file : virtualFiles) {
            if(reviews.containsKey(file.getUrl())) {
               reviewCount += reviews.get(file.getUrl()).size();
            }
        }
        return reviewCount;
    }

    public void setSaveReviewsToPatch(boolean saveReviewsToPatch) {
        this.saveReviewsToPatch = saveReviewsToPatch;
    }

    public boolean isSaveReviewsToPatch() {
        return saveReviewsToPatch;
    }

    public static class State {
        @Tag("reviews")
        @AbstractCollection(surroundWithTag = false)
        public List<ReviewBean> reviews = new ArrayList<ReviewBean>();
        @Tag("removed_reviews")
        @AbstractCollection(surroundWithTag = false)
        public List<ReviewBean> removed = new ArrayList<ReviewBean>();
    }
}
