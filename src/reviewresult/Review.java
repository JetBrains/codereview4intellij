package reviewresult;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 7:02 PM
 */
public class Review {
    private List<ReviewItem> reviewItems;
    private String url;
    private int lineNumber;

    public Review() {
        reviewItems = new ArrayList<ReviewItem>();
    }

    public Review(String text, ReviewStatus status, String url, int lineNumber) {
        this.url = url;
        this.lineNumber = lineNumber;
        reviewItems = new ArrayList<ReviewItem>();
        reviewItems.add(new ReviewItem(text, status));
    }

    public void addReviewItem(String text, ReviewStatus status) {
        reviewItems.add(new ReviewItem(text, status));
    }

    public ReviewItem getReviewItem(int i) {
        return reviewItems.get(i);
    }

    public List<ReviewItem> getReviewItems(){
        return reviewItems;
    }

    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

}
