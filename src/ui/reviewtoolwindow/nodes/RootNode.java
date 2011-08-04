package ui.reviewtoolwindow.nodes;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.ui.ListTableModel;
import ui.reviewtoolwindow.ReviewToolWindowSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 8/3/11
 * Time: 5:21 PM
 */
public class RootNode extends SimpleNode {

    private List<SimpleNode> children = new ArrayList<SimpleNode>();
    private Project project;
    private ReviewToolWindowSettings settings;

    public RootNode(Project project, ReviewToolWindowSettings settings) {
        this.project = project;
        this.settings = settings;
        for(Module module : ModuleManager.getInstance(project).getModules()) {
            ModuleNode moduleNode = new ModuleNode(project, module, settings);
            children.add(moduleNode);
        }
    }

    public List<SimpleNode> getPlainChildren() {
        return children;
    }

    @Override
    public SimpleNode[] getChildren() {
        if(!settings.isGroupByModule()) {
            List<SimpleNode> newChildren = new ArrayList<SimpleNode>();
            for (SimpleNode child : children) {
                    newChildren.addAll(Arrays.asList(child.getChildren()));
            }
            return newChildren.toArray(new SimpleNode[newChildren.size()]);
        }
        return children.toArray(new SimpleNode[children.size()]);
    }

}
