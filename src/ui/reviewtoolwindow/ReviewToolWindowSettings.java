package ui.reviewtoolwindow;

import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.*;
import com.intellij.psi.impl.file.impl.FileManager;
import com.intellij.util.xmlb.XmlSerializer;
import com.sun.imageio.plugins.common.InputStreamAdapter;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewsState;
import utils.Util;

import javax.swing.*;
import java.awt.*;
import java.io.*;

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
