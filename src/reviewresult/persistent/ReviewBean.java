package reviewresult.persistent;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

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
    private Context context;
    private boolean isValid = true;
    private boolean isDeleted;

    private List<ReviewItem> reviewItems = new ArrayList<ReviewItem>();

    public ReviewBean() {}

    public ReviewBean(String name, int start, int end) {
        this.name = name;
        this.context = new Context(start, end);
    }

    public void checkValid(long length, boolean isFileValid) {
        final int start = context.getStart();
        final int end = context.getEnd();
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


    @Tag("review_items")
    @AbstractCollection(surroundWithTag = false)
    public List<ReviewItem> getReviewItems() {
        return reviewItems;
    }


    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
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
        isDeleted = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewBean that = (ReviewBean) o;

        return !(guid != null ? !guid.equals(that.guid) : that.guid != null);

    }

    @Override
    public int hashCode() {
        return guid != null ? guid.hashCode() : 0;
    }

    @Tag("context")
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }


}
