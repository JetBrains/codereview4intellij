package ui.reviewtoolwindow.nodes;

import com.intellij.codeInsight.highlighting.HighlightManager;
import com.intellij.find.findUsages.FindUsagesManager;
import com.intellij.ide.projectView.PresentationData;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.NodeDescriptor;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.apache.xmlbeans.xml.stream.events.ElementTypeNames;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import ui.actions.ReviewActionManager;

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
        if(review.isValid()) {
            int searchStart = review.getSearchStart();
            String name = review.getName();
            if(searchStart >= 0) {
                EditorColorsManager colorManager = EditorColorsManager.getInstance();
                TextAttributes attributes = colorManager.getGlobalScheme().getAttributes(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
                presentationData.addText(name.substring(0, searchStart), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                presentationData.addText(name.substring(searchStart, review.getSearchEnd()), SimpleTextAttributes.fromTextAttributes(attributes));
                presentationData.addText(name.substring(review.getSearchEnd(), name.length()), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            }
            else {
                presentationData.addText(name, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

            }
            int line = review.getLine();
            if(line < 0) {
                presentationData.addText(new ColoredFragment("(INVALID)", SimpleTextAttributes.ERROR_ATTRIBUTES));
                return;
            }
            int lineNumber = line + 1;
            presentationData.addText(" (line : " + String.valueOf(lineNumber) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
        } else {
            presentationData.addText(review.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            presentationData.addText(new ColoredFragment("(INVALID)", SimpleTextAttributes.ERROR_ATTRIBUTES));
        }
    }

    public Review getReview() {
        return review;
    }

    public void setReview(Review review) {
        this.review = review;
    }

    @Override
    public void navigate(boolean requestFocus) {

        OpenFileDescriptor element = review.getElement();
        if(element == null) return;
        element.navigate(true);
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