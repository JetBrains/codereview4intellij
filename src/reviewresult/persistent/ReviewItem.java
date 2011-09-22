package reviewresult.persistent;


import com.intellij.util.text.DateFormatUtil;
import com.intellij.util.xmlb.annotations.Tag;
import ui.reviewtoolwindow.filter.Searcher;

import java.util.Date;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:37 PM
 */
@Tag("review_item")
public class ReviewItem {
    private Date date = new Date();
    private String author;
    private String text = "";

    @SuppressWarnings({"UnusedDeclaration"})
    public ReviewItem() {
    }

    public ReviewItem(String text) {
        this.author = System.getProperty("user.name");
        this.text = text;
        this.date = new Date();
    }

    @Tag("author")
    public String getAuthor() {
        return author;
    }

    @SuppressWarnings({"UnusedDeclaration"})
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

        return author.equals(that.getAuthor())
                && date.equals(that.getDate())
                && text.equals(that.getText());

    }

    @Override
    public int hashCode() {
        int result = date.hashCode();
        result = 31 * result + author.hashCode();
        result = 31 * result + text.hashCode();
        return result;
    }

    public boolean isMine() {
        return author.equals(System.getProperty("user.name"));
    }

    public String getHtmlReport(Searcher searcher) {
        final String filter = searcher.getFilter();
        String result = text;
        if(!"".equals(filter)) {
             result = result.replace(filter, "<span class=\"highlight\">" + filter + "</span>");
        }
        result = "<strong>" + author + "</strong> at " +
                 DateFormatUtil.formatPrettyDateTime(date) +  " added: <br/> " +
                 result + " <br/>";
        return result;
    }
}
