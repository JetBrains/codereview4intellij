package ui.reviewtoolwindow;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import ui.reviewtoolwindow.nodes.FileNode;

import javax.xml.bind.NotIdentifiableEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 4:01 PM
 */
public class ReviewTreeStructure extends SimpleTreeStructure {
    private Project project;
    private SimpleNode rootElement;

    protected ReviewTreeStructure(Project project, SimpleNode root) {
        super();
        this.project = project;
        rootElement = root;
    }

    @Override
    public Object getRootElement() {
        return rootElement;
    }

}
