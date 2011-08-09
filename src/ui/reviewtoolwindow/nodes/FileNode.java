package ui.reviewtoolwindow.nodes;


import com.intellij.ide.projectView.PresentationData;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.util.IconUtil;
import org.jetbrains.annotations.NotNull;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.reviewtoolwindow.ReviewToolWindowSettings;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/14/11
 * Time: 5:50 PM
 */
public class FileNode extends PlainNode implements Navigatable{
    private Project project;
    private VirtualFile file;
    private ReviewToolWindowSettings settings;

    public FileNode(Project project, VirtualFile value, ReviewToolWindowSettings settings) {
        super(project);
        this.project = project;
        this.settings = settings;
        file = value;
       /* if(!file.isDirectory()) {
            List<Review> reviews = ReviewManager.getInstance(project).getValidReviews(file.getUrl());
            if(reviews.isEmpty())return;
            for(Review review : reviews) {
                ReviewNode reviewNode = new ReviewNode(project, review);
                reviewNode.setPlainParent(this);
                children.add(reviewNode);
            }
        }*/
    }

    @Override
    public Object[] getEqualityObjects() {
        Object[] equalityObjects = new Object[1];
        equalityObjects[0] = file;
        return equalityObjects;
    }

    @NotNull
    @Override
    public SimpleNode[] getChildren() {
        if(!settings.isGroupByFile()) {
            List<SimpleNode> newChildren = new ArrayList<SimpleNode>();
            for (PlainNode child : children) {
                if(child instanceof ReviewNode) {
                    for (SimpleNode oldChild : children) {
                        newChildren.add(oldChild);
                    }
                    break;
                } else {
                    newChildren.addAll(Arrays.asList(child.getChildren()));
                }
            }
            return newChildren.toArray(new SimpleNode[newChildren.size()]);
        }
        return children.toArray(new SimpleNode[children.size()]);
    }

    @Override
    public boolean isAutoExpandNode() {
        return true;
    }

    @Override
    public void update(PresentationData data) {
        //if(children.isEmpty()) return;
        data.addText(file.getName(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
        if(!file.isDirectory()) {
            List<Review> reviews = ReviewManager.getInstance(project).getValidReviews(file.getUrl());
            if(reviews == null || reviews.isEmpty()) {
                data.clear();
                return;
            }
            int number =  reviews.size();
            String text = " " + String.valueOf(number) + ((number == 1)?" review":" reviews");
            data.addText( text, SimpleTextAttributes.GRAYED_BOLD_ATTRIBUTES);
        }
        Icon icon  = IconUtil.getIcon(file, Iconable.ICON_FLAG_OPEN, project);
        data.setIcons(icon);

    }


    @Override
    public void navigate(boolean requestFocus) {
        Document document = FileDocumentManager.getInstance().getDocument(file);
        if(document == null) return;
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if (psiFile == null) return;
        psiFile.navigate(requestFocus);
    }

    @Override
    public boolean canNavigate() {
        return true;
    }

    @Override
    public boolean canNavigateToSource() {
        return false;
    }

    public VirtualFile getFile() {
        return file;
    }


    @Override
    public void addChild(PlainNode node) {
        if(!children.contains(node)) {
            node.setPlainParent(this);
            children.add(node);
        }
    }






}
