package reviewresult;

import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.*;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewItem;
import reviewresult.persistent.ReviewsState;
import ui.reviewpoint.ReviewPointManager;
import utils.Util;

import java.util.*;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:51 PM
 */

public class ReviewManager extends AbstractProjectComponent {

    private Map<String, List<Review>> filePath2reviews = new HashMap<String, List<Review>>();

    private Set<String> exportedFiles = new HashSet<String>();

    private boolean saveReviewsToPatch;

    private static final Logger LOG = Logger.getInstance(ReviewManager.class.getName());
    private ReviewsChangedListener eventPublisher;

    public ReviewManager(Project project) {
        super(project);
        VirtualFileManager.getInstance().addVirtualFileListener(new ReviewVirtualFileListener(), project);
        eventPublisher = project.getMessageBus().syncPublisher(ReviewChangedTopics.REVIEW_STATUS);
    }

    public static ReviewManager getInstance(@NotNull Project project) {
        return project.getComponent(ReviewManager.class);
    }

    public List<ReviewBean> getState() {
        List<ReviewBean> result = new ArrayList<ReviewBean>();
        for (Map.Entry<String, List<Review>> entry : filePath2reviews.entrySet()) {
            for (Review review : entry.getValue()) {
                result.add(review.getReviewBean());
            }
        }
        return result;
    }

    public void loadState(List<ReviewBean> reviewBeans) {
        loadReviews(reviewBeans, false);
    }

    public void loadReviewsForFile(List<ReviewBean> reviewBeans) {
        loadReviews(reviewBeans, true);
    }

    public void loadReviews(List<ReviewBean> reviewBeans, boolean part) {
        if(!part) {
            filePath2reviews = new HashMap<String, List<Review>>();
        }
        for (ReviewBean reviewBean : reviewBeans) {
            Review review = new Review(reviewBean, myProject);
            addReview(review);

        }
        ReviewPointManager.getInstance(myProject).updateUI();
    }


    public void addReview(Review newReview) {
        String filePath = newReview.getReviewBean().getFilePath();

        if(filePath2reviews.containsKey(filePath)) {
            List<Review> reviewList = filePath2reviews.get(filePath);
            boolean contains = false;
            for(Review review : reviewList) {
                if(review.equals(newReview)) {
                    review.setReviewBean(newReview.getReviewBean());

                    contains = true;
                    if(!review.isValid()) {
                        eventPublisher.reviewDeleted(review);
                        logInvalidReview(review);
                    } else {
                        eventPublisher.reviewChanged(review);
                    }
                    break;
                }
            }
            if(!contains) {
                reviewList.add(newReview);
                eventPublisher.reviewAdded(newReview);
            }
        } else {
            if(newReview.isValid()) {
                ArrayList<Review> reviewsList = new ArrayList<Review>();
                reviewsList.add(newReview);
                eventPublisher.reviewAdded(newReview);
                filePath2reviews.put(filePath, reviewsList);
            } else {
                 logInvalidReview(newReview);
            }
        }
        ReviewPointManager.getInstance(myProject).reloadReviewPoint(newReview);
    }

    public List<Review> getValidReviews(String filepath) {
        ArrayList<Review> reviewsList = new ArrayList<Review>();
        List<Review> reviews = filePath2reviews.get(filepath);
        if(reviews == null || reviews.isEmpty()) return reviews;
        for(Review review : reviews) {
            if(review.isValid()) {
                reviewsList.add(review);
            }
        }
        return reviewsList;
    }

    public Set<String> getFileNames() {
        return filePath2reviews.keySet();
    }



    @NotNull
    @Override
    public String getComponentName() {
        return "ReviewManager";
    }

    public void removeReview(Review review) {
        if(review.isValid()) {
            review.getReviewBean().setDeleted(true);
        }
        eventPublisher.reviewDeleted(review);
        ReviewPointManager.getInstance(myProject).reloadReviewPoint(review);
    }

