/*

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.IdeaTestCase;
import reviewresult.Review;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewItem;
import ui.gutterpoint.ReviewPoint;
import ui.gutterpoint.ReviewPointManager;

import java.util.List;
import java.util.Map;
import java.util.Set;



*/
/**
 * User: Alisa.Afonina
 * Date: 7/26/11
 * Time: 12:22 PM
 *//*



public class ReviewManagerTest extends IdeaTestCase {

    private ReviewManager reviewManager;

    public ReviewManagerTest() {
        super();
        IdeaTestCase.initPlatformPrefix();
    }

    public void setUp() throws Exception {
        super.setUp();
        reviewManager = ReviewManager.getInstance(getProject());
    }


    public void testAddOneReview() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getBaseDir();
        Review review = addNewReview(projectFile, "test Review", 1, 2);
        List<Review> reviews = reviewManager.getValidReviews(projectFile.getUrl());
        assertEquals(1, reviews.size());
        Review addedReview = reviews.get(0);
        assertReviewsEquals(review, addedReview);

    }
   */
/*
    public void testAddReviewAfterAnotherSameFile() throws Exception {
        Project project = getProject();
        VirtualFile firstFile = project.getBaseDir();
        Review firstReview = addNewReview(firstFile, "test Review", 1, 2);
        Review secondReview = addNewReview(firstFile, "test Review", 2, 4);
        List<Review> reviews = reviewManager.getValidReviews(firstFile.getUrl());
        assertEquals(2, reviews.size());
        Review addedReview = reviews.get(1);
        assertReviewsEquals(secondReview, addedReview);
    }

    public void testAddReviewAfterAnotherDifferentFilesFile() throws Exception {
        Project project = getProject();
        VirtualFile firstFile = project.getBaseDir();
        VirtualFile secondFile = this.createMainModule().getModuleFile();

        Review firstReview = addNewReview(firstFile, "test review 1", 1, 3);
        Review secondReview = addNewReview(secondFile, "test review 2", 2, 3);

        Set<String> reviewFileNames = reviewManager.getFileNames();
        assertEquals(2, reviewFileNames.size());
        String firstFileUrl = firstFile.getUrl();
        assertEquals(true, reviewFileNames.contains(firstFileUrl));
        String secondFileUrl = secondFile.getUrl();
        assertEquals(true, reviewFileNames.contains(secondFileUrl));

        List<Review> reviews = reviewManager.getValidReviews(firstFileUrl);
        assertEquals(1, reviews.size());
        Review addedReview = reviews.get(0);

        assertReviewsEquals(firstReview, addedReview);

        reviews = reviewManager.getValidReviews(secondFileUrl);
        assertEquals(1, reviews.size());
        addedReview = reviews.get(0);

        assertReviewsEquals(secondReview, addedReview);
    }

    public void testEditReview() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getBaseDir();
        Review firstReview = addNewReview(projectFile, "test Review", 1, 2);
        List<Review> reviews = reviewManager.getValidReviews(projectFile.getUrl());
        assertEquals(1, reviews.size());
        Review addedReview = reviews.get(0);
        firstReview.setName("new Test Name");

        assertReviewsEquals(firstReview, addedReview);
    }

    public void testAddReviewItem() throws Exception {
         Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        Review firstReview = addNewReview(firstFile, "test Review", 1, 2);
        List<Review> reviews = reviewManager.getValidReviews(firstFile.getUrl());
        assertEquals(1, reviews.size());
        Review addedReview = reviews.get(0);
        firstReview.addReviewItem(new ReviewItem("review Text", ReviewStatus.COMMENT));
        assertEquals(1, addedReview.getReviewItems().size());
        assertReviewsEquals(firstReview, addedReview);
    }

    public void testRemoveReviewItem() throws Exception {
        Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        Review firstReview = addNewReview(firstFile, "test Review", 1, 2);
        ReviewItem reviewItem = new ReviewItem("review Text", ReviewStatus.COMMENT);
        firstReview.addReviewItem(reviewItem);
        List<Review> reviews = reviewManager.getValidReviews(firstFile.getUrl());
        assertEquals(1, reviews.size());
        Review addedReview = reviews.get(0);
        firstReview.getReviewBean().getReviewItems().remove(reviewItem);
        assertReviewsEquals(firstReview, addedReview);
    }

    public void testAddOneReviewPoint() throws Exception {
        Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        Review review = addNewReviewPoint(firstFile, "test Review", 1, 2);
        List<Review> reviews = reviewManager.getValidReviews(firstFile.getUrl());
        assertEquals(1, reviews.size());
        Review addedReview = reviews.get(0);
        assertReviewsEquals(review, addedReview);
        Map<Review, ReviewPoint> reviewPoints = ReviewPointManager.getInstance(project).getReviewPoints();
        assertEquals(1, reviewPoints.size());
        assertTrue(reviewPoints.containsKey(review));
        assertReviewsEquals(review, reviewPoints.get(review).getReview());
    }

    public void testAddReviewPointAfterAnother() throws Exception {
        Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        Review firstReview = addNewReviewPoint(firstFile, "test Review", 1, 2);
        Review secondReview = addNewReviewPoint(firstFile, "test Review", 1, 2);

        List<Review> reviews = reviewManager.getValidReviews(firstFile.getUrl());
        assertEquals(2, reviews.size());
        Map<Review, ReviewPoint> reviewPoints = ReviewPointManager.getInstance(project).getReviewPoints();
        assertEquals(2, reviewPoints.size());

        assertTrue(reviewPoints.containsKey(firstReview));
        assertTrue(reviewPoints.containsKey(secondReview));

    }

    public void testRemoveOneOfOneReviewPoint() throws Exception {
        Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        Review firstReview = addNewReviewPoint(firstFile, "test Review", 1, 2);

        ReviewPoint pointToRemove = ReviewPointManager.getInstance(project).findReviewPoint(firstReview);
        assertNotNull(pointToRemove);
        reviewManager.removeReview(firstReview);
        Map<Review, ReviewPoint> reviewPoints = ReviewPointManager.getInstance(project).getReviewPoints();
        assertFalse(reviewPoints.containsKey(firstReview));
        List<Review> reviews = reviewManager.getValidReviews(firstFile.getUrl());
        if(reviews != null) {
            assertFalse(reviews.contains(firstReview));
        }
    }


    public void testFindReviewPoint() throws Exception {
        Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        assertNotNull(firstFile);
        Document document = FileDocumentManager.getInstance().getDocument(firstFile);
        assertNotNull(document);
        document.insertString(0, "1\n");

        VirtualFile secondFile = this.createMainModule().getModuleFile();
        Document secondDocument = FileDocumentManager.getInstance().getDocument(secondFile);
        assertNotNull(secondDocument);
        secondDocument.insertString(0, "1\n");

        //find existing
        //Review firstReview = new Review(new ReviewBean("test review", 1, 1, secondFile.getUrl()), project);
        //firstReview.setValid(true);
        Review firstReview = addNewReview(firstFile, "test review", 1,1);
        assertNotNull(ReviewPointManager.getInstance(project).findReviewPoint(firstReview));

        //no result for nonexistent
        Review secondReview = new Review(new ReviewBean("test review review", 11, 1, secondFile.getUrl()), project);
        assertNull(ReviewPointManager.getInstance(project).findReviewPoint(secondReview));
    }

    public void testRemoveOneOfManyReviewPoints() throws Exception {
        Project project = getProject();

        VirtualFile firstFile = project.getBaseDir();
        VirtualFile secondFile = this.createMainModule().getModuleFile();
        Review firstReview = addNewReviewPoint(firstFile, "test Review", 1, 2);
        Review secondReview = addNewReviewPoint(secondFile, "test Review", 1, 2);

        ReviewPoint point = ReviewPointManager.getInstance(project).findReviewPoint(firstReview);
        reviewManager.removeReview(firstReview);

        Map<Review, ReviewPoint> reviewPoints = ReviewPointManager.getInstance(project).getReviewPoints();
        assertFalse(reviewPoints.containsKey(firstReview));
        assertTrue(reviewPoints.containsKey(secondReview));
        List<Review> reviews = reviewManager.getValidReviews(secondFile.getUrl());
        if(reviews != null) {
            assertFalse(reviews.contains(firstReview));
            assertTrue(reviews.contains(secondReview));
        }
    }

    public void testAddIncorrectReview() throws Exception {
        Project project = getProject();
        VirtualFile projectFile = project.getBaseDir();
        assertNotNull(projectFile);
        Review review = new Review(project, "test review", -1, 1, projectFile);
        assertIncorrectReview(projectFile, review);

        review = new Review(project, "test review", 1, -1, projectFile);
        assertIncorrectReview(projectFile, review);

        review = new Review(project, "test review", 10, 9, projectFile);
        assertIncorrectReview(projectFile, review);

        review = new Review(new ReviewBean("test review", 9, 10, "fake url"), project);
        assertIncorrectReview(projectFile, review);
    }

    public void testReviewIfDocumentChanging() throws Exception {
        VirtualFile firstFile = myProject.getBaseDir();

        assertNotNull(firstFile);
        Document document = FileDocumentManager.getInstance().getDocument(firstFile);
        Review review = addNewReviewPoint(firstFile, "test review",
                                                    document.getLineStartOffset(0),
                                                    document.getLineEndOffset(0));

        //insert line before review into document
        int start = review.getReviewBean().getStart();
        document.insertString(0, "1\n");
        assertFalse(start == review.getReviewBean().getStart());

        //insert line in the middle of review into document
        start = review.getReviewBean().getStart();
        document.insertString(start + 1, "1\n");
        assertEquals(start, review.getReviewBean().getStart());

        //insert line at the end of review into document
        start = review.getReviewBean().getStart();
        document.insertString(document.getText().length(), "1");
        assertTrue(start == review.getReviewBean().getStart());
    }

    private void assertIncorrectReview(VirtualFile projectFile, Review review) {
*//*

*/
/*
        assertFalse(review.isValid());
        reviewManager.createReviewPoint(review);
        assertFalse(reviewManager.getReviewPoints().containsKey(review));
        assertNullOrEmpty(reviewManager.getValidReviews(projectFile));
        assertFalse(reviewManager.getState().reviews.contains(review.getReviewBean()));
*//*




    private static void assertReviewBeansEquals(ReviewBean reviewBean, String name, int start, int end, List<ReviewItem> reviewItems) {
        assertEquals(reviewBean.getName(), name);
        assertEquals(reviewBean.getContext().getStart(), start);
        assertEquals(reviewBean.getContext().getEnd(), end);
        for(ReviewItem item : reviewBean.getReviewItems()) {
            assertEquals(true, reviewItems.contains(item));
        }
    }

    private static void assertReviewsEquals(Review review, Review addedReview) {
        ReviewBean reviewBean = addedReview.getReviewBean();
        assertEquals(review.getProject(), addedReview.getProject());
        assertEquals(review.getFilePath(), addedReview.getFilePath());
        assertReviewBeansEquals(review.getReviewBean(),
                reviewBean.getName(), reviewBean.getContext().getStart(), reviewBean.getContext().getEnd(), reviewBean.getReviewItems());
    }

    private Review addNewReview(VirtualFile file, String name, int start, int end) {
        assertNotNull(file);
        Document document = FileDocumentManager.getInstance().getDocument(file);
        assertNotNull(document);
        document.insertString(0, "1\n");

        Review review = new Review(new ReviewBean(name, start, end), myProject, file.getPath());
        review.setValid(true);
        //reviewManager.createReviewPoint(review);
        reviewManager.placeReview(review);

        return review;
    }
}
     */
/*
     private Review addNewReviewPoint(VirtualFile file, String name, int start, int end) {
        assertNotNull(file);
        Document document = FileDocumentManager.getInstance().getDocument(file);
        assertNotNull(document);
        document.insertString(0, "111\n");

        Review review = new Review(new ReviewBean(name, start, end, file.getUrl()), myProject);
        review.setValid(true);
        //reviewManager.createReviewPoint(review);
        //reviewManager.placeReview(review)
        return review;
    }
}

*/

