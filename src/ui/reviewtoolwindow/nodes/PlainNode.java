package ui.reviewtoolwindow.nodes;


import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import ui.reviewtoolwindow.ReviewToolWindowSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 8/5/11
 * Time: 1:13 PM
 */
public abstract class PlainNode extends SimpleNode {
    protected final List<PlainNode> children = new ArrayList<PlainNode>();
    private PlainNode parent;
    private final ReviewToolWindowSettings settings;

    PlainNode(Project project, ReviewToolWindowSettings settings) {
        super(project);
        this.settings = settings;
    }

    public List<PlainNode> getPlainChildren() {
        return Collections.unmodifiableList(children);
    }

    void removeChild(PlainNode childToRemove) {
        if(children.contains(childToRemove)) {
            children.remove(childToRemove);
        }
    }

    public abstract Object getObject();

    public void removeFromParent() {
            parent.removeChild(this);
    }

    abstract public void addChild(PlainNode node);


    public PlainNode getPlainParent() {
        return parent;
    }

    void setPlainParent(PlainNode parent) {
        this.parent = parent;
    }

    int getChildrenCount() {
        return children.size();
    }

    ReviewToolWindowSettings getSettings() {
        return settings;
    }
}
