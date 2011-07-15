package reviewresult;


import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.Date;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:37 PM
 */
@Tag("review_item")
public class ReviewItem {
    private Date date;
    private String author;
    private String text;
    private ReviewStatus status;

    public ReviewItem() {
    }

    public ReviewItem(Project project, String text, ReviewStatus status) {
        this.author = System.getProperty("user.name");
        this.text = text;
        this.date = new Date();
        this.status = status;
    }

    @Tag("author")
    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @Tag("text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Attribute("status")
    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    @Tag("date")
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

}
