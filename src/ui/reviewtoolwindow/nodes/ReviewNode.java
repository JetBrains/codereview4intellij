package ui.reviewtoolwindow.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:05 PM
 */
public class ReviewNode extends SimpleNode implements Navigatable{
    private Review review;

    public ReviewNode(Project project, Review review) {
        super(project);
        this.review = review;
    }

    @Override
    public boolean isAlwaysLeaf() {
        return true;
    }

    @NotNull
    @Override
    public SimpleNode[] getChildren() {
        return new SimpleNode[0];
    }

    @Override
    protected void update(PresentationData presentationData) {
        presentationData.addText(review.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
        int lineNumber = review.getLine();
        presentationData.addText(" (line : " + String.valueOf(lineNumber) + ")", SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    @Override
    public void navigate(boolean requestFocus) {
        Document document = FileDocumentManager.getInstance().getDocument(review.getVirtualFile());
        if(document == null) return;
        PsiFile psiFile = PsiDocumentManager.getInstance(review.getProject()).getPsiFile(document);
        if (psiFile == null) return;
        psiFile.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

}