    public void logInvalidReview(Review review) {
        String message = "Review with start offset " + String.valueOf(review.getStart())
                    + " and file \"" + review.getReviewBean().getFilePath() + "\" became invalid";
        LOG.warn(message);
    }

    /*public void unloadReviewsForFile(State state) {
        for (ReviewBean reviewBean : state.reviews) {
            removeReview(new Review(reviewBean, myProject));
        }
    }*/

    public List<ReviewBean> getAddedForFile(String filepath) {
        List<Review> reviewsPart = filePath2reviews.get(filepath);
        if(reviewsPart != null && !reviewsPart.isEmpty()) {
            List<ReviewBean> reviewBeans = new ArrayList<ReviewBean>();
            for(Review review : reviewsPart) {
                reviewBeans.add(review.getReviewBean());
            }
            return reviewBeans;
        }
        return null;
    }

    /*public List<ReviewBean> getRemovedForFile(VirtualFile file) {
        return removed.get(file.getUrl());
    }*/

    /*public List<Review> getFilteredReviews(String fileName) {
        return filteredReviews.get(fileName);
    }*/

    public int getReviewCount(Collection<VirtualFile> virtualFiles) {
        int reviewCount = 0;
        for(VirtualFile file : virtualFiles) {
            if(filePath2reviews.containsKey(file.getUrl())) {
               reviewCount += filePath2reviews.get(file.getUrl()).size();
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

    public List<ReviewBean> getReviewsToExport() {
        return getReviewsToExport("");
    }

    public List<ReviewBean> getReviewsToExport(String filepath) {
        if(exportedFiles.isEmpty()) {
            exportedFiles = filePath2reviews.keySet();
        }
        List<ReviewBean> result = new ArrayList<ReviewBean>();
        if("".equals(filepath)) {
            for(String path : filePath2reviews.keySet()) {
                result.addAll(getAddedForFile(path));
            }
            return result;
        }
        exportedFiles.remove(filepath);
        return getAddedForFile(filepath);
    }

   public String getExportText() {
        return getExportTextForFile("");
   }

   public String getExportTextForFile(String filepath) {
        ReviewsState.State state = new ReviewsState.State();
        String result = "";
        state.reviews = getReviewsToExport(filepath);
        if((state.reviews == null || state.reviews.isEmpty())/* && (state.removed == null || state.removed.isEmpty()) */) return "";
        Element addedElement = XmlSerializer.serialize(state);
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        result += outputter.outputString(addedElement);
        return result;
   }

    public void removeAll(VirtualFile file) {
        if(filePath2reviews.containsKey(file.getUrl())) {
            List<Review> reviews = filePath2reviews.get(file.getUrl());
            for (Review review : reviews) {
                removeReview(review);
            }
        } else {
            for(VirtualFile child : file.getChildren()) {
                removeAll(child);
            }
        }
    }

    private class ReviewVirtualFileListener extends VirtualFileAdapter {
        @Override
        public void beforeFileMovement(VirtualFileMoveEvent event) {
            String  url  = event.getOldParent().getUrl() + "/" + event.getFileName();
            String  newUrl  = event.getNewParent().getUrl() + "/"  + event.getFileName();
            if(filePath2reviews.containsKey(url)) {
                List<Review> reviewList = filePath2reviews.get(url);
                filePath2reviews.remove(url);
                filePath2reviews.put(newUrl, reviewList);
                for (Review review : reviewList) {
                    review.getReviewBean().setFilePath(newUrl);
                }
            }
        }

        @Override
        public void beforeFileDeletion(VirtualFileEvent event) {
            VirtualFile oldFile = event.getFile();
            String url = oldFile.getUrl();
            if(filePath2reviews.containsKey(url)) {
                List<Review> reviewList = filePath2reviews.get(url);
                filePath2reviews.remove(url);
                for (Review review : reviewList) {
                    //ReviewPointManager.getInstance(myProject).reloadReviewPoint(review);
                    review.getReviewBean().setDeleted(true);
                }
            }
        }
    }
}
