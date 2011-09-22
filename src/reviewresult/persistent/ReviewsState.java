package reviewresult.persistent;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.util.xmlb.annotations.AbstractCollection;
import com.intellij.util.xmlb.annotations.Tag;
import reviewresult.ReviewManager;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 8/3/11
 * Time: 1:24 PM
 */

@State(
    name = "ReviewManager",
    storages = {
       @Storage(id = "default", file = "$PROJECT_FILE$"),
       @Storage(id = "dir", file = "$PROJECT_CONFIG_DIR$/codeReview.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class ReviewsState extends AbstractProjectComponent implements PersistentStateComponent<ReviewsState.State> {
        private State state = new State();

        protected ReviewsState(Project project) {
            super(project);
        }

        @Override
        public State getState() {
            state.setReviews(ReviewManager.getInstance(myProject).getState());
            return this.state;
        }

        @Override
        public void loadState(State state) {
            this.state = state;
            ReviewManager.getInstance(myProject).loadState(state.getReviews());
        }

        @SuppressWarnings({"AssignmentToCollectionOrArrayFieldFromParameter", "ReturnOfCollectionOrArrayField"})
        public static class FileReviewsList {
            private String filePath;
            private String checksum;
            private List<ReviewBean> reviewBeans = new ArrayList<ReviewBean>();

            @SuppressWarnings({"UnusedDeclaration"})
            public FileReviewsList() {
            }

            public FileReviewsList(String filePath, String checksum, List<ReviewBean> resultBeans) {
                this.filePath = filePath;
                this.checksum = checksum;
                reviewBeans = resultBeans;
            }

            @Tag("reviews")
            @AbstractCollection(surroundWithTag = false)
            public List<ReviewBean> getReviewBeans() {
                return reviewBeans;
            }

            @SuppressWarnings({"UnusedDeclaration"})
            public void setReviewBeans(List<ReviewBean> reviewBeans) {
                this.reviewBeans = reviewBeans;
            }

            @Tag("file")
            public String getFilePath() {
                return filePath;
            }

            @SuppressWarnings({"UnusedDeclaration"})
            public void setFilePath(String filePath) {
                this.filePath = filePath;
            }

            @Tag("checksum")
            public String getChecksum() {
                return checksum;
            }

            @SuppressWarnings({"UnusedDeclaration"})
            public void setChecksum(String checksum) {
                this.checksum = checksum;
            }
        }

        @SuppressWarnings({"ReturnOfCollectionOrArrayField", "AssignmentToCollectionOrArrayFieldFromParameter", "UnusedDeclaration"})
        public static class State {

            private List<FileReviewsList> reviews = new ArrayList<FileReviewsList>();

             @Tag("all_reviews")
            @AbstractCollection(surroundWithTag = false)
            public List<FileReviewsList> getReviews() {
                return reviews;
            }

            public void setReviews(List<FileReviewsList> reviews) {
                this.reviews = reviews;
            }
        }
}
