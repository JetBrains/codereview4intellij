package reviewresult;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.sun.jna.platform.win32.Guid;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 6:14 PM
 */

@Tag("review")
public class ReviewBean {
    private String guid = UUID.randomUUID().toString();
    private String name;
    private int start;
    private int end;
    private List<ReviewItem> reviewItems = new ArrayList<ReviewItem>();
    private String url;

    public ReviewBean() {
    }

    public ReviewBean(String name, int start, int end, String url) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.url = url;
    }


    @Attribute("name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Tag("start")
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }


    @Tag("end")
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }


    @Tag("review_items")
    @AbstractCollection(surroundWithTag = false)
    public List<ReviewItem> getReviewItems() {
        return reviewItems;
    }


    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }

    @Tag("filepath")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addReviewItem(ReviewItem reviewItem) {
        reviewItems.add(reviewItem);
    }

    public void addReviewItems(List<ReviewItem> reviewItems) {
        reviewItems.addAll(reviewItems);
    }

    @Attribute("id")
    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewBean that = (ReviewBean) o;

        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return guid != null ? guid.hashCode() : 0;
    }
}
