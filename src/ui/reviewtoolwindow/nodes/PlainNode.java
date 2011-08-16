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
    protected List<PlainNode> children = new ArrayList<PlainNode>();
    private PlainNode parent;
    private ReviewToolWindowSettings settings;

    public PlainNode(Project project, ReviewToolWindowSettings settings) {
        super(project);
        this.settings = settings;
    }

    public List<PlainNode> getPlainChildren() {
        return Collections.unmodifiableList(children);
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

    public int getChildrenCount() {
        if(/*getChildren().length == 0 ||*/ children.isEmpty()) {
            return 0;
        }
        else return children.size();
    }

    public ReviewToolWindowSettings getSettings() {
        return settings;
    }

   // public abstract Object getObject();
}
