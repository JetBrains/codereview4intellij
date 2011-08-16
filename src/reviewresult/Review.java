package reviewresult;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
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

    private boolean activated = false;

    public static final DataKey<Review> REVIEW_DATA_KEY = DataKey.create("Review");
    public Review(@NotNull ReviewBean reviewBean, @NotNull Project project){
        this.reviewBean = reviewBean;
        this.project = project;
        final VirtualFile baseDir = project.getBaseDir();
        if(baseDir == null)  {reviewBean.setValid(false); return;}
        VirtualFile virtualFile = baseDir.findFileByRelativePath(reviewBean.getFilePath());
        if(virtualFile == null)  {reviewBean.setValid(false); return;}

        this.reviewBean.checkValid(virtualFile.getLength(), virtualFile.isValid());
    }

    public Review(Project project, String reviewName, int start, int end, VirtualFile virtualFile) {
        this.project = project;
        VirtualFile baseDir = project.getBaseDir();
        if(baseDir == null)  {return;}
        String relativePath = VfsUtil.getRelativePath(virtualFile, baseDir, '/');
        this.reviewBean = new ReviewBean(reviewName, start, end, relativePath);
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
            return new OpenFileDescriptor(project, getVirtualFile() , reviewBean.getStart());
        else
            return null;
    }

    public boolean isValid() {
        return reviewBean.isValid()
          && ProjectRootManager.getInstance(project).getFileIndex().isInContent(getVirtualFile());
    }

    public int getLine() {
        if(!reviewBean.isValid()) return -1;
        Document document = FileDocumentManager.getInstance().getDocument(getVirtualFile());
        if(document == null) return -1;
        if(reviewBean.getStart()  > document.getText().length()) return -1;
        return document.getLineNumber(reviewBean.getStart());
    }


    public Project getProject() {
        return project;
    }

    public VirtualFile getVirtualFile() {
        final VirtualFile baseDir = project.getBaseDir();
        if(baseDir == null)  {reviewBean.setValid(false); return null;}
        return baseDir.findFileByRelativePath(reviewBean.getFilePath());
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
        return !(reviewBean != null ? !reviewBean.equals(review.getReviewBean()) : review.getReviewBean() != null);

    }

    @Override
    public int hashCode() {
        return reviewBean != null ? reviewBean.hashCode() : 0;
    }

    public void setValid(boolean valid) {
        reviewBean.setValid(valid);
    }

    public void setLine(int line) {
        Document document = FileDocumentManager.getInstance().getDocument(getVirtualFile());
        if(document == null) return;
        reviewBean.setStart(document.getLineStartOffset(line));
        reviewBean.setEnd(document.getLineEndOffset(line));
    }

    public void setReviewBean(ReviewBean reviewBean) {
        this.reviewBean = reviewBean;
    }
}
