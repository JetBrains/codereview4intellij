package ui.reviewtoolwindow;

/**
 * User: Alisa.Afonina
 * Date: 8/3/11
 * Time: 3:56 PM
 */
public class ReviewToolWindowSettings {
    private boolean groupByModule;
    private boolean groupByFile;
    private boolean searchEnabled;
    private boolean isShowPreview;

    public ReviewToolWindowSettings() {}

    public boolean isGroupByModule() {
        return groupByModule;
    }

    public void setGroupByModule(boolean groupByModule) {
        this.groupByModule = groupByModule;
    }

    public boolean isGroupByFile() {
        return groupByFile;
    }

    public void setGroupByFile(boolean groupByFile) {
        this.groupByFile = groupByFile;
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public boolean isShowPreview() {
        return isShowPreview;
    }

    public void setShowPreview(boolean showPreview) {
        isShowPreview = showPreview;
    }


}
