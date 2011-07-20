package reviewresult;

import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 7:02 PM
 */
@Tag("review")
public class Review {
    private List<ReviewItem> reviewItems = new ArrayList<ReviewItem>();
    private Project project;
    @Transient
    private RangeMarker rangeMarker;
    private String url;
    private int start;
    private int end;
    private String name;
    public Review() {
    }

    public Review(Project project, String reviewName, RangeMarker rangeMarker, VirtualFile virtualFile) {
        this.project = project;
        this.rangeMarker = rangeMarker;
        this.name = reviewName;
        start = rangeMarker.getStartOffset();
        end = rangeMarker.getEndOffset();
        url = virtualFile.getUrl();
        reviewItems = new ArrayList<ReviewItem>();
    }

    @Tag("review_items")
    @AbstractCollection(surroundWithTag = false)
    public List<ReviewItem> getReviewItems(){
        return reviewItems;
    }

    public void setReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems = reviewItems;
    }

    public void addReviewItem(ReviewItem reviewItem) {

        reviewItems.add(reviewItem);
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

    @Tag("url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OpenFileDescriptor getElement() {
        VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(url);
        OpenFileDescriptor element = new OpenFileDescriptor(project, virtualFile, start);
        return element;
    }

    @Transient
    public void setProject(Project project) {
        this.project = project;
    }

    public void addReviewItems(List<ReviewItem> reviewItems) {
        this.reviewItems.addAll(reviewItems);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;

        if (end != review.end) return false;
        if (start != review.start) return false;
        if (name != null ? !name.equals(review.name) : review.name != null) return false;
        if (url != null ? !url.equals(review.url) : review.url != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = url != null ? url.hashCode() : 0;
        result = 31 * result + start;
        result = 31 * result + end;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
