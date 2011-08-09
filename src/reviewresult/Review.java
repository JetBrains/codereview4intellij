package reviewresult;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewItem;

import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 7:02 PM
 */

public class Review {
    private ReviewBean reviewBean;
    private final Project project;
    private final VirtualFile virtualFile;

    private boolean activated = false;

    public static final DataKey<Review> REVIEW_DATA_KEY = DataKey.create("Review");
    public Review(@NotNull ReviewBean reviewBean, @NotNull Project project){
        this.reviewBean = reviewBean;
        this.project = project;
        this.virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(reviewBean.getFilePath());

        if(virtualFile == null)  {reviewBean.setValid(false); return;}
        this.reviewBean.checkValid(virtualFile.getLength(), virtualFile.isValid());
    }

    public Review(Project project, String reviewName, int start, int end, VirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;
        this.reviewBean = new ReviewBean(reviewName, start, end, virtualFile.getUrl());
        this.reviewBean.checkValid(virtualFile.getLength(), virtualFile.isValid());
    }


    public void addReviewItem(ReviewItem reviewItem) {
        reviewBean.addReviewItem(reviewItem);
    }

    public List<ReviewItem> getReviewItems(){
        return reviewBean.getReviewItems();
    }

    public String getName() {
        return reviewBean.getName();
    }

    public void setName(String name) {
        this.reviewBean.setName(name);
    }

    public int getStart() {
        return reviewBean.getStart();
    }

    @Nullable
    public OpenFileDescriptor getElement() {
        if(reviewBean.isValid())
            return new OpenFileDescriptor(project, virtualFile, reviewBean.getStart());
        else
            return null;
    }

    public boolean isValid() {
        return reviewBean.isValid()
          && ReviewManager.getInstance(project).getRootManager().getFileIndex().isInContent(virtualFile);
    }

    public int getLine() {
        if(!reviewBean.isValid()) return -1;
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        if(document == null) return -1;
        if(reviewBean.getStart()  > document.getText().length()) return -1;
        return document.getLineNumber(reviewBean.getStart());
    }


    public Project getProject() {
        return project;
    }

    public VirtualFile getVirtualFile() {
        return virtualFile;
    }

    public ReviewBean getReviewBean() {
        return reviewBean;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isActivated() {
        return activated;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Review review = (Review) o;
        return !(reviewBean != null ? !reviewBean.equals(review.reviewBean) : review.reviewBean != null);

    }

    @Override
    public int hashCode() {
        return reviewBean != null ? reviewBean.hashCode() : 0;
    }

    public void setReviewBean(ReviewBean reviewBean) {
        this.reviewBean = reviewBean;
    }

    public void setValid(boolean valid) {
        reviewBean.setValid(valid);
    }
}
