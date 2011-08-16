package ui.reviewtoolwindow.nodes;


import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import ui.reviewtoolwindow.ReviewToolWindowSettings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:50 PM
 */
public class ModuleNode extends PlainNode implements Navigatable{
    private Project project;
    private Module module;

    public ModuleNode(Project project, Module module, ReviewToolWindowSettings settings) {
        super(project, settings);
        this.project = project;
        this.module = module;
    }

    @Override
    public Object[] getEqualityObjects() {
        Object[] equalityObjects = new Object[1];
        equalityObjects[0] = module;
        return equalityObjects;
    }

    @NotNull
    @Override
    public SimpleNode[] getChildren() {
        List<SimpleNode> newChildren = new ArrayList<SimpleNode>();
        if(!getSettings().isGroupByFile()) {
            for (SimpleNode child : children) {
                    newChildren.addAll(Arrays.asList(child.getChildren()));
            }
        } else {
            for(PlainNode node : children) {
                if(node.getChildrenCount() != 0) {
                    newChildren.add(node);
                }
            }
        }
        return newChildren.toArray(new SimpleNode[newChildren.size()]);
    }


    @Override
    public boolean isAutoExpandNode() {
        return true;
    }

    @Override
    public void update(PresentationData data) {
        data.addText(module.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        Icon icon  = module.getModuleType().getNodeIcon(true);
        data.setIcons(icon);

    }


    @Override
    public void navigate(boolean requestFocus) {
        VirtualFile moduleFile = module.getModuleFile();
        if(moduleFile == null) return;
        Document document = FileDocumentManager.getInstance().getDocument(moduleFile);
        if(document == null) return;
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;
        psiFile.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    public Module getModule() {
        return module;
    }

    @Override
    public void addChild(PlainNode node) {
        if(node instanceof FileNode) {
            if(!children.contains(node)) {
                node.setPlainParent(this);
                children.add(node);
            }
        }
    }
}
