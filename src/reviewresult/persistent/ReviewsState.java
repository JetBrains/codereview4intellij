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
        State state = new State();

        protected ReviewsState(Project project) {
            super(project);
        }

        @Override
        public State getState() {
            state.reviews = ReviewManager.getInstance(myProject).getState();
            return this.state;
        }

        @Override
        public void loadState(State state) {
            this.state = state;
            ReviewManager.getInstance(myProject).loadState(state.reviews);
        }

        public static class FileReviewsList {
            private String filePath;
            private String checksum;
            private List<ReviewBean> reviewBeans = new ArrayList<ReviewBean>(

            );


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

            public void setReviewBeans(List<ReviewBean> reviewBeans) {
                this.reviewBeans = reviewBeans;
            }

            @Tag("file")
            public String getFilePath() {
                return filePath;
            }

            public void setFilePath(String filePath) {
                this.filePath = filePath;
            }

            @Tag("checksum")
            public String getChecksum() {
                return checksum;
            }

            public void setChecksum(String checksum) {
                this.checksum = checksum;
            }
        }
        public static class State {
            @Tag("all_reviews")
            @AbstractCollection(surroundWithTag = false)
            public List<FileReviewsList> reviews = new ArrayList<FileReviewsList>();

        }
}
