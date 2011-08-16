package ui.reviewtoolwindow;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.search.ThrowSearchUtil;
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

    public PlainNode removeReview(Review review) {
        PlainNode invalidChild = findInvalidAncestorNode(rootElement, review);
        if(invalidChild != null) {
            PlainNode parent = invalidChild.getPlainParent();
            if((invalidChild instanceof RootNode)) {
                rootElement = new RootNode(project, settings);
            } else {
                invalidChild.removeFromParent();
            }
            return parent;
        }
        return null;
    }

    public PlainNode addReview(Review review) {
        Searcher.getInstance(project).addSearchResult(review);
        PlainNode ancestor = findAncestorNode(rootElement, review.getVirtualFile());
        addChildrenToAncestorNode(ancestor, new ReviewNode(project, review, settings));
        ancestor.update();
        return ancestor;
    }

    private PlainNode findAncestorNode(PlainNode rootElement, VirtualFile file) {
        for(PlainNode node : rootElement.getPlainChildren()) {
            if (node instanceof ModuleNode) {
                Module module = ((ModuleNode) node).getModule();
                if(ModuleRootManager.getInstance(module).getFileIndex().isInContent(file)){
                    return findAncestorNode(node, file);
                }
            }

            if(node instanceof FileNode) {
                if(VfsUtil.isAncestor(((FileNode) node).getFile(),file, false)) {
                    return findAncestorNode(node, file);
                }
            }
        }
        return rootElement;
    }

    //@Nullable
    private PlainNode findInvalidAncestorNode(PlainNode rootElement, Review review) {
        List<PlainNode> rootChildren = rootElement.getPlainChildren();
        PlainNode invalid = null;
        for(PlainNode node : rootChildren) {
            final VirtualFile virtualFile = review.getVirtualFile();
            if (node instanceof ModuleNode) {
                Module module = ((ModuleNode) node).getModule();
                if(ModuleRootManager.getInstance(module).getFileIndex().isInContent(virtualFile)) {
                    invalid = findInvalidAncestorNode(node, review);
                }
            }

            if(node instanceof FileNode) {
                if(VfsUtil.isAncestor(((FileNode) node).getFile(), virtualFile, false)) {
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
        if(parent == null) return;
        if(!file.equals(finalParent)) {
            getFilePath(path, parent, finalParent);
            path.add(new FileNode(project, file, settings));
        }

    }

    private void addChildrenToAncestorNode(PlainNode root, PlainNode finalChild) {
        List<PlainNode> path = new ArrayList<PlainNode>();
        path.add(root);
        VirtualFile parentFile = null;
        VirtualFile childFile = null;
        if(finalChild instanceof ReviewNode) {
            childFile = ((ReviewNode) finalChild).getReview().getVirtualFile();
        }
        if(finalChild instanceof FileNode) {
            childFile = ((FileNode) finalChild).getFile();
        }
        if(childFile == null) return;
        if(root instanceof FileNode) {
            parentFile = ((FileNode) root).getFile();
        }
        if(root instanceof ModuleNode) {
            parentFile = ProjectRootManager.getInstance(project).getFileIndex().getContentRootForFile(childFile);
        }
        if(root instanceof RootNode) {
            Module module = ProjectRootManager.getInstance(project).getFileIndex().getModuleForFile(childFile);
            ModuleNode node = new ModuleNode(project, module, settings);
            addChildrenToAncestorNode(node, finalChild);
            root.addChild(node);
        }
        getFilePath(path, childFile, parentFile);
        path.add(finalChild);
        PlainNode parentNode = path.get(0);
        for (int i = 1; i < path.size(); i++) {
            PlainNode childNode = path.get(i);
            parentNode.addChild(childNode);
            parentNode = childNode;
        }
    }

    public PlainNode getNode(Object o) {
     if(o instanceof Review) {
         PlainNode node = findAncestorNode(rootElement, ((Review) o).getVirtualFile());
         if(node instanceof FileNode) {
             for(PlainNode child : node.getPlainChildren()) {
                if(child instanceof ReviewNode && ((ReviewNode) child).getReview().equals(o)) {return child;}
             }
         }
    }
        return null;
    }

}
