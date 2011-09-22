package reviewresult.persistent;

import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import reviewresult.ReviewStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User: Alisa.Afonina
 * Date: 7/20/11
 * Time: 6:14 PM
 */

@SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"})
@Tag("review")

public class ReviewBean {
    private String guid = UUID.randomUUID().toString();
    private String name;
    private ReviewStatus status;
    private Context context;
    private boolean isValid = true;
    private boolean isDeleted;

    private List<ReviewItem> reviewItems = new ArrayList<ReviewItem>();
    private List<String> tags = new ArrayList<String>();

    @SuppressWarnings({"UnusedDeclaration"})
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

    @SuppressWarnings({"UnusedDeclaration"})
    public void setName(String name) {
        this.name = name;
    }


    @Tag("review_items")
    @AbstractCollection(surroundWithTag = false)
    public List<ReviewItem> getReviewItems() {
        return reviewItems;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }


    public void addReviewItem(ReviewItem reviewItem) {
        reviewItems.add(reviewItem);
    }

    @Attribute("id")
    @SuppressWarnings({"UnusedDeclaration"})
    public String getGuid() {
        return guid;
    }

    @SuppressWarnings({"UnusedDeclaration"})
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
        isDeleted = deleted;
    }

    @Tag("status")
    public ReviewStatus getStatus() {
        return status;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setStatus(ReviewStatus status) {
        this.status = status;
    }



    @Tag("context")
    public Context getContext() {
        return context;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public void setContext(Context context) {
        this.context = context;
    }


    @Tag("tags")
    @AbstractCollection(surroundWithTag = false, elementTag = "tag")
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

      @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewBean that = (ReviewBean) o;
        return !(guid != null ? !guid.equals(that.getGuid()) : that.getGuid() != null);

    }

    @Override
    public int hashCode() {
        return guid != null ? guid.hashCode() : 0;
    }
}
