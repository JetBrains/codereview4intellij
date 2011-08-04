package reviewresult;

import com.intellij.util.messages.Topic;
import reviewresult.Review;

import java.util.EventListener;

/**
 * User: Alisa.Afonina
 * Date: 8/4/11
 * Time: 11:57 AM
 */
public interface ReviewsChangedListener extends EventListener {
    public void reviewAdded(Review review);
    public void reviewDeleted(Review review);
    public void reviewChanged(Review review);
}
