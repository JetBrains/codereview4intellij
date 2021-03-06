package ui.reviewtoolwindow.filter;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Pair;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewItem;
import utils.ReviewsBundle;
import utils.Util;

import java.util.*;

/**
 * User: Alisa.Afonina
 * Date: 8/5/11
 * Time: 7:40 PM
 */
public class Searcher  extends AbstractProjectComponent {
    private Map<Review, Pair<Integer, Integer>> review2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
    private Map<ReviewItem, Pair<Integer, Integer>> reviewitem2searchresult =
                                                            new HashMap<ReviewItem, Pair<Integer, Integer>>();
    private Set<String> filteredFileNames = new HashSet<String>();

    private final Project project;

    private boolean authorSpecified;
    private boolean statusSpecified;
    private boolean tagSpecified;

    private String filterText = "";
    private Searcher.AdditionalFilter additionalFilter  = new AdditionalFilter();
    private boolean caseSensitive = false;

    protected Searcher(Project project) {
        super(project);
        this.project = project;
    }

    public static Searcher getInstance(@NotNull Project project) {
        return project.getComponent(Searcher.class);
    }

    public boolean filterIsSet(){
        return additionalFilterIsSet() || !"".equals(filterText);
    }

    public boolean additionalFilterIsSet(){
        return authorSpecified || statusSpecified || tagSpecified;
    }

    public Set<String> getFilteredFileNames() {
        if(filterIsSet()) {
            return Collections.unmodifiableSet(filteredFileNames);
        } else return ReviewManager.getInstance(project).getFileNames();
    }

    public void createFilter(String text) {
        review2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
        reviewitem2searchresult = new HashMap<ReviewItem, Pair<Integer, Integer>>();
        filteredFileNames = new HashSet<String>();
        filterText = text;
        extractAuthor();
        //extractStatus();
        extractTag();

        ReviewManager instance = ReviewManager.getInstance(myProject);
        Set<String> fileNames = instance.getFileNames();
        if(fileNames == null) return;
        for(String url : fileNames) {
            List<Review> validReviews = instance.getValidReviews(url);
            if(validReviews == null) return;
            for(Review review : validReviews) {
                addSearchResult(review);
            }
        }
    }

    private void extractTag() {
        final int tagIndex = Util.find(filterText, additionalFilter.TAG, caseSensitive);
        if(tagIndex != -1) {
            tagSpecified = true;
            final int beginTagName = tagIndex + additionalFilter.TAG.length();

            final int beginIndex = filterText.indexOf("\"", beginTagName);
            if(beginIndex < 0)  {
                Messages.showWarningDialog(ReviewsBundle.message("reviews.tagQuotesMissingMessage"),
                                           ReviewsBundle.message("reviews.tagQuotesMissing"));
                return;
            }

            final int endIndex = filterText.indexOf("\"", beginIndex + 1);
            if(endIndex == beginIndex)  {
                Messages.showWarningDialog(ReviewsBundle.message("reviews.tagNotClosedMessage"),
                                                                 ReviewsBundle.message("reviews.tagNotClosed"));
                return;
            }

            additionalFilter.setTag(filterText.substring(beginIndex + 1, endIndex));
            if("".equals(additionalFilter.getTag().trim())) {
                Messages.showErrorDialog(ReviewsBundle.message("reviews.noTagMessage"),
                                                                ReviewsBundle.message("reviews.noTag"));
                if(tagIndex > 0) {
                    filterText = filterText.substring(0, tagIndex);
                }
            }
            else {
                filterText = filterText.substring(endIndex + 1).trim();
            }
        }
    }

   /* private void extractStatus() {
        final int statusIndex = Util.find(filterText, AdditionalFilter.STATUS);
        if(statusIndex != -1) {
            statusSpecified = true;
            final int beginStatusName = statusIndex + AdditionalFilter.STATUS.length();
            final String[] split = filterText.substring(beginStatusName).split(" ");
            if(split.length == 1 && split[0].equals(AdditionalFilter.STATUS)) {
                Messages.showErrorDialog("No status was specified for selection", "No Status");
                if(statusIndex > 0) {
                    filterText = filterText.substring(0, statusIndex);
                }
            }
            else {
                additionalFilter.setStatus(split[0]);
                filterText = filterText.substring(beginStatusName + additionalFilter.getStatus().length()).trim();
            }
        }
    }*/

