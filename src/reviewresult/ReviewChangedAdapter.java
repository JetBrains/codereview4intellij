package reviewresult;

/**
 * User: Alisa.Afonina
 * Date: 8/19/11
 * Time: 2:20 PM
 */
public abstract class ReviewChangedAdapter implements ReviewsChangedListener{
    @Override
    public void reviewAdded(Review review) {}

    @Override
    public void reviewDeleted(Review review) {}

    @Override
    public void reviewChanged(Review newReview) {}

}
