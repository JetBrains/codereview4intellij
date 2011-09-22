package reviewresult;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.text.DateFormatUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewItem;
import utils.Util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 7:02 PM
 */

public class Review{
    private ReviewBean reviewBean;
    private final Project project;

    public static final DataKey<Review> REVIEW_DATA_KEY = DataKey.create("Review");
    private String filePath;
    private String fileName;

    private boolean activated = false;

    //private static final Logger LOG = Logger.getInstance(Review.class.getName());
    private static final int INFO_LENGTH = 20;

    public Review(@NotNull ReviewBean reviewBean, @NotNull Project project, @NotNull String filePath){
        this.reviewBean = reviewBean;
        this.project = project;
        this.filePath = filePath;
        VirtualFile virtualFile = Util.getInstance(project).getVirtualFile(filePath);
        if(virtualFile == null)  {reviewBean.setValid(false); return;}
        fileName = virtualFile.getName();
        this.reviewBean.checkValid(virtualFile.getLength(), virtualFile.isValid());
        if(isValid()) {
            reviewBean.getContext().setContext(Util.getInstance(project).getDocument(filePath));
        }
    }

    public Review(Project project, int start, int end, VirtualFile virtualFile) {
        this.project = project;
        VirtualFile baseDir = project.getBaseDir();
        if(baseDir == null)  {return;}
        String relativePath = VfsUtil.getRelativePath(virtualFile, baseDir, '/');
        this.reviewBean = new ReviewBean(null, start, end);
        this.filePath = relativePath;
        fileName = virtualFile.getName();
        this.reviewBean.checkValid(virtualFile.getLength(), virtualFile.isValid());
        if(isValid()) {
            reviewBean.getContext().setContext(Util.getInstance(project).getDocument(filePath));
        }
    }

    public Project getProject() {
        return project;
    }

    public void addReviewItem(ReviewItem reviewItem) {
        reviewBean.addReviewItem(reviewItem);
    }

    public List<ReviewItem> getReviewItems(){
        return reviewBean.getReviewItems();
    }

    public boolean isValid() {
        final VirtualFile virtualFile = Util.getInstance(project).getVirtualFile(filePath);
        return virtualFile != null && reviewBean.isValid() && ProjectRootManager.getInstance(project).
                                                                getFileIndex().isInContent(virtualFile);
    }

    public void setValid(boolean valid) {
        reviewBean.setValid(valid);
    }

    public ReviewBean getReviewBean() {
        return reviewBean;
    }

    public void setReviewBean(ReviewBean reviewBean) {
        this.reviewBean = reviewBean;
    }

    public boolean isActivated() {
            return activated;
        }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        if(!reviewBean.isValid()) return -1;
        Document document = Util.getInstance(project).getDocument(filePath);
        if(document == null) return -1;
        if(reviewBean.getContext().getStart()  > document.getText().length()) return -1;
        return document.getLineNumber(reviewBean.getContext().getStart());
    }

    public String getPresentationInfo(boolean full) {
        if(reviewBean.getReviewItems().isEmpty())
            return "EMPTY COMMENT";
        final String text = reviewBean.getReviewItems().get(0).getText();
        if(full) return text;
        String infoText = text.split("\n")[0];
        return (infoText.length() > INFO_LENGTH) ?
                infoText.substring(0, INFO_LENGTH - 3) + "..." :
                infoText ;
    }

    public int getStart() {
        return reviewBean.getContext().getStart();
    }

    public void setStart(int newStart) {
        reviewBean.getContext().setStart(newStart);
    }

    public int getEnd() {
        return reviewBean.getContext().getEnd();
    }

    public void setEnd(int newEnd) {
        reviewBean.getContext().setEnd(newEnd);
    }

    public List<String> getTags() {
        return reviewBean.getTags();
    }

    public void setTags(List<String> tags) {
        reviewBean.setTags(tags);
    }

    public Date getDateOfCreation() {
        Date dateOfCreation = new Date();
        for(ReviewItem item : getReviewItems()) {
            final Date date = item.getDate();
            if(date.compareTo(dateOfCreation) < 0) {
                dateOfCreation = date;
            }
        }
        return dateOfCreation;
    }

    public List<String> getAuthors() {
        List<String> authors = new ArrayList<String>();
        for(ReviewItem item : reviewBean.getReviewItems()) {
            String author = item.getAuthor();
            if(!authors.contains(author)){
                authors.add(author);
            }
        }
        return authors;
    }

    public String getReviewText() {
        StringBuilder result = new StringBuilder("");
        for(ReviewItem item : reviewBean.getReviewItems()) {
            result.append(item.getAuthor());
            result.append(" reported ");
            result.append(DateFormatUtil.formatDateTime(item.getDate()));
            result.append("\n");
            result.append(item.getText());
            result.append("\n");
        }
        return result.toString();
    }

    @Nullable
    public ReviewItem getLastReviewItem() {
        final List<ReviewItem> reviewItems = reviewBean.getReviewItems();
        if(reviewItems.isEmpty()) return null;
        return reviewItems.get(reviewItems.size() - 1);
    }

    public String getLastCommenter() {
        String lastCommenter = "";
        Date lastCommentDate = new Date(0);
        for(ReviewItem item : getReviewItems()) {
            final Date date = item.getDate();
            if(date.compareTo(lastCommentDate) > 0) {
                lastCommentDate = date;
                lastCommenter = item.getAuthor();
            }
        }
        return lastCommenter;
    }

    @Nullable
    public String getFirstCommenter() {
        if(reviewBean.getReviewItems().isEmpty()) return null;
        return reviewBean.getReviewItems().get(0).getAuthor();
    }

    public String getLineBefore() {
        return reviewBean.getContext().getLineBefore();
    }

    public String getLine() {
        return reviewBean.getContext().getLine();
    }

    public String getLineAfter() {
        return reviewBean.getContext().getLineAfter();
    }

    public boolean isDeleted() {
        return reviewBean.isDeleted();
    }

    public void setDeleted(boolean deleted) {
        reviewBean.setDeleted(deleted);
    }

    public void changeContext(int lineStartOffset, int lineEndOffset) {
        setStart(lineStartOffset);
        setEnd(lineEndOffset);
        reviewBean.getContext().setContext(Util.getInstance(project).getDocument(filePath));
    }

    public void checkContext() {
        reviewBean.getContext().
                checkContext(Util.getInstance(project).getDocument(filePath));
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

}
