package reviewresult.persistent;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import org.apache.commons.lang.Validate;
import org.jetbrains.annotations.NotNull;
import ui.actions.DeleteReviewAction;

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
    private String filePath;

    private boolean isValid = true;
    private boolean isDeleted;

    private List<ReviewItem> reviewItems = new ArrayList<ReviewItem>();

    public ReviewBean() {}

    public ReviewBean(String name, int start, int end, String filePath) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.filePath = filePath;
    }

    public void checkValid(long length, boolean isFileValid) {
            setValid(isValid()
                    && isFileValid
                    && start > 0 && end > 0
                    && start <= end
                    && end < length);
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
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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


    @Tag("valid")
    public boolean isValid() {
        //checkValid();
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    @Tag("deleted")
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isValid = !deleted;
        isDeleted = deleted;
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
