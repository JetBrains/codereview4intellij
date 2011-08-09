package ui.reviewtoolwindow.nodes;


import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.SimpleNode;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 8/5/11
 * Time: 1:13 PM
 */
public abstract class PlainNode extends SimpleNode {
    protected List<PlainNode> children = new ArrayList<PlainNode>();
    protected PlainNode parent;
    public PlainNode(Project project) {
        super(project);
    }

    public List<PlainNode> getPlainChildren() {
        return children;
    }

    public void removeChild(PlainNode child) {
        if(children.contains(child)) {
            children.remove(child);
        }
    }

    public void removeFromParent() {
            parent.removeChild(this);
    }

    abstract public void addChild(PlainNode node);


    public PlainNode getPlainParent() {
        return parent;
    }

    public void setPlainParent(PlainNode parent) {
        this.parent = parent;
    }


}