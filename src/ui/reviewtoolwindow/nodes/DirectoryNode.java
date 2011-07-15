package ui.reviewtoolwindow.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:51 PM
 */
public class DirectoryNode extends AbstractTreeNode {
    private List<AbstractTreeNode> children  = new ArrayList<AbstractTreeNode>();

    public DirectoryNode(Project project) {
        super(project, "ROOOT");
    }

    public void addChild(FileNode fileNode) {
        children.add(fileNode);
    }

    @NotNull
    @Override
    public Collection<? extends AbstractTreeNode> getChildren() {
        return children;
    }

    @Override
    protected void update(PresentationData presentationData) {
        presentationData.setPresentableText("ROOOOOOT");
    }
}
