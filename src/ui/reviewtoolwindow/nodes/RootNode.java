package ui.reviewtoolwindow.nodes;


import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
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
public class RootNode extends PlainNode {
    private ReviewToolWindowSettings settings;

    public RootNode(Project project, ReviewToolWindowSettings settings) {
        super(project);
        this.settings = settings;
        for(Module module : ModuleManager.getInstance(project).getModules()) {
            ModuleNode moduleNode = new ModuleNode(project, module, settings);
            addChild(moduleNode);
        }
    }

    @Override
    public SimpleNode[] getChildren() {
        List<SimpleNode> newChildren = new ArrayList<SimpleNode>();
        if(!settings.isGroupByModule()) {
            for (SimpleNode child : children) {
                    newChildren.addAll(Arrays.asList(child.getChildren()));
            }
        }
        else {
            for(PlainNode node : children) {
                if(!node.getPlainChildren().isEmpty()) {
                    newChildren.add(node);
                }
            }
        }
        return newChildren.toArray(new SimpleNode[newChildren.size()]);
    }

    @Override
    public void addChild(PlainNode node) {
        if(node instanceof ModuleNode) {
            node.setPlainParent(this);
            if(!children.contains(node)) {
                children.add(node);
            }
        }
    }

}
