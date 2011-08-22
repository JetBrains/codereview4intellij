package ui.reviewtoolwindow;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import utils.Util;

import java.util.*;

/**
 * User: Alisa.Afonina
 * Date: 8/5/11
 * Time: 7:40 PM
 */
public class Searcher  extends AbstractProjectComponent {
    private Map<Review, Pair<Integer, Integer>> review2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
    private Map<ReviewItem, Pair<Integer, Integer>> reviewitem2searchresult = new HashMap<ReviewItem, Pair<Integer, Integer>>();
    private Set<String> filteredFileNames = new HashSet<String>();
    private String filter = "";
    private final Project project;

    protected Searcher(Project project) {
        super(project);
        this.project = project;
    }

    public static Searcher getInstance(@NotNull Project project) {
        return project.getComponent(Searcher.class);
    }

    public Set<String> getFilteredFileNames() {
        if(filterIsSet()) {
            return Collections.unmodifiableSet(filteredFileNames);
        } else return ReviewManager.getInstance(project).getFileNames();
    }

    private boolean filterIsSet(){
        return !("".equals(filter));
    }

    public void createFilter(String text) {
        filter = text;
        ReviewManager instance = ReviewManager.getInstance(myProject);
        Set<String> fileNames = instance.getFileNames();
        if(fileNames == null) return;
        emptyFilter();
        if(!(text == null || "".equals(text))) {
            for(String url : fileNames) {
                List<Review> validReviews = instance.getValidReviews(url);
                if(validReviews == null) return;
                for(Review review : validReviews) {
                    addSearchResult(review);
                }

            }
        }
    }

    public void addSearchResult(Review review) {
        int reviewStart = Util.find(review.getName(), filter);
        boolean contains = false;
        int reviewEnd;
        if(reviewStart >= 0) {
            contains = true;
            reviewEnd = reviewStart + filter.length();
            Pair<Integer, Integer> reviewResult = new Pair<Integer, Integer>(reviewStart, reviewEnd);
            review2searchresult.put(review, reviewResult);
        }
        for(ReviewItem item : review.getReviewItems()) {
            int itemStart = Util.find(item.getText(), filter);
            int itemEnd;
            if(itemStart != -1) {
                contains = true;
                if(itemStart >= 0) {
                    itemEnd = itemStart + filter.length();
                    Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(itemStart, itemEnd);
                    reviewitem2searchresult.put(item, itemResult);
                }
            }
        }
        if(contains) filteredFileNames.add(review.getFilePath());
    }

    public void emptyFilter() {
        review2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
        reviewitem2searchresult = new HashMap<ReviewItem, Pair<Integer, Integer>>();
        filteredFileNames = new HashSet<String>();
    }

    public Pair<Integer, Integer> getItemSearchResult(ReviewItem item) {
        if(reviewitem2searchresult.containsKey(item)) {
            return reviewitem2searchresult.get(item);
        }
        return new Pair<Integer, Integer>(-1,-1);
    }

    public Pair<Integer, Integer> getReviewSearchResult(Review review) {
        if(review2searchresult.containsKey(review)) {
            return review2searchresult.get(review);
        }
        return new Pair<Integer, Integer>(-1,-1);
    }

    public boolean containsReview(Review review) {
        boolean contains = review2searchresult.containsKey(review);
        for (ReviewItem reviewItem : review.getReviewItems()) {
            contains |= (containsReviewItem(reviewItem));
        }
        return !filterIsSet() || contains;
    }

    private boolean containsReviewItem(ReviewItem reviewItem) {
        return !filterIsSet() || reviewitem2searchresult.containsKey(reviewItem);
    }

    public String getFilter() {
        return filter;
    }

    public boolean isEmpty() {
        return filterIsSet() && (review2searchresult.isEmpty() && reviewitem2searchresult.isEmpty());
    }
}
