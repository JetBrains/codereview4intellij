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

        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.sortByAuthor", String.valueOf(sortByAuthor));
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.sortByDate", String.valueOf(sortByDate));
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.sortByOffset", String.valueOf(sortByOffset));
        PropertiesComponent.getInstance(project).setValue("ReviewToolWindowSettings.sortByLastCommenter", String.valueOf(sortByLastCommenter));
    }


    private void loadState() {
        groupByFile = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.fileEnabled", false);
        groupByModule = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.moduleEnabled", false);
        showPreviewEnabled = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.previewEnabled", false);

        sortByAuthor = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.sortByAuthor", false);
        sortByDate = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.sortByDate", false);
        sortByOffset = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.sortByOffset", false);
        sortByLastCommenter = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.sortByLastCommenter", false);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isSortByDate() {
        return sortByDate;
    }

    public void setSortByDate(boolean sortByDate) {
        disableAllSorting();
        this.sortByDate = sortByDate;
    }

    public boolean isSortByAuthor() {
        return sortByAuthor;
    }

    public void setSortByAuthor(boolean sortByAuthor) {
        disableAllSorting();
        this.sortByAuthor = sortByAuthor;
    }

    public boolean isSortByOffset() {
        return sortByOffset;
    }

    public void setSortByOffset(boolean sortByOffset) {
        disableAllSorting();
        this.sortByOffset = sortByOffset;
    }

    public boolean isSortByLastCommenter() {
        return sortByLastCommenter;
    }

    public void setSortByLastCommenter(boolean sortByLastCommenter) {
        disableAllSorting();
        this.sortByLastCommenter = sortByLastCommenter;
    }

    public void disableAllSorting() {
        this.sortByAuthor = false;
        this.sortByDate = false;
        this.sortByLastCommenter = false;
        this.sortByOffset = false;
    }
}
