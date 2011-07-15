package ui.reviewtoolwindow;

import com.intellij.ide.projectView.TreeStructureProvider;
import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.ide.util.treeView.AbstractTreeStructureBase;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;

import java.util.Collections;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/13/11
 * Time: 4:01 PM
 */
public class ReviewTreeStructure extends AbstractTreeStructureBase {
    protected AbstractTreeNode rootElement;

    protected ReviewTreeStructure(Project project, AbstractTreeNode root) {
        super(project);
        rootElement = root;
    }

    @Override
    public List<TreeStructureProvider> getProviders() {
        return Collections.emptyList();
    }

    public AbstractTreeNode getRootElement() {
        return rootElement;
    }

    @Override
    public void commit() {
        PsiDocumentManager.getInstance(myProject).commitAllDocuments();
    }

    @Override
    public boolean hasSomethingToCommit() {
        return PsiDocumentManager.getInstance(myProject).hasUncommitedDocuments();
    }

}
