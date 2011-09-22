package ui.reviewtoolwindow.filter;

import com.intellij.openapi.project.Project;
import reviewresult.ReviewManager;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 9/22/11
 * Time: 3:38 PM
 */

public class Filter {
    private String status = "";
    private String author = "";
    private String tag = "";

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
