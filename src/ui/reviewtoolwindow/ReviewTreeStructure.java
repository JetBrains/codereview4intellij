package ui.reviewtoolwindow;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import reviewresult.Review;
import sun.java2d.pipe.SpanShapeRenderer;
import ui.reviewtoolwindow.nodes.FileNode;
import ui.reviewtoolwindow.nodes.ModuleNode;
import ui.reviewtoolwindow.nodes.ReviewNode;
import ui.reviewtoolwindow.nodes.RootNode;

import javax.xml.bind.NotIdentifiableEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 4:01 PM
 */
public class ReviewTreeStructure extends SimpleTreeStructure {
    private Project project;
    private RootNode rootElement;

    protected ReviewTreeStructure(Project project, RootNode root) {
        super();
        this.project = project;
        rootElement = root;
    }

    @Override
    public Object getRootElement() {
        return rootElement;
    }


    public SimpleNode getNodeToRemove(Review review) {
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        VirtualFile contentRoot = fileIndex.getContentRootForFile(review.getVirtualFile());
        Module module = fileIndex.getModuleForFile(contentRoot);
        //for()
        //SimpleNode node = children.[Arrays.binarySearch(children, module)];
        ArrayList<VirtualFile> path = new ArrayList<VirtualFile>();
        getFilePath(path, review.getVirtualFile());
        SimpleNode nodeToRemove = null;
        for (SimpleNode moduleNode : rootElement.getPlainChildren()) {
            Module nodeModule = ((ModuleNode)moduleNode).getModule();
            if(nodeModule.equals(module)) {
                 nodeToRemove = removeNodes(moduleNode, path, review);
            }
        }
        return nodeToRemove;
    }

    private SimpleNode removeNodes(SimpleNode node, List<VirtualFile> path, Review review) {

        for (SimpleNode child : node.getChildren()) {
            if(child instanceof ReviewNode) {
                if(((ReviewNode) child).getReview().equals(review)) {
                    return child;
                }
                continue;
            }
            if(child.getElement().equals(path.get(path.size() - 1))) {
                if(child.getChildCount() == 1) {
                    return child;
                }
                else {
                    return removeNodes(child, path.subList(0, path.size() - 2), review);
                }
            }
        }
        return null;
    }

    public void addNode(Review review) {
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        VirtualFile contentRoot = fileIndex.getContentRootForFile(review.getVirtualFile());
        Module module = fileIndex.getModuleForFile(contentRoot);
        //SimpleNode node = children[Arrays.binarySearch(children, module)];
    }

    public void getFilePath(List<VirtualFile>path, VirtualFile file) {
        ProjectFileIndex fileIndex = ProjectRootManager.getInstance(project).getFileIndex();
        //List<VirtualFile> path = new ArrayList<VirtualFile>();
        VirtualFile contentRoot = fileIndex.getContentRootForFile(file);
        VirtualFile parent = file.getParent();
        path.add(file);
        if(!file.equals(contentRoot)) {
            getFilePath(path, parent);
        }
    }
}
