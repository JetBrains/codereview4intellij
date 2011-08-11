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
    private Project project;

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


    public void loadState() {
        groupByFile = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.fileEnabled", false);
        groupByModule = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.moduleEnabled", false);
        showPreviewEnabled = PropertiesComponent.getInstance(project).getBoolean("ReviewToolWindowSettings.previewEnabled", false);
    }
}
