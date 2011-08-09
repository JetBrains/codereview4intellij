package ui.reviewtoolwindow;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import org.jetbrains.annotations.Nullable;
import reviewresult.Review;
import reviewresult.ReviewManager;
import ui.reviewtoolwindow.nodes.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 4:01 PM
 */
public class ReviewTreeStructure extends SimpleTreeStructure {
    private Project project;
    private RootNode rootElement;
    private ReviewToolWindowSettings settings;
    @Nullable
    private PlainNode invalidChild;

    protected ReviewTreeStructure(Project project, RootNode root, ReviewToolWindowSettings settings) {
        super();
        this.project = project;
        rootElement = root;
        this.settings = settings;
        Set<String> filesWithReview = Searcher.getInstance(project).getFilteredFileNames();
        for (String virtualFileName : filesWithReview) {
            List<Review> validReviews = ReviewManager.getInstance(project).getValidReviews(virtualFileName);
            for (Review review : validReviews)
                addNode(review, settings);
        }
    }

    @Override
    public Object getRootElement() {
        return rootElement;
    }


    public void removeReview(Review review) {
        findInvalidAncestorNode(rootElement, review);
        if(invalidChild != null) {
            if(!(invalidChild instanceof RootNode)) {
                invalidChild.removeFromParent();
            } else {
                rootElement = new RootNode(project, settings);
            }
            invalidChild = null;
        }
        rootElement.update();

    }

    public void addNode(Review review, ReviewToolWindowSettings settings) {
        PlainNode parent = findAncestorNode(rootElement, review);
        VirtualFile virtualFile = review.getVirtualFile();
        if(parent instanceof FileNode) {
            PlainNode ancestorNode = getAncestorNode(((FileNode) parent).getFile(), review);
            for(PlainNode node : ancestorNode.getPlainChildren()) {
                parent.addChild(node);
            }
        }
        if(parent instanceof ModuleNode) {
            if(ModuleRootManager.getInstance(((ModuleNode) parent).getModule()).getFileIndex().isInContent(virtualFile)){
                VirtualFile root = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(virtualFile);
                PlainNode rootNode = getAncestorNode(root, review);
                parent.addChild(rootNode);
            }
        }

    }

    private PlainNode findAncestorNode(PlainNode rootElement, Review review) {
        for(PlainNode node : rootElement.getPlainChildren()) {
            if (node instanceof ModuleNode) {
                Module module = ((ModuleNode) node).getModule();
                if(ModuleRootManager.getInstance(module).getFileIndex().isInContent(review.getVirtualFile())) {
                    return findAncestorNode(node, review);
                }
            }

            if(node instanceof FileNode) {
                if(VfsUtil.isAncestor(((FileNode) node).getFile(), review.getVirtualFile(), false)) {
                    return findAncestorNode(node, review);
                }
            }
        }
        return rootElement;
    }

    private boolean findInvalidAncestorNode(PlainNode rootElement, Review review) {
        List<PlainNode> rootChildren = rootElement.getPlainChildren();
        for(PlainNode node : rootChildren) {
            if (node instanceof ModuleNode) {
                Module module = ((ModuleNode) node).getModule();
                if(ModuleRootManager.getInstance(module).getFileIndex().isInContent(review.getVirtualFile())) {
                    return findInvalidAncestorNode(node, review);
                }
            }

            if(node instanceof FileNode) {
                int size = node.getPlainChildren().size();
                if(invalidChild == null) {
                    if(size == 1) {
                        invalidChild = node;
                    }
                } else {
                    if(size != 1) {
                        invalidChild = null;
                    }
                }
                if(VfsUtil.isAncestor(((FileNode) node).getFile(), review.getVirtualFile(), false)) {
                    return findInvalidAncestorNode(node, review);
                }
            }

            if(node instanceof ReviewNode) {
                if(((ReviewNode)node).getReview().equals(review)) {
                    if(invalidChild == null) {
                        invalidChild = node;
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public void getFilePath(List<PlainNode> path, VirtualFile file, VirtualFile finalParent) {
        VirtualFile parent = file.getParent();

        if(!file.equals(finalParent)) {
            getFilePath(path, parent, finalParent);
        }
        path.add(new FileNode(project, file, settings));
    }

    public PlainNode getAncestorNode(VirtualFile root, Review review) {
        List<PlainNode> path = new ArrayList<PlainNode>();
        getFilePath(path, review.getVirtualFile(), root);
        path.add(new ReviewNode(project, review));
        if(path.size() == 1) {return path.get(0);}
        PlainNode parentNode = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            PlainNode childNode = path.get(i);
            parentNode.addChild(childNode);
            parentNode = childNode;
        }
        return path.get(0);
    }
}
