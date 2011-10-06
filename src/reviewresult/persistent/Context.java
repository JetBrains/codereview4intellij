package reviewresult.persistent;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.xmlb.annotations.Tag;
import utils.Util;

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
    private int lineNumber;
    private int afterLineNumber;
    private int beforeLineNumber;

    public Context() {
    }

    public Context(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @Tag("line")
    public String getLine() {
        return "<span class=\"context_line\">" +
                    line.replaceAll("\n", "<br/>") +
                     "</span>";
    }

    public void setLine(String line) {
        this.line = line;
    }

    @Tag("line_number")
    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }


    @Tag("line_after_number")
    public int getAfterLineNumber() {
        return afterLineNumber;
    }

    public void setAfterLineNumber(int lineNumber) {
        this.afterLineNumber = lineNumber;
    }


    @Tag("line_before_number")
    public int getBeforeLineNumber() {
        return beforeLineNumber;
    }

    public void setBeforeLineNumber(int lineNumber) {
        this.beforeLineNumber = lineNumber;
    }


    @Tag("line_before")
    public String getLineBefore() {
        return lineBefore.replaceAll("\n", "<br/>");
    }

    public void setLineBefore(String lineBefore) {
        this.lineBefore = lineBefore;
    }

    @Tag("line_after")
    public String getLineAfter() {
        return lineAfter.replaceAll("\n", "<br/>");
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

    public void setLineText(Document document) {
        if(document == null) return;
        setLineNumber(document.getLineNumber(getStart()) + 1);
        setLine(document.getText(new TextRange(getStart(), getEnd())));
     }

     public void setContext(Document document) {
        setLineText(document);
        setBeforeLineText(document);
        setAfterLineText(document);
    }

    public void setBeforeLineText(Document document) {
        if(document == null) return;
        int beforeLineNumber = document.getLineNumber(getStart()) - 1;
        if(beforeLineNumber >=0) {
            int start = document.getLineStartOffset(beforeLineNumber);
            setBeforeLineNumber(document.getLineNumber(start) + 1);
            setLineBefore(document.getText(new TextRange(start, getStart())));
        }
    }

    public void setAfterLineText(Document document) {
        if(document == null) return;
        int afterLineNumber = document.getLineNumber(getEnd()) + 1;
        if(afterLineNumber < document.getLineCount()) {
            int end = document.getLineEndOffset(afterLineNumber);
            setAfterLineNumber(document.getLineNumber(getEnd()) + 1);
            setLineAfter(document.getText(new TextRange(getEnd(), end)));
        }
    }

    public void checkContext(Document document) {
        if(document == null || "".equals(document.getText())) return;
        final String text = document.getText();
        final int beforeOffset = Util.find(text, getLineBefore(), true);
        final int afterOffset = Util.find(text, getLineAfter(), true);
        if(!getLine().equals(document.getText(new TextRange(getStart(), getEnd())))) {
            int offset = Util.find(text, getLine(), true);
            if(offset > 0) {
                if(offset != getStart()) {
                    // todo check context around
                    start = offset;
                    end = document.getLineEndOffset(document.getLineNumber(offset));
                    setBeforeLineText(document);
                    setAfterLineText(document);
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
     //           LOG.warn("May be reviewPoint would be wrond placed, because text was increeeedibly changed\n" +
     //             " Review name: " + reviewBean.getName() + " in " + getFileName() + " line " + getLineNumber());
            }
        }
        setStart(start);
        setEnd(end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return end == context.getEnd() && start == context.getStart();
    }

    @Override
    public int hashCode() {
        int result = start;
        result = 31 * result + end;
        return result;
    }
}
