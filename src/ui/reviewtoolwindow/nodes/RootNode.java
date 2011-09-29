package ui.reviewtoolwindow.nodes;


import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import ui.reviewtoolwindow.ReviewToolWindowSettings;
import ui.reviewtoolwindow.filter.Searcher;
import utils.ReviewsBundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 8/3/11
 * Time: 5:21 PM
 */
public class RootNode extends PlainNode {

    public RootNode(Project project, ReviewToolWindowSettings settings) {
        super(project,settings);
        for(Module module : ModuleManager.getInstance(project).getModules()) {
            ModuleNode moduleNode = new ModuleNode(project, module, settings);
            addChild(moduleNode);
        }
    }

    @Override
    public SimpleNode[] getChildren() {
        List<SimpleNode> newChildren = new ArrayList<SimpleNode>();
        final Searcher searcher = Searcher.getInstance(myProject);
        if(searcher.isEmpty()) return new SimpleNode[]{new SimpleNode() {
            @Override
            protected void update(PresentationData presentation) {
                if(!searcher.additionalFilterIsSet()) {
                    presentation.addText(ReviewsBundle.message("reviews.noReviewsWithParameters", searcher.getFilter()),
                                         SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                } else {
                    presentation.addText(ReviewsBundle.message("reviews.noReviewsWithParameters"),
                                         SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                }

            }

            @Override
            public SimpleNode[] getChildren() {
                return new SimpleNode[0];
            }
        }};
        if(!getSettings().isGroupByModule()) {
            for (SimpleNode child : children) {
                    newChildren.addAll(Arrays.asList(child.getChildren()));
            }
        }
        else {
            for(PlainNode node : children) {
                if(node.getChildrenCount() != 0) {
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

    @Override
    public Object getObject() {
        return null;
    }
}
