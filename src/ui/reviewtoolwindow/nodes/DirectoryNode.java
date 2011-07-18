package ui.reviewtoolwindow.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vcs.update.FileOrDirectoryTreeNode;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import reviewresult.ReviewManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;


/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:51 PM
 */
public class DirectoryNode extends AbstractTreeNode {
    private List<AbstractTreeNode> children  = new ArrayList<AbstractTreeNode>();
    private Project project;

    public DirectoryNode(Project project, VirtualFile file) /*, PsiDirectory directory*/ {
        super(project, file);
        this.project = project;
    }

    public void addChild(FileNode fileNode) {
        children.add(fileNode);
    }

    public Collection<AbstractTreeNode> getChildren() {
        Project project = getProject();
        Set<VirtualFile> filesWithReview = ReviewManager.getInstance(project).getFiles();

        for (VirtualFile virtualFile : filesWithReview) {
            FileNode newNode = new FileNode(project, virtualFile);
            addNode(newNode, this);
        }

        return children;
    }

    private boolean addNode(AbstractTreeNode newNode, DirectoryNode directoryNode) {
        VirtualFile parentDirectory = ((VirtualFile)newNode.getValue()).getParent();
        Collection<AbstractTreeNode> brothersNodes = directoryNode.children;

        VirtualFile currentDirectory = (VirtualFile) directoryNode.getValue();
        VirtualFile[] brothers = currentDirectory.getChildren();
        if(currentDirectory.isDirectory()) {
            if(!brothersNodes.contains(newNode)) {
                if(currentDirectory.equals(parentDirectory)) {
                    brothersNodes.add(newNode);
                    return true;
                }
                else {
                    for (VirtualFile brother : brothers) {
                        DirectoryNode brotherNode = new DirectoryNode(getProject(), brother);
                        if(addNode(newNode, brotherNode)) {
                            brothersNodes.add(brotherNode);
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        return false;
    }



    public void update(PresentationData data) {
        VirtualFile file = ((VirtualFile)getValue());
        data.setPresentableText(file.getName());
        Icon icon  = IconUtil.getIcon((VirtualFile) getValue(), Iconable.ICON_FLAG_OPEN, project);
        data.setIcons(icon);
    }

}
