package ui.reviewtoolwindow.nodes;


import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:50 PM
 */
public class FileNode extends AbstractTreeNode {
    private Project project;

    public FileNode(Project project, VirtualFile value) {
       super(project, value);
        this.project = project;
    }

    @NotNull
    @Override
    public Collection<AbstractTreeNode> getChildren() {
        VirtualFile file = (VirtualFile) getValue();
        List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
        List<Review> reviews = ReviewManager.getInstance(project).getReviews(file);

        for(Review review : reviews) {
            children.add(new ReviewNode(project, review));
        }

        /*Review review = ReviewManager.getInstance(project).getReviews(file);
        children.add(new ReviewNode(project, review));*/
        return children;
    }

    @Override
    public void update(PresentationData data) {
        VirtualFile file = ((VirtualFile)getValue());
        data.setPresentableText(file.getName());
        Icon icon  = IconUtil.getIcon((VirtualFile) getValue(), Iconable.ICON_FLAG_OPEN, project);
        data.setIcons(icon);
    }

}
