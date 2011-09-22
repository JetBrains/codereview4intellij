package patch;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vcs.ui.RefreshableOnComponent;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.ReviewManager;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

/**
 * User: Alisa.Afonina
 * Date: 8/2/11
 * Time: 12:14 PM
 */
public class ReviewCommitHandlerFactory  extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(CheckinProjectPanel panel, CommitContext commitContext) {
        return new ReviewCommitHandler(panel);
    }

    private class ReviewCommitHandler extends CheckinHandler {
        private final CheckinProjectPanel checkinProjectPanel;
        private JCheckBox checkbox;

        public ReviewCommitHandler(CheckinProjectPanel panel) {
            this.checkinProjectPanel = panel;
        }

        @Override
        public ReturnResult beforeCheckin() {
            if(checkbox != null)
                ReviewManager.getInstance(checkinProjectPanel.getProject()).setSaveReviewsToPatch(checkbox.isSelected());
            return super.beforeCheckin();
        }

        @Nullable
        @Override
        public RefreshableOnComponent getBeforeCheckinConfigurationPanel() {
            Collection<VirtualFile> virtualFiles = checkinProjectPanel.getVirtualFiles();
            int reviewCount = ReviewManager.getInstance(checkinProjectPanel.getProject()).getReviewCount(virtualFiles);
            if(reviewCount > 0) {
                checkbox = new JCheckBox("Add " +
                        reviewCount +
                        " existing reviews in " + virtualFiles.size() + " files");

                final JPanel panel = new JPanel(new GridLayout(1,1));
                panel.add(checkbox);
                return new RefreshableOnComponent() {
                    @Override
                    public JComponent getComponent() {
                        return panel;
                    }

                    @Override
                    public void refresh() {
                        Collection<VirtualFile> virtualFiles = checkinProjectPanel.getVirtualFiles();
                        int reviewCount = ReviewManager.getInstance(checkinProjectPanel.getProject()).getReviewCount(virtualFiles);
                        checkbox.setText("Add " +
                        String.valueOf(reviewCount) +
                        "existing reviews in " + virtualFiles.size());
                    }

                    @Override
                    public void saveState() {}

                    @Override
                    public void restoreState() {}
                };
            } else return null;
        }
    }
}
