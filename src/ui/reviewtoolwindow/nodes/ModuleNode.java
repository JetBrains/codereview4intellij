package ui.reviewtoolwindow.nodes;


import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.roots.impl.ProjectFileIndexImpl;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:50 PM
 */
public class ModuleNode extends SimpleNode implements Navigatable{
    private Project project;
    private Module module;
    private List<SimpleNode> children = new ArrayList<SimpleNode>();

    public ModuleNode(Project project, Module module) {
        super(project/*, parentDescriptor*/);
        this.project = project;
        this.module = module;
        List<FileNode> roots = new ArrayList<FileNode>();
        for(VirtualFile root : ModuleRootManager.getInstance(module).getContentRoots()) {
            children.add(new FileNode(project, root));
        }
    }

    public ModuleNode(Project project, Module module, SimpleNode node) {
        this.project = project;
        this.module = module;
        children.add(node);
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
        return children.toArray(new SimpleNode[children.size()]);
    }


    @Override
    public boolean isAutoExpandNode() {
        return true;
    }

    @Override
    public void update(PresentationData data) {
        //if(children.isEmpty()) return;
        data.addText(module.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        Icon icon  = module.getModuleType().getNodeIcon(true);
        data.setIcons(icon);

    }


    @Override
    public void navigate(boolean requestFocus) {
        Document document = FileDocumentManager.getInstance().getDocument(module.getModuleFile());
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

    //todo : do real update, not rebuilding all children
    public void updateContent() {
    }

    public void addChild(SimpleNode node) {
        children.add(node);
    }
}
