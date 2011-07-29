package reviewresult;

import com.intellij.openapi.diff.impl.patch.PatchEP;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.StringReader;

/**
 * User: Alisa.Afonina
 * Date: 7/28/11
 * Time: 3:51 PM
 */
public class ReviewPatchExtensionPoint implements PatchEP{
    @NotNull
    @Override
    public String getName() {
        return "reviewresult.reviewpatchextensionpoint";
    }

    @Override
    public CharSequence provideContent(@NotNull String path) {
        String result = "";
        for(Project project : ProjectManager.getInstance().getOpenProjects()) {
            VirtualFile file = project.getBaseDir().findFileByRelativePath(path);
            if(file == null) return "";

            ReviewManager.State state = new ReviewManager.State();

            state.reviews = ReviewManager.getInstance(project).getAddedForFile(file);
            state.removed = ReviewManager.getInstance(project).getRemovedForFile(file);
            if((state.reviews == null || state.reviews.isEmpty()) && (state.removed == null || state.removed.isEmpty()) ) return "";
            Element addedElement = XmlSerializer.serialize(state);
            XMLOutputter outputter = new XMLOutputter(Format.getCompactFormat());
            result += outputter.outputString(addedElement);
            break;
        }
        return new StringBuilder(result);
    }

    @Override
    public void consumeContent(@NotNull String path, @NotNull CharSequence content) {
        for(Project project : ProjectManager.getInstance().getOpenProjects()) {
            VirtualFile file = project.getBaseDir().findFileByRelativePath(path);
            if(file == null) return;
            try {
                SAXBuilder builder = new SAXBuilder();
                Element root = builder.build(new StringReader(content.toString())).getRootElement();


                ReviewManager.State state = XmlSerializer.deserialize(root, ReviewManager.State.class);
                ReviewManager reviewManager = ReviewManager.getInstance(project);
                reviewManager.loadReviewsForFile(state);
               // ReviewManager.getInstance(project).unloadReviewsForFile(state);
                reviewManager.updateUI();
            } catch(JDOMException e) {
                e.printStackTrace();
            } catch(NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
