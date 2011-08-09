package ui.reviewtoolwindow;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import utils.Util;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 8/5/11
 * Time: 7:40 PM
 */
public class Searcher  extends AbstractProjectComponent {
    private Map<Review, Pair<Integer, Integer>> reviews2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
    private Map<ReviewItem, Pair<Integer, Integer>> reviewitem2searchresult = new HashMap<ReviewItem, Pair<Integer, Integer>>();
    private Set<String> filteredFileNames = new HashSet<String>();
    private String filter;

    protected Searcher(Project project) {
        super(project);
    }

    public static Searcher getInstance(@NotNull Project project) {
        return project.getComponent(Searcher.class);
    }

    public Set<String> getFilteredFileNames() {
        return filteredFileNames;
    }

    public boolean filterIsSet(){
        return "".equals(filter);
    }

    public void createFilter(String text) {
        filter = text;
        ReviewManager instance = ReviewManager.getInstance(myProject);
        if(text == null || "".equals(text)) {
            filter = "";
            filteredFileNames = new HashSet<String>();
            filteredFileNames.addAll(instance.getFileNames());
        }
        else {

            for(String url : instance.getFileNames()) {
                boolean contains = false;
                for(Review review : instance.getValidReviews(url)) {
                    int reviewStart = Util.find(review.getName(), text);
                    int reviewEnd = -1;
                    if(reviewStart >= 0) {
                        contains = true;
                        reviewEnd = reviewStart + text.length();
                        Pair<Integer, Integer> reviewResult = new Pair<Integer, Integer>(reviewStart, reviewEnd);
                        reviews2searchresult.put(review, reviewResult);
                    }
                    for(ReviewItem item : review.getReviewItems()) {
                        int itemStart = Util.find(item.getText(), text);
                        int itemEnd = -1;
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
        reviews2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
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
        if(reviews2searchresult.containsKey(review)) {
            return reviews2searchresult.get(review);
        }
        return new Pair<Integer, Integer>(-1,-1);
    }
}
