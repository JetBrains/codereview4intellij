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
    private String name = "Note";

    public Review() {
    }

    public Review(Project project, String text, ReviewStatus status, RangeMarker rangeMarker, PsiFile psiFile) {
        this.project = project;
        this.rangeMarker = rangeMarker;
        start = rangeMarker.getStartOffset();
        end = rangeMarker.getEndOffset();
        url = psiFile.getVirtualFile().getUrl();
        reviewItems = new ArrayList<ReviewItem>();
        reviewItems.add(new ReviewItem(project, text, status));
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
}
