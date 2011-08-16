package ui.reviewtoolwindow.nodes;

import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.pom.Navigatable;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import ui.reviewtoolwindow.ReviewToolWindowSettings;
import ui.reviewtoolwindow.Searcher;

import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:05 PM
 */
public class ReviewNode extends PlainNode implements Navigatable{
    private Review review;
    private Searcher searcher;

    public ReviewNode(Project project, Review review, ReviewToolWindowSettings settings) {
        super(project, settings);
        this.review = review;
        this.searcher = Searcher.getInstance(project);
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
            int searchStart = searcher.getReviewSearchResult(review).first;
            int searchEnd = searcher.getReviewSearchResult(review).second;
            String name = review.getName();
            if(searchStart >= 0) {
                EditorColorsManager colorManager = EditorColorsManager.getInstance();
                TextAttributes attributes = colorManager.getGlobalScheme().getAttributes(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
                presentationData.addText(name.substring(0, searchStart), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                presentationData.addText(name.substring(searchStart, searchEnd), SimpleTextAttributes.fromTextAttributes(attributes));
                presentationData.addText(name.substring(searchEnd, name.length()), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
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
            if(!getSettings().isGroupByFile()) {
                presentationData.addText( " (" + review.getVirtualFile().getName() + " : " + String.valueOf(lineNumber) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            } else {
                presentationData.addText(" (line : " + String.valueOf(lineNumber) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        } else {
            presentationData.addText(review.getName(), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            presentationData.addText(new ColoredFragment("(INVALID)", SimpleTextAttributes.ERROR_ATTRIBUTES));
        }
    }


    @Override
    public Object getObject() {
        return review;
    }

    @Override
    public void navigate(boolean requestFocus) {

        OpenFileDescriptor element = review.getElement();
        if(element == null) return;
        element.navigate(false);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    @Override
    public void removeFromParent() {
        getPlainParent().removeChild(this);
    }

    @Override
    public void addChild(PlainNode node) {
        node.setPlainParent(this);
        children.add(node);
    }
}