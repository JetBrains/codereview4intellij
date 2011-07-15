package ui.reviewtoolwindow.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import reviewresult.Review;
import reviewresult.ReviewItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:05 PM
 */
public class ReviewNode extends AbstractTreeNode{
    private List<ReviewItemNode> children = new ArrayList<ReviewItemNode>();
    private String text;
    private PsiElement element;
    private Review review;

    public ReviewNode(Project project, Review review) {
        super(project, review.getName());
        for(ReviewItem reviewItem : review.getReviewItems()) {
            children.add(new ReviewItemNode(project, reviewItem));
        }
        this.review = review;
        this.text = review.getName();
    }

    @Override
    public Collection getChildren() {
        return children;
    }

    @Override
    protected void update(PresentationData presentationData) {
        presentationData.setPresentableText(text);
    }

    public void addChild(ReviewItemNode child) {
        children.add(child);
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }
}