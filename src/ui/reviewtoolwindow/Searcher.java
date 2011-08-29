package ui.reviewtoolwindow;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
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
    private static final String AUTHOR = "author:";
    private static final String STATUS = "status:";

    private boolean authorSpecified;
    private boolean statusSpecified;

    private String status  ="";
    private String author = "";

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
        return !("".equals(filter)) || authorSpecified || statusSpecified;
    }

    public void createFilter(String text) {
        emptyFilter();
        filter = text;
        final int authorIndex = Util.find(filter, AUTHOR);
        if(authorIndex != -1) {
            authorSpecified = true;
            final int beginAuthorName = authorIndex + AUTHOR.length();
            //final String[] split = filter.substring(beginAuthorName).split("\t");
            final int endIndex = filter.lastIndexOf("\"");
            author = filter.substring(filter.indexOf("\"") + 1, endIndex);
            if("".equals(author.trim())) {
                Messages.showErrorDialog("No author was specified for selection", "No Author");
                if(authorIndex > 0) {
                    filter = filter.substring(0, authorIndex);
                }
            }
            else {
                filter = filter.substring(endIndex + 1).trim();
            }
        }

        final int statusIndex = Util.find(filter, STATUS);
        if(statusIndex != -1) {
            statusSpecified = true;
            final int beginStatusName = statusIndex + STATUS.length();
            final String[] split = filter.substring(beginStatusName).split(" ");
            if(split.length == 1 && split[0].equals(STATUS)) {
                Messages.showErrorDialog("No status was specified for selection", "No Status");
                if(authorIndex > 0) {
                    filter = filter.substring(0, statusIndex);
                }
            }
            else {
                status = split[0];
                filter = filter.substring(beginStatusName + status.length()).trim();
            }
        }

        ReviewManager instance = ReviewManager.getInstance(myProject);
        Set<String> fileNames = instance.getFileNames();
        if(fileNames == null) return;
        if(!(filter == null || "".equals(filter)) || statusSpecified || authorSpecified) {
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
        boolean contains = false;
        if(filter != null && !"".equals(filter)) {
            int reviewStart = Util.find(review.getPresentationInfo(false), filter);
            int reviewEnd;
            if(reviewStart >= 0) {
                contains = true;
                reviewEnd = reviewStart + filter.length();
                Pair<Integer, Integer> reviewResult = new Pair<Integer, Integer>(reviewStart, reviewEnd);
                review2searchresult.put(review, reviewResult);
            }
        }
        for(ReviewItem item : review.getReviewItems()) {
            boolean containsStatus = false;
            boolean containsAuthor = false;
            boolean containsReviewItem = false;
            if(statusSpecified) {
                if(item.getStatus().name().compareToIgnoreCase(status) == 0) {
                    containsStatus = true;
                }
            }

            if(authorSpecified) {
                if(item.getAuthor().compareToIgnoreCase(author) == 0) {
                    containsAuthor = true;
                }
            }

            if(containsAuthor) {
                containsReviewItem = true;
                Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(-2, -2);
                reviewitem2searchresult.put(item, itemResult);
            }
            if(containsStatus) {
                containsReviewItem = true;
                Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(-3, -3);
                reviewitem2searchresult.put(item, itemResult);
            }
            final boolean filterSpecified = authorSpecified || statusSpecified;
            final boolean b = !(filterSpecified ^ containsReviewItem);
            if(filter != null && !"".equals(filter) && b) {
                int itemStart = Util.find(item.getText(), filter);
                int itemEnd;
                if(itemStart != -1) {
                    containsReviewItem = true;
                    if(itemStart >= 0) {
                        itemEnd = itemStart + filter.length();
                        Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(itemStart, itemEnd);
                        reviewitem2searchresult.put(item, itemResult);
                    }
               }
            }
            if(containsReviewItem) contains = true;
        }
        if(contains) filteredFileNames.add(review.getFilePath());
    }

    public void emptyFilter() {
        filter = "";
        authorSpecified = false;
        statusSpecified = false;
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

    public boolean containsReviewItem(ReviewItem reviewItem) {
        return !filterIsSet() || reviewitem2searchresult.containsKey(reviewItem);
    }

    public String getFilter() {
        return filter;
    }

    public boolean isEmpty() {
        return filterIsSet() && (review2searchresult.isEmpty() && reviewitem2searchresult.isEmpty());
    }

    public boolean isAuthorSpecified() {
        return authorSpecified;
    }

    public void setAuthorSpecified(boolean authorSpecified) {
        this.authorSpecified = authorSpecified;
    }

    public boolean isStatusSpecified() {
        return statusSpecified;
    }

    public void setStatusSpecified(boolean statusSpecified) {
        this.statusSpecified = statusSpecified;
    }

    public String[] getFilterKeywords() {
        return new String[]{AUTHOR, STATUS};
    }
}
