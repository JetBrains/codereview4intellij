package tests;

import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.IdeaTestCase;
import reviewresult.Review;
import reviewresult.persistent.ReviewBean;
import ui.reviewtoolwindow.ReviewToolWindowSettings;
import ui.reviewtoolwindow.ReviewTreeStructure;
import ui.reviewtoolwindow.nodes.FileNode;
import ui.reviewtoolwindow.nodes.ModuleNode;
import ui.reviewtoolwindow.nodes.ReviewNode;
import ui.reviewtoolwindow.nodes.RootNode;

import java.io.IOException;

/**
 * User: Alisa.Afonina
 * Date: 8/9/11
 * Time: 4:44 PM
 */
public class ReviewTreeStructureTest extends IdeaTestCase {

    private ReviewTreeStructure structure;
    private ReviewToolWindowSettings settings;

    public ReviewTreeStructureTest() {
        super();
        IdeaTestCase.initPlatformPrefix();
    }

    public void setUp() throws Exception {
        super.setUp();
        settings = new ReviewToolWindowSettings(myProject);
        structure = new ReviewTreeStructure(myProject, settings);
    }

    public void initStructure() throws IOException {

        //tree looks like
        //      module -- root
        //    /   \
        //   f    f
        //   |    /|
        //   f-r  r r

        //init review system
        VirtualFile firstFile = myProject.getProjectFile();
        VirtualFile secondFile = this.createMainModule().getModuleFile();
        if(secondFile == null) return;
        VirtualFile secondChildFile = secondFile.findOrCreateChildData(null, "test");
        Review firstLeftReview = new Review(new ReviewBean("11", 1, 1, firstFile.getUrl()), myProject);
        firstLeftReview.setValid(true);
        Review secondLeftReview = new Review(new ReviewBean("11", 1, 1, firstFile.getUrl()), myProject);
        secondLeftReview.setValid(true);
        Review firstRightReview = new Review(new ReviewBean("11", 1, 1, secondChildFile.getUrl()), myProject);
        firstRightReview.setValid(true);


        //init node structure
        RootNode root = (RootNode) structure.getRootElement();
        ModuleNode module = new ModuleNode(myProject, myModule, settings);
        root.addChild(module);
        ReviewNode leftChildReviewNode = new ReviewNode(myProject, firstLeftReview);
        ReviewNode secondLeftChildReviewNode = new ReviewNode(myProject, secondLeftReview);

        ReviewNode rightChildReviewNode = new ReviewNode(myProject, firstRightReview);

        FileNode leftFileNode = new FileNode(myProject, firstFile, settings);
        FileNode rightFileNode = new FileNode(myProject, secondFile, settings);
        FileNode rightChildFileNode = new FileNode(myProject, secondChildFile, settings);
        module.addChild(leftFileNode);
        module.addChild(rightFileNode);

        rightFileNode.addChild(rightChildFileNode);

        leftFileNode.addChild(leftChildReviewNode);
        leftFileNode.addChild(secondLeftChildReviewNode);
        rightChildFileNode.addChild(rightChildReviewNode);
    }

    public void testAddReview() throws Exception {
    }

    public void testRemoveReview() throws Exception {

    }

    public void testGetFilePath() throws Exception {

    }

    public void testGetAncestorNode() throws Exception {
        VirtualFile firstFile = myProject.getProjectFile();
        Review firstLeftReview = new Review(new ReviewBean("11", 1, 1, firstFile.getUrl()), myProject);
        //structure.addChildrenToAncestorNode()
    }

    public void testFindInvalidAncestorNode() throws Exception {

    }
}
