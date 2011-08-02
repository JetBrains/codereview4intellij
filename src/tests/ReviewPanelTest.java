/*
package tests;

import com.intellij.ide.util.treeView.AbstractTreeNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.impl.source.tree.TreeUtil;
import com.intellij.testFramework.IdeaTestCase;
import com.intellij.ui.treeStructure.PatchedDefaultMutableTreeNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeBuilder;
import reviewresult.*;
import ui.reviewtoolwindow.ReviewPanel;
import ui.reviewtoolwindow.nodes.FileNode;
import ui.reviewtoolwindow.nodes.ReviewNode;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.stream.events.EndDocument;

*/
/**
 * User: Alisa.Afonina
 * Date: 7/27/11
 * Time: 1:02 PM
 *//*

public class ReviewPanelTest extends IdeaTestCase {
    private ReviewManager reviewManager;

    public ReviewPanelTest() {
        super();
        IdeaTestCase.initPlatformPrefix();
    }

    public void setUp() throws Exception {
        super.setUp();
        reviewManager = ReviewManager.getInstance(getProject());
    }

    public void testCreateOneElementTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = getModule().getModuleFile();//project.getProjectFile();
        addNewReview(projectFile, "test review", 1, 2);

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        int leafCount = root.getLeafCount();
        assertEquals(leafCount, 1);
        SimpleNode leaf = (SimpleNode) root.getFirstLeaf().getUserObject();
        assertTrue(leaf instanceof ReviewNode);
//        SimpleNode parent  = leaf.getParent();
//        assertTrue(parent instanceof FileNode);
 //       assertEquals(((FileNode) parent).getFile().getUrl(), projectFile.getUrl());
    }

    public void testCreateTwoElementInOneFileTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();

        addNewReview(projectFile, "test review 1", 1, 1);
        addNewReview(projectFile, "test review 2", 2, 3);

        assertEquals(reviewManager.getReviews(projectFile).size(), 2);
        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();
        int leafCount = root.getLeafCount();
        assertEquals(leafCount, 2);
        assertEquals(2, root.getFirstLeaf().getParent().getChildCount());
        SimpleNode leaf = (SimpleNode) root.getFirstLeaf().getUserObject();
        assertTrue(leaf instanceof ReviewNode);
        SimpleNode parent  = leaf.getParent();
      //  assertTrue(parent instanceof FileNode);
      //  assertEquals(((FileNode) parent).getFile().getUrl(), projectFile.getUrl());

    }

    public void testCreateTwoElementInDifferentFilesTree() throws Exception {
        Project project = getProject();
        VirtualFile firstFile = project.getProjectFile();
        VirtualFile secondFile = this.createMainModule().getModuleFile();

        addNewReview(firstFile, "test review 1", 1, 1);
        addNewReview(secondFile, "test review 2", 2, 3);
        assertEquals(1, reviewManager.getReviews(firstFile).size());
        assertEquals(1, reviewManager.getReviews(secondFile).size());

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();
        int leafCount = root.getLeafCount();
        assertEquals(leafCount, 2);
       // assertEquals(1, root.getFirstLeaf().getParent().getChildCount());
    }

    public void testEditOneElementTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();
        Review review = addNewReview(projectFile, "test review 1", 1, 1);
        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        review.setName("new name");
        int leafCount = root.getLeafCount();
        assertEquals(leafCount, 1);
        SimpleNode leaf = (SimpleNode) root.getFirstLeaf().getUserObject();
        assertTrue(leaf instanceof ReviewNode);
        assertEquals(((ReviewNode) leaf).getReview().getName(), review.getName());

        ReviewItem item = new ReviewItem("test", ReviewStatus.COMMENT);
        review.addReviewItem(item);

        assertEquals(1, ((ReviewNode) leaf).getReview().getReviewItems().size());
    }

    public void testChangingTreeWhenEditDocumentPath() throws Exception {
        this.simulateProjectOpen();
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();

        String oldUrl = projectFile.getUrl();

        addNewReview(projectFile, "test review 1", 1, 1);

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        VirtualFile baseDir = project.getBaseDir().createChildDirectory(null, "child");
        projectFile.move(null, baseDir);
        panel.updateUI();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();
        int leafCount = root.getLeafCount();
        assertEquals(leafCount, 1);
        SimpleNode leaf = (SimpleNode) root.getFirstLeaf().getUserObject();
        assertTrue(leaf instanceof ReviewNode);
        SimpleNode parent  = leaf.getParent();
       // assertTrue(parent instanceof FileNode);
       // assertEquals(((FileNode) parent).getFile().getUrl(), projectFile.getUrl());
       // assertFalse(oldUrl.equals(projectFile.getUrl()));
    }

    public void testChangingTreeWhenDeleteDocument() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();
        String oldUrl = projectFile.getUrl();

        addNewReview(projectFile, "test review 1", 1, 1);

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();

        projectFile.delete(null);
        panel.updateUI();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();
        int leafCount = root.getLeafCount();
//      assertEquals(0, leafCount);
        assertFalse(((DefaultMutableTreeNode)root.getFirstChild()).getUserObject() instanceof FileNode);
    }

    public void testRemoveReviewFromOneElementTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();
        Review review = addNewReview(projectFile, "test review 1", 1, 1);
        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        reviewManager.removeReview(review);
        panel.updateUI();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();
        int leafCount = root.getLeafCount();
//      assertEquals(0, leafCount);
        assertFalse(((DefaultMutableTreeNode)root.getFirstChild()).getUserObject() instanceof FileNode);
    }

    public void testRemoveReviewFromTwoElementTree() throws Exception {
        Project project = getProject();
        VirtualFile firstFile = project.getProjectFile();
        VirtualFile secondFile = this.createMainModule().getModuleFile();

        Review firstReview = addNewReview(firstFile, "test review 1", 1, 1);
        Review secondReview = addNewReview(secondFile, "test review 2", 2, 3);

        assertEquals(1, reviewManager.getReviews(firstFile).size());
        assertEquals(1, reviewManager.getReviews(secondFile).size());

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();

        reviewManager.removeReview(firstReview);


        panel.updateUI();

        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();


        int leafCount = root.getLeafCount();
        assertEquals(leafCount, 1);
        assertEquals(secondReview, ((ReviewNode)root.getFirstLeaf().getUserObject()).getReview());

        reviewManager.removeReview(reviewManager.findReviewPoint(secondReview));
        panel.updateUI();
        assertFalse(((DefaultMutableTreeNode) root.getFirstChild()).getUserObject() instanceof FileNode);
    }

    public void testGetNextOccurenceInNoElementTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();

        ReviewPanel panel = new ReviewPanel(myProject);

        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getUserObject()), false);
        assertFalse(panel.hasNextOccurence());
    }


    public void testGetNextOccurenceInOneElementTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();
        Review review = addNewReview(projectFile, "test review 1", 1, 1);

        ReviewPanel panel = new ReviewPanel(myProject);

        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getUserObject()), false);
        assertTrue(panel.hasNextOccurence());

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getFirstLeaf().getUserObject()), false);
        assertFalse(panel.hasNextOccurence());
    }

    public void testGetNextOccurenceTwoElementInDifferentFilesTree() throws Exception {
        Project project = getProject();
        VirtualFile firstFile = project.getProjectFile();
        VirtualFile secondFile = this.createMainModule().getModuleFile();

        Review firstReview = addNewReview(firstFile, "test review 1", 1, 1);
        Review secondReview = addNewReview(secondFile, "test review 2", 2, 3);

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getUserObject()), false);
        assertTrue(panel.hasNextOccurence());

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getFirstLeaf().getUserObject()), false);
        assertTrue(panel.hasNextOccurence());

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getLastLeaf().getUserObject()), false);
        assertFalse(panel.hasNextOccurence());
    }

    public void testGetPrevOccurenceInOneElementTree() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getProjectFile();
        Review review = addNewReview(projectFile, "test review 1", 1, 1);

        ReviewPanel panel = new ReviewPanel(myProject);

        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getLastLeaf().getUserObject()), false);
        assertFalse(panel.hasPreviousOccurence());
    }

    public void testGetPrevOccurenceTwoElementInDifferentFilesTree() throws Exception {
        Project project = getProject();
        VirtualFile firstFile = project.getProjectFile();
        VirtualFile secondFile = this.createMainModule().getModuleFile();

        Review firstReview = addNewReview(firstFile, "test review 1", 1, 1);
        Review secondReview = addNewReview(secondFile, "test review 2", 2, 3);

        ReviewPanel panel = new ReviewPanel(myProject);
        SimpleTree reviewTree = panel.getReviewTree();
        PatchedDefaultMutableTreeNode root = (PatchedDefaultMutableTreeNode) reviewTree.getBuilderModel().getRoot();

        //reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), root., false);
        //assertFalse(panel.hasPreviousOccurence());

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getFirstLeaf().getUserObject()), false);
        assertFalse(panel.hasPreviousOccurence());

        reviewTree.setSelectedNode(panel.getReviewTreeBuilder(), ((SimpleNode)root.getLastLeaf().getUserObject()), false);
        assertTrue(panel.hasPreviousOccurence());
    }





    private Review addNewReview(VirtualFile file, String name, int start, int end) {
        assertNotNull(file);
        Document document = FileDocumentManager.getInstance().getDocument(file);
        assertNotNull(document);
        document.insertString(0, "1\n");

        Review review = new Review(new ReviewBean(name, start, end, file.getUrl()), myProject);
        review.setValid(true);
        reviewManager.createReviewPoint(review);

        return review;
    }
}
*/
