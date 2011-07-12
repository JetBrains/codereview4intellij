package reviewresult;


import java.util.Date;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:37 PM
 */
public class ReviewItem {
    private String author;
    private String text;
    private ReviewStatus status;
    private Date date;

    public ReviewItem() {
    }

    public ReviewItem(String text, ReviewStatus status) {
        this.author = System.getProperty("user.name");
        this.text = text;
        this.date = new Date();
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public void setStatus(ReviewStatus status) {
        this.status = status;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }


}
