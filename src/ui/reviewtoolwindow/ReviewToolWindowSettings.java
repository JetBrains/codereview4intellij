package ui.reviewtoolwindow;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

/**
 * User: Alisa.Afonina
 * Date: 8/3/11
 * Time: 3:56 PM
 */

public class ReviewToolWindowSettings {
    private boolean groupByModule;
    private boolean groupByFile;
    private boolean searchEnabled;
    private boolean showPreviewEnabled;

    private final Project project;
    private boolean enabled = true;

    private boolean sortByDate = true;
    private boolean sortByStatus = false;
    private boolean sortByAuthor = false;
    private boolean sortByOffset = false;
    private boolean sortByLastCommenter = false;

    public ReviewToolWindowSettings(Project project) {
        this.project = project;
        loadState();
    }

    public boolean isGroupByModule() {
        return groupByModule;
    }

    public void setGroupByModule(boolean groupByModule) {
        this.groupByModule = groupByModule;
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.moduleEnabled", String.valueOf(groupByModule));
        }

    public boolean isGroupByFile() {
        return groupByFile;
    }

    public void setGroupByFile(boolean groupByFile) {
        this.groupByFile = groupByFile;
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.fileEnabled", String.valueOf(groupByFile));
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public boolean isShowPreviewEnabled() {
        return showPreviewEnabled;
    }

    public void setShowPreviewEnabled(boolean showPreviewEnabled) {
        this.showPreviewEnabled = showPreviewEnabled;
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.previewEnabled", String.valueOf(this.showPreviewEnabled));
    }


    public void saveState() {
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.fileEnabled", String.valueOf(groupByFile));
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.moduleEnabled", String.valueOf(groupByModule));
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.previewEnabled", String.valueOf(showPreviewEnabled));
    }


    private void loadState() {
        groupByFile = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.fileEnabled", false);
        groupByModule = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.moduleEnabled", false);
        showPreviewEnabled = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.previewEnabled", false);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSortByDate() {
        return !sortByLastCommenter && !sortByAuthor && sortByDate;
    }

    public void setSortByDate(boolean sortByDate) {
        disableAllSorting();
        this.sortByDate = sortByDate;
    }

    public boolean isSortByAuthor() {
        return !sortByLastCommenter && sortByAuthor && !sortByDate;
    }

    public void setSortByAuthor(boolean sortByAuthor) {
        disableAllSorting();
        this.sortByAuthor = sortByAuthor;
    }

    public boolean isSortByStatus() {
        return sortByStatus;
    }

    public void setSortByStatus(boolean sortByStatus) {
        disableAllSorting();
        this.sortByStatus = sortByStatus;
    }

    public boolean isSortByOffset() {
        return sortByOffset;
    }

    public void setSortByOffset(boolean sortByOffset) {
        //disableAllSorting();
        this.sortByOffset = sortByOffset;
    }

    public boolean isSortByLastCommenter() {
        return sortByLastCommenter && !sortByAuthor && !sortByDate;
    }

    public void setSortByLastCommenter(boolean sortByLastCommenter) {
        //disableAllSorting();
        this.sortByLastCommenter = sortByLastCommenter;
    }

    public void disableAllSorting() {
        this.sortByAuthor = false;
        this.sortByDate = false;
        this.sortByLastCommenter = false;
        this.sortByOffset = false;
    }
}
