package reviewresult;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.file.exclude.PersistentFileSetManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import com.intellij.openapi.vfs.newvfs.persistent.PersistentFS;

import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 7:02 PM
 */

public class Review {
    private ReviewBean reviewBean;
    private Project project;
    private VirtualFile virtualFile;
    private boolean isValid = true;
    private boolean activated = false;

    public Review(ReviewBean reviewBean, Project project){
        this.reviewBean = reviewBean;
        this.project = project;
        this.virtualFile = VirtualFileManager.getInstance().refreshAndFindFileByUrl(reviewBean.getUrl());
        int start = reviewBean.getStart();
        int end = reviewBean.getEnd();
        isValid = (virtualFile != null && virtualFile.isValid() && start > 0 && end > 0 && start < end && end < virtualFile.getLength());
    }

    public Review(Project project, String reviewName, int start, int end, VirtualFile virtualFile) {
        this.project = project;
        this.virtualFile = virtualFile;
        this.reviewBean = new ReviewBean(reviewName, start, end, virtualFile.getUrl());
        isValid = (virtualFile != null && virtualFile.isValid() && start > 0 && end > 0 && start < end && end < virtualFile.getLength());
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

    public OpenFileDescriptor getElement() {
        if(isValid)
            return new OpenFileDescriptor(project, virtualFile, reviewBean.getStart());
        else
            return null;
    }

    public boolean isValid() {
        return isValid;
    }

    public int getLine() {
            Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
            if(!isValid || document == null) return -1;
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
}
