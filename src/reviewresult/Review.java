package reviewresult;

import com.intellij.openapi.actionSystem.DataKey;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import reviewresult.persistent.Context;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewItem;
import utils.Util;

import java.util.*;

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

    private static final Logger LOG = Logger.getInstance(Review.class.getName());
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
            setLineText();
            setBeforeLineText();
            setAfterLineText();
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
            setLineText();
            setBeforeLineText();
            setAfterLineText();
        }
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
        return reviewBean.getContext().getStart();
    }

    public boolean isValid() {
        final VirtualFile virtualFile = Util.getInstance(project).getVirtualFile(filePath);
        return virtualFile != null && reviewBean.isValid() && ProjectRootManager.getInstance(project).
                                                                getFileIndex().isInContent(virtualFile);
    }

    public int getLineNumber() {
        if(!reviewBean.isValid()) return -1;
        Document document = Util.getInstance(project).getDocument(filePath);
        if(document == null) return -1;
        if(reviewBean.getContext().getStart()  > document.getText().length()) return -1;
        return document.getLineNumber(reviewBean.getContext().getStart());
    }


    public Project getProject() {
        return project;
    }

    public ReviewBean getReviewBean() {
        return reviewBean;
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

    public void setReviewBean(ReviewBean reviewBean) {
        this.reviewBean = reviewBean;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

     public void setLineText() {
        Document document = Util.getInstance(project).getDocument(filePath);
        if(document == null) return;
        final Context context = reviewBean.getContext();
        context.setLine(document.getText(new TextRange(context.getStart(), context.getEnd())));
     }

    public void setBeforeLineText() {
        int beforeLineNumber = getLineNumber() - 1;
        if(beforeLineNumber >=0) {
            Document document = Util.getInstance(project).getDocument(filePath);
            if(document == null) return;
            int start = document.getLineStartOffset(beforeLineNumber);
            //int end = document.getLineEndOffset(beforeLineNumber);
            final Context context = reviewBean.getContext();
            context.setLineBefore(document.getText(new TextRange(start, getStart())));
        }
    }

    public void setAfterLineText() {
        Document document = Util.getInstance(project).getDocument(filePath);
        if(document == null) return;
        int afterLineNumber = document.getLineNumber(reviewBean.getContext().getEnd()) + 1;
        if(afterLineNumber < document.getLineCount()) {
            //int start = document.getLineStartOffset(afterLineNumber);
            int end = document.getLineEndOffset(afterLineNumber);
            final Context context = reviewBean.getContext();
            context.setLineAfter(document.getText(new TextRange(getEnd(), end)));
        }
    }

    public void checkContext() {
        Document document = Util.getInstance(project).getDocument(filePath);
        if(document == null) return;
        final Context context = reviewBean.getContext();
        int start = context.getStart();
        int end = context.getEnd();
        final int beforeOffset = Util.find(document.getText(), context.getLineBefore());
        final int afterOffset = Util.find(document.getText(), context.getLineAfter());
        if(!context.getLine().equals(document.getText(new TextRange(context.getStart(), context.getEnd())))) {
            int offset = Util.find(document.getText(), context.getLine());
            if(offset > 0) {
                if(offset != context.getStart()) {
                    // todo check context around
                    start = offset;
                    end = document.getLineEndOffset(document.getLineNumber(offset));
                    setBeforeLineText();
                    setAfterLineText();
                }
            } else {
                if(beforeOffset > 0 && afterOffset > 0) {
                    final int beforeLineNumber = document.getLineNumber(beforeOffset);
                    final int afterLineNumber = document.getLineNumber(afterOffset);

                    if(beforeLineNumber < document.getLineCount() && afterLineNumber > 0 ) {
                        //todo since comment is not to one line anymore - change it
                        if((beforeLineNumber + 2)==afterLineNumber) { // if between these two lines exactly one placed;
                            start = document.getLineStartOffset(beforeLineNumber + 1);
                            end = document.getLineEndOffset(beforeLineNumber + 1);
                        } else {
                            //todo best line between before and after lines should be found
                            start = document.getLineStartOffset(beforeLineNumber + 1);
                            end = document.getLineEndOffset(beforeLineNumber + 1);
                        }
                    } else {
                        if (beforeLineNumber < document.getLineCount()) {
                            start = document.getLineStartOffset(afterLineNumber - 1);
                            end = document.getLineEndOffset(afterLineNumber - 1);
                        } else {
                            start = document.getLineStartOffset(beforeLineNumber - 1);
                            end = document.getLineEndOffset(beforeLineNumber - 1);
                        }
                    }
                }
               //todo check at least for one
                LOG.warn("May be reviewPoint would be wrond placed, because text was increeeedibly changed\n" +
                    " Review name: " + reviewBean.getName() + " in " + getFileName() + " line " + getLineNumber());
            }
        }
        context.setStart(start);
        context.setEnd(end);
    }

    public String getFileName() {
        return fileName;
    }

    public void addTag(String tag) {
        List<String> tags = reviewBean.getTags();
        if(!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void deleteTag(String tag) {
        List<String> tags = reviewBean.getTags();
        tags.remove(tag);
    }

    public String getPresentationInfo(boolean full) {
        if(reviewBean.getReviewItems().isEmpty())
            return "EMPTY COMMENT";
        final String text = reviewBean.getReviewItems().get(0).getText();
        if(full)return text;
        String infoText = text.split("\n")[0];
        return (infoText.length() > INFO_LENGTH) ? infoText.substring(0, INFO_LENGTH - 3) + "..." : infoText ;
    }

    public int getEnd() {
        return reviewBean.getContext().getEnd();
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
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

    public Date getFirstDate() {
        Date firstCommentDate = new Date();
        for(ReviewItem item : getReviewItems()) {
            final Date date = item.getDate();
            if(date.compareTo(firstCommentDate) < 0) {
                firstCommentDate = date;
            }
        }
        return firstCommentDate;
    }

    public String getAuthor() {
        return reviewBean.getReviewItems().get(0).getAuthor();
    }

    public void addTags(List<String> tags) {
        reviewBean.getTags().addAll(tags);
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
}
