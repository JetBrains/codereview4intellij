package ui.reviewtoolwindow;

import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.*;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.BalloonBuilder;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewsState;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;

/**
 * User: Alisa.Afonina
 * Date: 8/9/11
 * Time: 4:55 PM
 */
public class ReviewToolWindowActionManager implements DumbAware{
    private ReviewPanel panel;
    private ReviewToolWindowSettings settings;

    public ReviewToolWindowActionManager(ReviewPanel panel, ReviewToolWindowSettings settings) {
        this.panel = panel;
        this.settings = settings;
    }

    private final class GroupByModuleAction extends ToggleAction implements DumbAware {

        private GroupByModuleAction() {
             super("Group reviews by module", "Group reviews by module", IconLoader.getIcon("/objectBrowser/showModules.png"));///actions/modul.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isGroupByModule();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            settings.setGroupByModule(state);
            updateUI();
        }
    }

    private final class GroupByFileAction extends ToggleAction  implements DumbAware {

        private GroupByFileAction() {
             super("Group reviews by file","Group reviews by file", IconLoader.getIcon("/fileTypes/text.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isGroupByFile();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            settings.setGroupByFile(state);
            updateUI();
        }
    }

    private final class SearchAction extends ToggleAction  implements DumbAware {

        private SearchAction() {
             super("Search in reviews", "Search in reviews", IconLoader.getIcon("/actions/find.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isSearchEnabled();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            settings.setSearchEnabled(state);
            if(!settings.isSearchEnabled()) {
                if(project == null) {return;}
                Searcher.getInstance(project).emptyFilter();
            }
            updateUI();
        }
    }

    private final class PreviewAction extends ToggleAction  implements DumbAware {

        public PreviewAction() {
            super("Preview reviews", "Preview reviews", IconLoader.getIcon("/actions/preview.png"));
        }



        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isShowPreviewEnabled();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            settings.setShowPreviewEnabled(state);
            panel.showPreview();
        }
    }

    private static final class ExportToFileAction extends AnAction  implements DumbAware {

        private static int FADEOUT_TIME = 1000;

        public ExportToFileAction() {
            super("Export to file...", "Export reviews to file", IconLoader.getIcon("/actions/export.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            FileSaverDescriptor descriptor = new FileSaverDescriptor("Save Reviews", "Export reviews to file", "xml");
            Project project = e.getData(PlatformDataKeys.PROJECT);
            FileSaverDialog saverDialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
            if(project == null) {return;}
            final VirtualFile baseDir = project.getBaseDir();
            VirtualFileWrapper wrapper = saverDialog.save(baseDir, null);
            if(wrapper == null) return;
            final VirtualFile file = wrapper.getVirtualFile(true);

            String text = ReviewManager.getInstance(project).getExportText();
            if(text == null) Messages.showInfoMessage("There are no reviews to export", "Nothing To Export");
            if( file == null || !file.isWritable()) return;
            BalloonBuilder balloonBuilder;
            final Component component = e.getInputEvent().getComponent();
            final Point centerPoint = new Point(component.getHeight()/ 2 ,component.getWidth()/ 2);
            try {
                OutputStream outputStream = file.getOutputStream(null);
                if(text == null) return;
                outputStream.write(text.getBytes());
                outputStream.flush();
                outputStream.close();
                final String htmlContent = "<a href= \"" + file.getPath() + "\">Patch successfully created</a>";
                 balloonBuilder = JBPopupFactory.getInstance().
                                                createHtmlTextBalloonBuilder(
                                                        htmlContent,
                                                        MessageType.INFO,
                                                        new HyperlinkAdapter() {
                                                            @Override
                                                            protected void hyperlinkActivated(HyperlinkEvent e) {
                                                                ShowFilePathAction.open(VfsUtil.virtualToIoFile(file), null);
                                                            }
                                                        });
                balloonBuilder.setFadeoutTime(FADEOUT_TIME);

            } catch (IOException e1) {
                balloonBuilder = JBPopupFactory.getInstance().
                                                createHtmlTextBalloonBuilder("While saving " + file.getName() + " error occured",
                                                        MessageType.ERROR, null);
            }
            Balloon balloon = balloonBuilder.createBalloon();
            balloon.show(new RelativePoint(component, centerPoint), Balloon.Position.above);

        }
    }

    private static final class ImportFromFileAction extends AnAction  implements DumbAware {

        public ImportFromFileAction() {
            super("Import from file...", "Import reviews from file", IconLoader.getIcon("/actions/import.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
            FileChooserDialog chooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
            VirtualFile[] files = chooserDialog.choose(null, project);
            if(files.length != 1) {return;} //may be show warning message?
            VirtualFile virtualFile = files[0];
            try {
                String contents = new String(virtualFile.contentsToByteArray());
                SAXBuilder builder = new SAXBuilder();
                Element root = builder.build(new StringReader(contents)).getRootElement();
                ReviewsState.State state = XmlSerializer.deserialize(root, ReviewsState.State.class);
                ReviewManager reviewManager = ReviewManager.getInstance(project);
                reviewManager.loadReviews(state.reviews, true);
            } catch(JDOMException e2) {
              //todo  e2.printStackTrace();
            } catch(NullPointerException e2) {
              //todo  e2.printStackTrace();
            } catch (IOException e2) {
              //todo  e2.printStackTrace();
            }
        }
    }

    private void updateUI() {
        panel.updateUI();
    }

    public JPanel createLeftMenu() {
        JPanel toolBar = new JPanel(new GridLayout());

        DefaultActionGroup leftGroup = new DefaultActionGroup();
        leftGroup.add(new PreviousOccurenceToolbarAction(panel));
        leftGroup.add(new NextOccurenceToolbarAction(panel));
        leftGroup.add(new PreviewAction());
        leftGroup.add(new GroupByModuleAction());
        leftGroup.add(new GroupByFileAction());
        leftGroup.add(new SearchAction());
        leftGroup.add(new ExportToFileAction());
        leftGroup.add(new ImportFromFileAction());
        toolBar.add(ActionManager.getInstance()
                    .createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, leftGroup, false)
                    .getComponent());
        return toolBar;
    }


}
