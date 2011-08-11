package ui.reviewtoolwindow;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
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

    public ReviewTreeStructure(Project project, ReviewToolWindowSettings settings) {
        super();
        this.project = project;
        rootElement = new RootNode(project, settings);
        this.settings = settings;
        Set<String> filesWithReview = Searcher.getInstance(project).getFilteredFileNames();
        for (String virtualFileName : filesWithReview) {
            List<Review> validReviews = ReviewManager.getInstance(project).getValidReviews(virtualFileName);
            if(validReviews == null) return;
            for (Review review : validReviews)
                addReview(review);
        }
    }

    @Override
    public Object getRootElement() {
        return rootElement;
    }

    public void removeReview(Review review) {
        PlainNode invalidChild = findInvalidAncestorNode(rootElement, review);
        if(invalidChild != null) {
            if((invalidChild instanceof RootNode)) {
                rootElement = new RootNode(project, settings);
            } else {
                invalidChild.removeFromParent();
            }
        }
        rootElement.update();
    }

    public void addReview(Review review) {
        PlainNode ancestor = findAncestorNode(rootElement, review);
        addChildrenToAncestorNode(ancestor, review);
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

    private PlainNode findInvalidAncestorNode(PlainNode rootElement, Review review) {
        List<PlainNode> rootChildren = rootElement.getPlainChildren();
        PlainNode invalid = null;
        for(PlainNode node : rootChildren) {
            if (node instanceof ModuleNode) {
                Module module = ((ModuleNode) node).getModule();
                if(ModuleRootManager.getInstance(module).getFileIndex().isInContent(review.getVirtualFile())) {
                    invalid = findInvalidAncestorNode(node, review);
                }
            }

            if(node instanceof FileNode) {
                if(VfsUtil.isAncestor(((FileNode) node).getFile(), review.getVirtualFile(), false)) {
                    invalid = findInvalidAncestorNode(node, review);
                }
            }

            if(node instanceof ReviewNode) {
                if(((ReviewNode)node).getReview().equals(review)) {
                    return node;
                }
            }

            if(invalid != null) {
                invalid.removeFromParent();
                if(node.getPlainChildren().isEmpty())
                    return node;
            }
        }
        return null;
    }

    private void getFilePath(List<PlainNode> path, VirtualFile file, VirtualFile finalParent) {
        VirtualFile parent = file.getParent();

        if(!file.equals(finalParent)) {
            getFilePath(path, parent, finalParent);
            path.add(new FileNode(project, file, settings));
        }

    }

    private void addChildrenToAncestorNode(PlainNode root, Review review) {
        List<PlainNode> path = new ArrayList<PlainNode>();
        path.add(root);
        VirtualFile file = review.getVirtualFile();
        if(root instanceof FileNode) {
            file = ((FileNode) root).getFile();
        }
        if(root instanceof ModuleNode) {
            file = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(review.getVirtualFile());
        }
        if(root instanceof RootNode) {
            Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(review.getVirtualFile());
            ModuleNode node = new ModuleNode(project, module, settings);
            addChildrenToAncestorNode(node, review);
            root.addChild(node);
        }
        getFilePath(path, review.getVirtualFile(), file);
        path.add(new ReviewNode(project, review));
        PlainNode parentNode = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            PlainNode childNode = path.get(i);
            parentNode.addChild(childNode);
            parentNode = childNode;
        }
    }
}
