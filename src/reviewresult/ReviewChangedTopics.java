package reviewresult;

import com.intellij.util.messages.Topic;

/**
 * User: Alisa.Afonina
 * Date: 8/4/11
 * Time: 12:19 PM
 */
public class ReviewChangedTopics {
    public static final Topic<ReviewsChangedListener> REVIEW_STATUS = new Topic<ReviewsChangedListener>("review", ReviewsChangedListener.class);

    private ReviewChangedTopics() {
    }
}
