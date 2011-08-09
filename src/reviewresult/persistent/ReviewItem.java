package reviewresult.persistent;


import com.intellij.util.xmlb.annotations.Attribute;
import com.intellij.util.xmlb.annotations.Tag;
import com.intellij.util.xmlb.annotations.Transient;
import reviewresult.ReviewStatus;

import java.util.Date;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:37 PM
 */
@Tag("review_item")
public class ReviewItem {
    private Date date = new Date();
    private String author;//= System.getProperty("user.name");
    private String text = "";// = "Add comment";
    private ReviewStatus status;

    public ReviewItem() {
    }

    public ReviewItem(String text, ReviewStatus status) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReviewItem that = (ReviewItem) o;

        if (!author.equals(that.author)) return false;
        if (!date.equals(that.date)) return false;
        if (status != that.status) return false;
        return text.equals(that.text);

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
