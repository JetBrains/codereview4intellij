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

    public void addReview(String name, String text, ReviewStatus status, VirtualFile virtualFile, int start, int end) {
        Document document = FileDocumentManager.getInstance().getDocument(virtualFile);

        Review review = new Review(project, name, text, status, document.createGuardedBlock(start, end), virtualFile);
        ReviewPoint point = new ReviewPoint(project, virtualFile, document.getLineNumber(review.getStart()));
        if(reviews.containsKey(virtualFile)) {
            reviews.get(virtualFile).add(review);
        } else {
            ArrayList<Review> reviewsList = new ArrayList<Review>();
            reviewsList.add(review);
            reviews.put(virtualFile, reviewsList);
        }
        state.reviews.add(review);
    }

    public State getState() {
        return state;
    }

    public void loadState(State state) {
        this.state = state;

        reviews = new HashMap<VirtualFile, List<Review>>();
        for (Review review : state.reviews) {
            VirtualFile virtualFile = VirtualFileManager.getInstance().findFileByUrl(review.getUrl());
            //PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
            if(reviews.containsKey(virtualFile)) {
                reviews.get(virtualFile).add(review);
            } else {
                ArrayList<Review> reviewsList = new ArrayList<Review>();
                reviewsList.add(review);
                reviews.put(virtualFile, reviewsList);
            }
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
