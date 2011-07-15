package ui.reviewtoolwindow;

import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.ide.util.treeView.AbstractTreeStructure;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultTreeModel;
import java.util.Comparator;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:39 PM
 */
public class ReviewTreeBuilder extends AbstractTreeBuilder {
    public ReviewTreeBuilder(Tree tree, DefaultTreeModel treeModel, AbstractTreeStructure treeStructure, @Nullable Comparator<NodeDescriptor> comparator) {
        super(tree, treeModel, treeStructure, comparator);
        init(tree, treeModel, treeStructure, comparator, false);
    }

}
