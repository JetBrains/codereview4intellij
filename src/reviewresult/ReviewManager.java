package reviewresult;

import com.intellij.ide.startup.StartupManagerEx;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.DumbAwareRunnable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.*;
import com.intellij.util.io.URLUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.persistent.ReviewBean;
import reviewresult.persistent.ReviewsState;
import ui.gutterpoint.ReviewPointManager;
import utils.Util;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.util.*;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:51 PM
 */

public class ReviewManager extends AbstractProjectComponent implements DumbAware {
    private static final Logger LOG = Logger.getInstance(ReviewManager.class.getName());
    private final StartupManagerEx startupManager;

    private Map<String, List<Review>> filePath2reviews = new HashMap<String, List<Review>>();
    private List<Review> removed = new ArrayList<Review>();
    private List<String> availableTags = new ArrayList<String>();
    private final ReviewsChangedListener eventPublisher;

    private boolean saveReviewsToPatch = true;
    private List<String> authors = new ArrayList<String>();

    public ReviewManager(@NotNull final Project project, final StartupManager startupManager) {
        super(project);
        this.startupManager = (StartupManagerEx)startupManager;

        VirtualFileManager.getInstance().addVirtualFileListener(new ReviewVirtualFileListener(), project);
        eventPublisher = project.getMessageBus().syncPublisher(ReviewChangedTopics.REVIEW_STATUS);

    }

    public static ReviewManager getInstance(@NotNull Project project) {
        return project.getComponent(ReviewManager.class);
    }


    public List<ReviewsState.FileReviewsList> getState() {
        List<ReviewsState.FileReviewsList> result = new ArrayList<ReviewsState.FileReviewsList>();

        for (Map.Entry<String, List<Review>> entry : filePath2reviews.entrySet()) {
            List<ReviewBean> resultBeans = new ArrayList<ReviewBean>();
            for (Review review : entry.getValue()) {
                resultBeans.add(review.getReviewBean());
            }
            for(Review removedReview : removed) {
                if(removedReview.getFilePath().equals(entry.getKey())) {
                    resultBeans.add(removedReview.getReviewBean());
                }
            }
            final String filePath = entry.getKey();
            result.add(new ReviewsState.FileReviewsList(filePath,
                    Util.getInstance(myProject).getCheckSum(filePath),
                    resultBeans));
        }
        return result;
    }

    public void loadState(List<ReviewsState.FileReviewsList> reviewBeans) {
        loadReviews(reviewBeans, false);
    }

    private void loadReviewsForFile(List<ReviewsState.FileReviewsList> reviewBeans) {
        loadReviews(reviewBeans, true);
    }

    public void loadReviews(final List<ReviewsState.FileReviewsList> lists, boolean isPartOfState) {
        if(!isPartOfState) {
            filePath2reviews = new HashMap<String, List<Review>>();
        }
        final Runnable runnable = new DumbAwareRunnable() {
            public void run() {
                for(ReviewsState.FileReviewsList list : lists) {
                    String filePath = list.getFilePath();

                    boolean checkSumIsCorrect = list.getChecksum().equals(Util.getInstance(myProject).getCheckSum(filePath));
                    for (ReviewBean reviewBean : list.getReviewBeans()) {
                        final Review review = new Review(reviewBean, myProject, filePath);
                        if(!checkSumIsCorrect)
                            review.checkContext();
                        placeReview(review);
                    }
                    }
                }
        };
        if (startupManager.startupActivityPassed()) {
            runnable.run();
        }
        else {
          startupManager.registerPostStartupActivity(runnable);
        }
    }


    public void placeReview(Review newReview) {
            List<Review> reviewList = filePath2reviews.get(newReview.getFilePath());
            if(reviewList == null || reviewList.isEmpty()) {
                    reviewList = new ArrayList<Review>();
            }
            int existingReviewIndex = reviewList.indexOf(newReview);
            if(existingReviewIndex >= 0) {
                Review review = reviewList.get(existingReviewIndex);
                //state of review changed from invalid to valid or vice versa
                if(!newReview.isValid() || !review.isValid()) {
                    selectReviewState(review/*, newReview*/);
                    return;
                } else {
                    //review exists and is valid, but something changed
                    mergeReviews(review, newReview);
                }
            }
             else {
                if(newReview.isValid() && !newReview.isDeleted()) {
                    if(reviewList.isEmpty()) {
                        filePath2reviews.put(newReview.getFilePath(), reviewList);
                    }
                    reviewList.add(newReview);
                    addTags(newReview.getTags());
                    for(String author : newReview.getAuthors()) {
                        if(!authors.contains(author))
                            authors.add(author);
                    }
                    eventPublisher.reviewAdded(newReview);
                }
                else {
                    if(!removed.contains(newReview)) {
                        addTags(newReview.getTags());
                        for(String author : newReview.getAuthors()) {
                        if(!authors.contains(author))
                            authors.add(author);
                        }
                        removed.add(newReview);
                    }
                    return;
                }
            }

        ReviewPointManager.getInstance(myProject).reloadReviewPoint(newReview);
    }

