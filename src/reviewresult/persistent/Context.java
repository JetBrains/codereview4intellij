package reviewresult.persistent;

import com.intellij.util.xmlb.annotations.Tag;

/**
 * User: Alisa.Afonina
 * Date: 8/17/11
 * Time: 12:57 PM
 */

public class Context {
    private String line = "";
    private String lineBefore = "";
    private String lineAfter = "";
    private int start = -1;
    private int end = -1;

    public Context() {
    }

    public Context(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Tag("line")
    public String getLine() {
        return line;
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Tag("line_before")
    public String getLineBefore() {
        return lineBefore;
    }

    public void setLineBefore(String lineBefore) {
        this.lineBefore = lineBefore;
    }

    @Tag("line_after")
    public String getLineAfter() {
        return lineAfter;
    }

    public void setLineAfter(String lineAfter) {
        this.lineAfter = lineAfter;
    }

    @Tag("start")
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @Tag("end")
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }


}
