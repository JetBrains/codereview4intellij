package reviewresult;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.vfs.*;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewsState;
import sun.security.jca.GetInstance;
import ui.gutterpoint.ReviewPointManager;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:51 PM
 */

public class ReviewManager extends AbstractProjectComponent implements DumbAware {
    private static final Logger LOG = Logger.getInstance(ReviewManager.class.getName());
    private final StartupManagerEx startupManager;

    private Map<String, List<Review>> filePath2reviews = new HashMap<String, List<Review>>();
    private ReviewsChangedListener eventPublisher;

    private boolean saveReviewsToPatch;

    public ReviewManager(@NotNull final Project project, final StartupManager startupManager) {
        super(project);
        this.startupManager = (StartupManagerEx)startupManager;

        VirtualFileManager.getInstance().addVirtualFileListener(new ReviewVirtualFileListener(), project);
        eventPublisher = project.getMessageBus().syncPublisher(ReviewChangedTopics.REVIEW_STATUS);

    }

    public static ReviewManager getInstance(@NotNull Project project) {
        final ReviewManager manager = project.getComponent(ReviewManager.class);
        //if(manager.eventPublisher == null){
        //    manager.eventPublisher = project.getMessageBus().syncPublisher(ReviewChangedTopics.REVIEW_STATUS);
        //}
        return manager;
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

    public void loadReviews(final List<ReviewBean> reviewBeans, boolean isPartOfState) {
        if(!isPartOfState) {
            filePath2reviews = new HashMap<String, List<Review>>();
        }
        final Runnable runnable = new DumbAwareRunnable() {
            public void run() {
                for (ReviewBean reviewBean : reviewBeans) {
                    final Review review = new Review(reviewBean, myProject);
                    addReview(review);
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


    public void addReview(Review newReview) {
        String filePath = newReview.getReviewBean().getFilePath();
        List<Review> reviewList = filePath2reviews.get(filePath);

        if(!(reviewList == null || reviewList.isEmpty())) {
            int existingReviewIndex = reviewList.indexOf(newReview);
            if(existingReviewIndex >= 0) {
                Review review = reviewList.get(existingReviewIndex);
                if(!newReview.isValid() || !review.isValid()) {
                    selectReviewState(review, newReview);
                    } else {
                    mergeReviews(review, newReview);
                }
            }
             else {
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

    private void mergeReviews(Review oldReview, Review newReview) {
        oldReview.setReviewBean(newReview.getReviewBean());
        eventPublisher.reviewChanged(oldReview);
    }

    private void selectReviewState(Review oldReview, Review newReview) {
        removeReview(oldReview);
    }


    @Nullable
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

    @Nullable
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

    @Nullable
    public List<ReviewBean> getReviewsForFile(String filepath) {
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

    public int getReviewCount(@NotNull Collection<VirtualFile> virtualFiles) {
        int reviewCount = 0;
        for(VirtualFile file : virtualFiles) {
                List<Review> reviews = filePath2reviews.get(getFilePath(file));
                if(!(reviews == null || reviews.isEmpty())) {
                    reviewCount += reviews.size();
                }
            }
        return reviewCount;
    }

    private String getFilePath(VirtualFile file) {
        VirtualFile baseDir = myProject.getBaseDir();
        if(baseDir == null) return "";
        return VfsUtil.getRelativePath(file, baseDir, '/');
    }

    public void setSaveReviewsToPatch(boolean saveReviewsToPatch) {
        this.saveReviewsToPatch = saveReviewsToPatch;
    }

    @Nullable
    public List<ReviewBean> getReviewsToExport(String filepath) {
        List<ReviewBean> result = new ArrayList<ReviewBean>();
        if("".equals(filepath)) {
            for(String path : filePath2reviews.keySet()) {
                result.addAll(getReviewsForFile(path));
            }
            return result;
        }
        return getReviewsForFile(filepath);
    }

   public String getExportText() {
        return getExportTextForFile("");
   }

   public String getExportTextForFile(String filepath) {
        ReviewsState.State state = new ReviewsState.State();
        String result = "";
        state.reviews = getReviewsToExport(filepath);
        if((state.reviews == null || state.reviews.isEmpty())) return "";
        Element addedElement = XmlSerializer.serialize(state);
        XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
        result += outputter.outputString(addedElement);
        return result;
   }

    public void removeAll(VirtualFile file) {
        List<Review> reviews = filePath2reviews.get(getFilePath(file));
        if(!(reviews == null || reviews.isEmpty())) {
            for (Review review : reviews) {
                removeReview(review);
            }
        } else {
            for(VirtualFile child : file.getChildren()) {
                removeAll(child);
            }
        }
    }

    public Review getReviewInLine(String url, int line) {
        List<Review> reviews = filePath2reviews.get(url);
        if(!(reviews == null || reviews.isEmpty())) {
            for(Review review : reviews) {
                if(review.getLine() == line) return review;
            }
        }
        return null;
    }

    public void changeReview(Review review) {
        eventPublisher.reviewChanged(review);
    }

    public void importReviewsToFile(String path, String content) {
        VirtualFile file = myProject.getBaseDir().findFileByRelativePath(path);
            if(file == null) return;
            try {
                SAXBuilder builder = new SAXBuilder();
                Element root = builder.build(new StringReader(content)).getRootElement();
                ReviewsState.State state = XmlSerializer.deserialize(root, ReviewsState.State.class);
                ReviewManager reviewManager = ReviewManager.getInstance(myProject);
                reviewManager.loadReviewsForFile(state.reviews);
            } catch(JDOMException e) {
                //todo e.printStackTrace();
            } catch(NullPointerException e) {
                //todo e.printStackTrace();
            } catch (IOException e) {
                //todo e.printStackTrace();
            }
    }

    private class ReviewVirtualFileListener extends VirtualFileAdapter {
        @Override
        public void beforeFileMovement(VirtualFileMoveEvent event) {
            String  url  = getFilePath(event.getOldParent()) + "/" + event.getFileName();
            String  newUrl  = getFilePath(event.getNewParent()) + "/"  + event.getFileName();
            List<Review> reviewList = filePath2reviews.get(url);
            if(!(reviewList == null || reviewList.isEmpty())) {
                filePath2reviews.remove(url);
                filePath2reviews.put(newUrl, reviewList);
                for (Review review : reviewList) {
                    eventPublisher.reviewDeleted(review);
                    review.getReviewBean().setFilePath(newUrl);
                }
            }
        }

        @Override
        public void fileMoved(VirtualFileMoveEvent event) {
            String  newUrl  = getFilePath(event.getNewParent()) + "/"  + event.getFileName();
            List<Review> reviewList = filePath2reviews.get(newUrl);
            if(!(reviewList == null || reviewList.isEmpty())) {
                for (Review review : reviewList) {
                    eventPublisher.reviewAdded(review);
                }
            }
        }

        @Override
        public void beforeFileDeletion(VirtualFileEvent event) {
            VirtualFile oldFile = event.getFile();
            String url = getFilePath(oldFile);
            if(filePath2reviews.containsKey(url)) {
                List<Review> reviewList = filePath2reviews.get(url);
                filePath2reviews.remove(url);
                for (Review review : reviewList) {
                    review.getReviewBean().setDeleted(true);
                }
            }

        }
    }
}
