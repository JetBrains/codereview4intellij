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
import com.intellij.util.text.DateFormatUtil;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import ui.reviewtoolwindow.ReviewToolWindowSettings;
import ui.reviewtoolwindow.Searcher;
import utils.Util;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 1:05 PM
 */
public class ReviewNode extends PlainNode implements Navigatable{
    private final Review review;
    private final Searcher searcher;

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
            String presentationInfo = review.getPresentationInfo(false);
            presentationData.setTooltip(review.getPresentationInfo(true));
            if(searchStart >= 0) {
                EditorColorsManager colorManager = EditorColorsManager.getInstance();
                TextAttributes attributes = colorManager.getGlobalScheme().getAttributes(EditorColors.TEXT_SEARCH_RESULT_ATTRIBUTES);
                presentationData.addText(presentationInfo.substring(0, searchStart), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
                presentationData.addText(presentationInfo.substring(searchStart, searchEnd), SimpleTextAttributes.fromTextAttributes(attributes));
                presentationData.addText(presentationInfo.substring(searchEnd, presentationInfo.length()), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            }
            else {
                presentationData.addText(presentationInfo, SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);

            }
            int line = review.getLineNumber();
            if(line < 0) {
                presentationData.addText(new ColoredFragment("(INVALID)", SimpleTextAttributes.ERROR_ATTRIBUTES));
                return;
            }
            int lineNumber = line + 1;
            if(!getSettings().isGroupByFile()) {
               /* if(getSettings().isSortByAuthor()) {
                    presentationData.addText(" (" + review.getAuthor() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                } else {
                    if(getSettings().isSortByDate()) {
                        presentationData.addText(" (" + DateFormatUtil.formatPrettyDateTime(review.getFirstDate()) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                    } else {
                        if(getSettings().isSortByLastCommenter()) {
                            presentationData.addText(" (" + review.getLastCommenter() + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                        } else {*/
                            presentationData.addText( " (" + review.getFileName() + " : " + String.valueOf(lineNumber) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
                /*        }
                    }
                }*/
            } else {
                presentationData.addText(" (line : " + String.valueOf(lineNumber) + ")", SimpleTextAttributes.GRAY_ATTRIBUTES);
            }
        } else {
            presentationData.addText(review.getPresentationInfo(false), SimpleTextAttributes.REGULAR_BOLD_ATTRIBUTES);
            presentationData.setTooltip(review.getPresentationInfo(true));
            presentationData.addText(new ColoredFragment("(INVALID)", SimpleTextAttributes.ERROR_ATTRIBUTES));
        }
    }


    @Override
    public Object getObject() {
        return review;
    }

    @Override
    public void navigate(boolean requestFocus) {

        OpenFileDescriptor element = Util.getInstance(review.getProject()).
                                            getOpenFileDescriptor(review.getFilePath(),
                                                    review.getStart()
                                            );
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