    public void changeReview(Review review) {
        eventPublisher.reviewChanged(review);
    }

    private void mergeReviews(Review oldReview, Review newReview) {
        if(Messages.showYesNoDialog("This review already exists. Would you like to overwrite it?",
                "Review Exists",
                Messages.getInformationIcon()) == Messages.OK) {
            oldReview.setReviewBean(newReview.getReviewBean());
            updateReview(oldReview);
        }
    }

    public void updateReview(Review review) {
        eventPublisher.reviewChanged(review);
    }

    private void selectReviewState(Review oldReview/*, Review newReview*/) {
        removeReview(oldReview);
    }

    public void removeReview(Review review) {
        if(review.isValid()) {
            review.setValid(false);
            review.setDeleted(true);
        }
        eventPublisher.reviewDeleted(review);
        ReviewPointManager.getInstance(myProject).reloadReviewPoint(review);
        if(!removed.contains(review))
            removed.add(review);
        filePath2reviews.get(review.getFilePath()).remove(review);
    }

    public void removeAll(VirtualFile file) {
        List<Review> reviews = filePath2reviews.get(getFilePath(file));
        if(!(reviews == null || reviews.isEmpty())) {
            List<Review> removableReviews = new ArrayList<Review>(reviews);
            for (Review review : removableReviews) {
                removeReview(review);
            }
        } else {
            for(VirtualFile child : file.getChildren()) {
                removeAll(child);
            }
        }
    }

    @Nullable
    public List<Review> getValidReviews(String filepath) {
        ArrayList<Review> reviewsList = new ArrayList<Review>();
        List<Review> reviews = filePath2reviews.get(filepath);
        if(reviews == null || reviews.isEmpty()) return reviews;
        for(Review review : reviews) {
            if(review.isValid()) {
                reviewsList.add(review);
            }
        }
        return reviewsList;
    }

    private List<ReviewBean> getReviewsForFile(String filepath) {
        List<Review> reviewsPart = filePath2reviews.get(filepath);
        if(reviewsPart != null && !reviewsPart.isEmpty()) {
            List<ReviewBean> reviewBeans = new ArrayList<ReviewBean>();
            for(Review review : reviewsPart) {
                reviewBeans.add(review.getReviewBean());
            }
            return reviewBeans;
        }
        return null;
    }
    @Nullable
    public Set<String> getFileNames() {
        return filePath2reviews.keySet();
    }

    public int getReviewCount(@NotNull Collection<VirtualFile> virtualFiles) {
        int reviewCount = 0;
        if(!saveReviewsToPatch) return reviewCount;
        for(VirtualFile file : virtualFiles) {
                List<Review> reviews = filePath2reviews.get(getFilePath(file));
                if(!(reviews == null || reviews.isEmpty())) {
                    reviewCount += reviews.size();
                }
            }
        return reviewCount;
    }

