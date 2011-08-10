package ui.reviewtoolwindow;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import utils.Util;

import javax.print.attribute.TextSyntax;
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
    private String filter;
    private Project project;

    protected Searcher(Project project) {
        super(project);
        this.project = project;
    }

    public static Searcher getInstance(@NotNull Project project) {
        return project.getComponent(Searcher.class);
    }

    public Set<String> getFilteredFileNames() {
        return Collections.unmodifiableSet(filteredFileNames);
    }

    public boolean filterIsSet(){
        return !("".equals(filter));
    }

    public void createFilter(String text) {
        filter = text;
        ReviewManager instance = ReviewManager.getInstance(myProject);
        Set<String> fileNames = instance.getFileNames();
        if(fileNames == null) return;

        if(text == null || "".equals(text)) {
            emptyFilter();
        }
        else {

            for(String url : fileNames) {
                boolean contains = false;
                List<Review> validReviews = instance.getValidReviews(url);
                if(validReviews == null) return;
                for(Review review : validReviews) {
                    int reviewStart = Util.find(review.getName(), text);
                    int reviewEnd;
                    if(reviewStart >= 0) {
                        contains = true;
                        reviewEnd = reviewStart + text.length();
                        Pair<Integer, Integer> reviewResult = new Pair<Integer, Integer>(reviewStart, reviewEnd);
                        review2searchresult.put(review, reviewResult);
                    }
                    for(ReviewItem item : review.getReviewItems()) {
                        int itemStart = Util.find(item.getText(), text);
                        int itemEnd;
                        if(itemStart != -1) {
                            contains = true;
                            if(itemStart >= 0) {
                                itemEnd = itemStart + text.length();
                                Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(itemStart, itemEnd);
                                reviewitem2searchresult.put(item, itemResult);
                            }
                        }
                    }
                }
                if(contains) filteredFileNames.add(url);
            }
        }
    }

    public void emptyFilter() {
        filter = "";
        review2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
        reviewitem2searchresult = new HashMap<ReviewItem, Pair<Integer, Integer>>();
        filteredFileNames = new HashSet<String>();
        ReviewView reviewView = ServiceManager.getService(project, ReviewView.class);
        reviewView.updateUI();
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
        return !filterIsSet() || review2searchresult.containsKey(review);
    }

    public boolean containsReviewItem(ReviewItem reviewItem) {
        return !filterIsSet() || reviewitem2searchresult.containsKey(reviewItem);
    }

    public String getFilter() {
        return filter;
    }
}
