package ui.reviewtoolwindow.nodes;


import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.projectView.ViewSettings;
import com.intellij.ide.projectView.impl.nodes.PsiFileNode;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import reviewresult.Review;
import reviewresult.ReviewManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:50 PM
 */
public class FileNode extends PsiFileNode {
    private Project project;

    public FileNode(Project project, PsiFile value) {
        super(project, value,ViewSettings.DEFAULT);
        this.project = project;
    }

    @Override
    public Collection<AbstractTreeNode> getChildrenImpl() {
        PsiFile psiFile = getValue();
        /*List<Review> reviews = ReviewManager.getInstance(project).getReviews(psiFile);

        for(Review review : reviews) {
            children.add(new ReviewNode(project, review));
        }*/
        List<AbstractTreeNode> children = new ArrayList<AbstractTreeNode>();
        Review review = ReviewManager.getInstance(project).getReviews(psiFile);
        children.add(new ReviewNode(project, review));
        return children;
    }

    @Override
    public void update(PresentationData data) {
        super.update(data);
        data.setPresentableText(getValue().getName());
    }
}