    public Review getReviewInLine(String url, int line) {
        List<Review> reviews = filePath2reviews.get(url);
        if(!(reviews == null || reviews.isEmpty())) {
            for(Review review : reviews) {
                if(review.getLineNumber() == line) return review;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "ReviewManager";
    }

    public void logInvalidReview(Review review) {
        String message = "Review with start offset " + String.valueOf(review.getStart())
                    + " and file \"" + review.getFilePath() + "\" became invalid";
        LOG.warn(message);
    }

    private String getFilePath(VirtualFile file) {
        VirtualFile baseDir = myProject.getBaseDir();
        if(baseDir == null) return "";
        return VfsUtil.getRelativePath(file, baseDir, '/');
    }

    public void setSaveReviewsToPatch(boolean saveReviewsToPatch) {
        this.saveReviewsToPatch = saveReviewsToPatch;
    }

   public String getExportText(boolean prettyFormat) {
        ReviewsState.State state = new ReviewsState.State();
        state.setReviews(getState());
        final String reportText = serialize(state);
        if(prettyFormat) {
            if(state.getReviews().isEmpty()) return null;
            return /*"<!--" + reportText + "-->"
                    +*/ getHTMLReport(reportText/*XmlSerializer.serialize(state)*/);
        }
        return reportText;
   }

   public String getHTMLReport(String reportText) {
        try {
            URL xsltUrl = getClass().getResource("/web/report.xsl");
            Source xslSource = new StreamSource(URLUtil.openStream(xsltUrl));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setURIResolver(new URIResolver() {
                @Override
                public Source resolve(String href, String base) throws TransformerException {
                    URL url = getClass().getResource("/web/" + href.substring(7));
                    try {
                        return new StreamSource(URLUtil.openStream(url));
                    } catch (IOException e) {
                        LOG.error(e);
                    }
                    return null;
                }
            });
            StringWriter stringWriter = new StringWriter();
            transformerFactory.newTransformer(xslSource).transform(
                    new StreamSource(new StringReader(reportText)),
                    new StreamResult(stringWriter)
            );
            return Util.getInstance(myProject).getHTMLContents(stringWriter.toString());
        } catch (IOException e) {
           // todo
        } catch (TransformerConfigurationException e) {
           // todo
        } catch (TransformerException e) {
           // todo
        }
        return null;
    }

    public String getExportTextForFile(String filepath) {
       final List<ReviewBean> reviewsForFile = getReviewsForFile(filepath);
       if(reviewsForFile == null || reviewsForFile.isEmpty()) return "";
       ReviewsState.FileReviewsList fileReviewsList = new ReviewsState.FileReviewsList(filepath,
                                                                                       Util.getInstance(myProject).getCheckSum(filepath),
                                                                                       reviewsForFile);
       ReviewsState.State state = new ReviewsState.State();
       state.getReviews().add(fileReviewsList);
       return serialize(state);
   }

    public String serialize(Object state) {
        String result = "";
        Element addedElement = XmlSerializer.serialize(state);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        result += outputter.outputString(addedElement);
        return result;
    }

    public void importReviewsForFile(String path, String content) {
        VirtualFile file = myProject.getBaseDir().findFileByRelativePath(path);
            if(file == null) return;
            try {
                SAXBuilder builder = new SAXBuilder();
                Element root = builder.build(new StringReader(content)).getRootElement();
                ReviewsState.State state = XmlSerializer.deserialize(root, ReviewsState.State.class);
                ReviewManager reviewManager = ReviewManager.getInstance(myProject);
                reviewManager.loadReviewsForFile(state.getReviews());
            } catch(JDOMException e) {
                //todo e.printStackTrace();
            } catch(NullPointerException e) {
                //todo e.printStackTrace();
            } catch (IOException e) {
                //todo e.printStackTrace();
            }
    }

    public String[] getAuthors() {
        if(!authors.contains(System.getProperty("user.name"))) {
            authors.add(System.getProperty("user.name"));
        }
        return authors.toArray(new String[authors.size()]);
    }

    public void addTags(List<String> tags) {
        availableTags.addAll(tags);
    }

    public String[] getAvailableTags() {
        return availableTags.toArray(new String[availableTags.size()]);
    }

    private class ReviewVirtualFileListener extends VirtualFileAdapter {
        @Override
        public void beforeFileMovement(VirtualFileMoveEvent event) {
            String  url  = getFilePath(event.getOldParent()) + File.pathSeparator + event.getFileName();
            String  newUrl  = getFilePath(event.getNewParent()) + File.pathSeparator  + event.getFileName();
            List<Review> reviewList = filePath2reviews.get(url);
            if(!(reviewList == null || reviewList.isEmpty())) {
                filePath2reviews.remove(url);
                filePath2reviews.put(newUrl, reviewList);
                for (Review review : reviewList) {
                    eventPublisher.reviewDeleted(review);
                    review.setFilePath(newUrl);
                }
            }
        }

        @Override
        public void fileMoved(VirtualFileMoveEvent event) {
            String  newUrl  = getFilePath(event.getNewParent()) + File.pathSeparator  + event.getFileName();
            List<Review> reviewList = filePath2reviews.get(newUrl);
            if(!(reviewList == null || reviewList.isEmpty())) {
                for (Review review : reviewList) {
                    eventPublisher.reviewAdded(review);
                }
            }
        }

        @Override
        public void beforeFileDeletion(VirtualFileEvent event) {
            VirtualFile oldFile = event.getFile();
            String url = getFilePath(oldFile);
            if(filePath2reviews.containsKey(url)) {
                List<Review> reviewList = filePath2reviews.get(url);
                filePath2reviews.remove(url);
                for (Review review : reviewList) {
                    review.setDeleted(true);
                }
            }
        }
    }
}
