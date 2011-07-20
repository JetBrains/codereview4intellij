package reviewresult;


import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;

import java.util.Date;
import java.util.Random;

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
        Random random = new Random();
        this.text = text;// + " " + String.valueOf(random.nextLong());
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
        //Random random = new Random();
        this.text = text;// + " " + String.valueOf(random.nextLong());
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewItem that = (ReviewItem) o;

        if (!author.equals(that.author)) return false;
        if (!date.equals(that.date)) return false;
        if (status != that.status) return false;
        if (!text.equals(that.text)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + text.hashCode();
        result = 31 * result + status.hashCode();
        return result;
    }
}
