package reviewresult;

import com.intellij.ide.projectView.impl.nodes.NamedLibraryElementNode;
import com.intellij.openapi.components.*;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import ui.reviewpoint.ReviewPoint;

import java.util.*;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:51 PM
 */

@State(
    name = "ReviewManager",
    storages = {
       @Storage(id = "default", file = "$PROJECT_FILE$"),
       @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/codeReview.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class ReviewManager implements PersistentStateComponent<ReviewManager.State> {

    State state = new State();
    private Map<VirtualFile, List<Review>> reviews = new HashMap<VirtualFile, List<Review>>();
    private static Project project;



    public static ReviewManager getInstance(Project newProject) {
        project = newProject;
        return ServiceManager.getService(project, ReviewManager.class);
    }

    public Project getProject() {
        return project;
    }

    public void addReview(String reviewName, String text, ReviewStatus status, VirtualFile virtualFile, int start, int end) {
        if(reviewName == null) {
            int NAME_LENGTH = 4;
            reviewName = (text.length() > NAME_LENGTH) ? text.trim().substring(0, NAME_LENGTH - 1) + "..." : text;
        }
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);
        ReviewItem item = new ReviewItem(project, text, status);
        Review newReview = new Review(project, reviewName, document.createRangeMarker(start, end), virtualFile);
        newReview.addReviewItem(item);
        addReview(newReview, virtualFile);

    }

    private void addReview(Review newReview, VirtualFile virtualFile) {

        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);

        if(reviews.containsKey(virtualFile)) {
            List<Review> reviewList = reviews.get(virtualFile);
            boolean reviewExists = false;
            for (Review review : reviewList) {
                if(newReview.equals(review)) {
                    review.addReviewItems(newReview.getReviewItems());
                    reviewExists = true;
                    break;
                }
            }
            if(!reviewExists) {
                ReviewPoint point = new ReviewPoint(project, virtualFile, document.getLineNumber(newReview.getStart()));
                reviewList.add(newReview);
                if(!state.reviews.contains(newReview)) {
                    state.reviews.add(newReview);
                }
            }
        } else {
            ArrayList<Review> reviewsList = new ArrayList<Review>();
            ReviewPoint point = new ReviewPoint(project, virtualFile, document.getLineNumber(newReview.getStart()));
            reviewsList.add(newReview);
            reviews.put(virtualFile, reviewsList);
            if(!state.reviews.contains(newReview)) {
                state.reviews.add(newReview);
            }
        }
    }

    public State getState() {
        return state;
    }

    public void loadState(State state) {
        this.state = state;

        reviews = new HashMap<VirtualFile, List<Review>>();
        for (Review review : state.reviews) {
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(review.getUrl());
            review.setProject(project);
            addReview(review, virtualFile);
        }

    }

    public List<Review> getReviews() {
        return state.reviews;
    }

    public List<Review> getReviews(VirtualFile virtualFile) {
        return reviews.get(virtualFile);
    }

    public Set<VirtualFile> getFiles() {
        return reviews.keySet();
    }

    public static class State {
        @Tag("reviews")
        @AbstractCollection(surroundWithTag = false)
        public List<Review> reviews;
        public State() {
            reviews = new ArrayList<Review>();
        }
    }

}