    private void extractAuthor() {
        final int authorIndex = Util.find(filterText, additionalFilter.AUTHOR, caseSensitive);
        if(authorIndex != -1) {
            authorSpecified = true;
            final int beginAuthorName = authorIndex + additionalFilter.AUTHOR.length();

            final int beginIndex = filterText.indexOf("\"", beginAuthorName);
            if(beginIndex < 0)  {
                Messages.showWarningDialog(ReviewsBundle.message("reviews.authorQuotesMissingMessage"),
                                           ReviewsBundle.message("reviews.authorQuotesMissing"));
                return;
            }

            final int endIndex = filterText.indexOf("\"", beginIndex + 1);
            if(endIndex == beginIndex)  {
                Messages.showWarningDialog(ReviewsBundle.message("reviews.authorNameNotClosedMessage"),
                                                                 ReviewsBundle.message("reviews.authorNameNotClosed"));
                return;
            }

            additionalFilter.setAuthor(filterText.substring(beginIndex + 1, endIndex));
            if("".equals(additionalFilter.getAuthor().trim())) {
                Messages.showErrorDialog(ReviewsBundle.message("reviews.noAuthorMessage"),
                                         ReviewsBundle.message("reviews.noAuthor"));
                if(authorIndex > 0) {
                    filterText = filterText.substring(0, authorIndex);
                }
            }
            else {
                filterText = filterText.substring(endIndex + 1).trim();
            }
        }
    }

    public void addSearchResult(Review review) {
        boolean contains = false;
        boolean containsTag = false;
        if(tagSpecified) {
            for(String existingTag : review.getTags()) {
                if(existingTag.compareToIgnoreCase(additionalFilter.getTag()) == 0) {
                    containsTag = true;
                }
            }
        }
        boolean containsStatus = false;
        /*if(statusSpecified) {
            if(review.getStatus().name().compareToIgnoreCase(status) == 0) {
                containsStatus = true;
            }
        }*/
        for(ReviewItem item : review.getReviewItems()) {

            boolean containsAuthor = false;
            if(authorSpecified) {
                if(item.getAuthor().compareToIgnoreCase(additionalFilter.getAuthor()) == 0) {
                    containsAuthor = true;
                }
            }

            final boolean additionalFiltersSpecified = authorSpecified || statusSpecified || tagSpecified;
            final boolean additionalFiltersExist = !(authorSpecified ^ containsAuthor) &&
                                         !(statusSpecified ^ containsStatus) &&
                                        !(tagSpecified ^ containsTag) && additionalFiltersSpecified;

            if("".equals(filterText) && additionalFiltersExist) {
                Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(-2, -2);
                reviewitem2searchresult.put(item, itemResult);
                contains = true;
            }
            final boolean filterExists = !(additionalFiltersSpecified ^ additionalFiltersExist);
            if(!(additionalFilter == null || "".equals(filterText)) && filterExists) {
                int itemStart = Util.find(item.getText(), filterText, caseSensitive);
                int itemEnd;
                if(itemStart != -1) {
                    contains = true;
                    if(itemStart >= 0) {
                        itemEnd = itemStart + filterText.length();
                        Pair<Integer, Integer> itemResult = new Pair<Integer, Integer>(itemStart, itemEnd);
                        reviewitem2searchresult.put(item, itemResult);
                    }
               }
            }
        }
        if(contains) {
            Pair<Integer, Integer> reviewResult = new Pair<Integer, Integer>(-2, -2);
            if(additionalFilter != null && !"".equals(filterText) ) {
                int reviewStart = Util.find(review.getPresentationInfo(false), filterText, caseSensitive);
                int reviewEnd;

                if(reviewStart >= 0) {
                    reviewEnd = reviewStart + filterText.length();
                    reviewResult = new Pair<Integer, Integer>(reviewStart, reviewEnd);
                }
            }
            review2searchresult.put(review, reviewResult);
            filteredFileNames.add(review.getFilePath());
        }
    }

    public void emptyFilter() {
        filterText = "";
        authorSpecified = false;
        statusSpecified = false;
        tagSpecified = false;
        review2searchresult = new HashMap<Review, Pair<Integer, Integer>>();
        reviewitem2searchresult = new HashMap<ReviewItem, Pair<Integer, Integer>>();
        filteredFileNames = new HashSet<String>();
    }

    public String getAdditionalFilterText() {
        String result = "";
        result += (tagSpecified) ? ReviewsBundle.message("reviews.tag") + " " + additionalFilter.getTag() + " ": "";
        result += (authorSpecified) ? ReviewsBundle.message("reviews.author") + " " + additionalFilter.getAuthor() + " ": "";
        return result;
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
        return filterText;
    }

    public boolean isEmpty() {
        return filterIsSet() && (review2searchresult.isEmpty() && reviewitem2searchresult.isEmpty());
    }

    public String[] getFilterKeywords() {
        return additionalFilter.getFilterKeywords();
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }


    private class AdditionalFilter {
    public final String AUTHOR = ReviewsBundle.message("reviews.author");
    //public static final String STATUS = "status:";
    public final String TAG = ReviewsBundle.message("reviews.tag");

    private String status = "";
    private String author = "";
    private String tag = "";

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
        
    private String[] getFilterKeywords() {
        List<String> keywords = new ArrayList<String>();
        final ReviewManager instance = ReviewManager.getInstance(project);
        if(instance.getAvailableTags().length != 0 && !tagSpecified) {
            keywords.add(TAG);
        }
        if(instance.getAuthors().length != 0 && !authorSpecified) {
            keywords.add(AUTHOR);
        }
        return keywords.toArray(new String[keywords.size()]);
    }
}
}


