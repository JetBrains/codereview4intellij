package ui.reviewtoolwindow;

import com.intellij.ide.BrowserUtil;
import com.intellij.ide.actions.NextOccurenceToolbarAction;
import com.intellij.ide.actions.PreviousOccurenceToolbarAction;
import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.fileChooser.FileChooserDialog;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.HyperlinkAdapter;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.ui.switcher.SwitchTarget;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewsState;
import ui.forms.ReviewSaveForm;

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
    private final ReviewPanel panel;
    private final ReviewToolWindowSettings settings;
    private ComponentPopupBuilder popupBuilder;


    public ReviewToolWindowActionManager(ReviewPanel panel, ReviewToolWindowSettings settings) {
        this.panel = panel;
        this.settings = settings;
    }

    public void createSortMenu() {
        DefaultActionGroup sortGroup = new DefaultActionGroup();
        sortGroup.addAction(new SortByAuthorAction());
        sortGroup.addAction(new SortByLastCommenterAction());
        sortGroup.addAction(new SortByDateAction());
        sortGroup.addAction(new SortByOffsetAction());
        final ActionToolbar sortToolbar = ActionManager.getInstance()
                .createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, sortGroup, true);
        popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(sortToolbar.getComponent(), panel);
    }

    private final class GroupByModuleAction extends ToggleAction implements DumbAware {

        private GroupByModuleAction() {
             super("Group reviews by module", "Group reviews by module", IconLoader.getIcon("/objectBrowser/showModules.png"));///actions/modul.png"));
            this.setEnabledInModalContext(false);
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

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled());
        }
    }

    private final class GroupByFileAction extends ToggleAction  implements DumbAware {

        private GroupByFileAction() {
             super("Group reviews by file","Group reviews by file", IconLoader.getIcon("/fileTypes/text.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            //final boolean sortingInNotFileMode = (settings.isSortByAuthor() || settings.isSortByDate() || settings.isSortByLastCommenter());
            return settings.isGroupByFile();// && !sortingInNotFileMode;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            //final boolean sortingInNotFileMode = (settings.isSortByAuthor() || settings.isSortByDate() || settings.isSortByLastCommenter());
            //if(!sortingInNotFileMode)
            settings.setGroupByFile(state);
            updateUI();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled());
        }
    }

    private final class SortByAuthorAction extends ToggleAction  implements DumbAware {

        private SortByAuthorAction() {
             super("Sort reviews by author","Sort reviews by author", IconLoader.getIcon("/icons/inspector/sortByName.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
           ///System.out.println("Is sort by author selected: "  + settings.isSortByAuthor());
            return true;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
                /*boolean selected = state && (!settings.isSortByDate()
                                            || !settings.isSortByLastCommenter()
                                            || !settings.isSortByOffset());*/
                settings.setSortByAuthor(!settings.isSortByAuthor());
                panel.rebuidTree();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled() && !settings.isGroupByFile());
        }
    }

    private final class SortByLastCommenterAction extends ToggleAction  implements DumbAware {

        private SortByLastCommenterAction() {
             super("Sort reviews by author of last comment","Sort reviews by author of last comment", IconLoader.getIcon("/icons/inspector/sortByName.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return true;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
                settings.setSortByLastCommenter(!settings.isSortByLastCommenter());
                panel.rebuidTree();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled() && !settings.isGroupByFile());
        }
    }

    private final class SortByDateAction extends ToggleAction  implements DumbAware {

        private SortByDateAction() {
             super("Sort reviews by date of creation","Sort reviews by date of creation", IconLoader.getIcon("/actions/analyze.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return true;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
                settings.setSortByDate(!settings.isSortByDate());
                panel.rebuidTree();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled() && !settings.isGroupByFile());
        }
    }

    private final class SortByOffsetAction extends ToggleAction  implements DumbAware {

        private SortByOffsetAction() {
             super("Sort reviews by offset in file","Sort reviews by offset in file", IconLoader.getIcon("/icons/inspector/sortByCategory.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return true;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            settings.setSortByOffset(!settings.isSortByOffset());
            panel.rebuidTree();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled() && settings.isGroupByFile());
        }
    }

    private final class ShowSortingFuctionsAction extends ToggleAction implements DumbAware {
        private boolean showSort;
        private JBPopup popup;

        private ShowSortingFuctionsAction() {
            super("Sorting functions...", "Sort reviews", IconLoader.getIcon("/general/secondaryGroup.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return showSort;
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            showSort = state;
            if(state) {
                popup = popupBuilder.createPopup();
                popup.showUnderneathOf(e.getInputEvent().getComponent());
            }
            else {
                settings.disableAllSorting();
                popup.dispose();
            }
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

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled());
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

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled());
        }
    }

    private static final class ExportToFileAction extends AnAction  implements DumbAware {

        private static final int FADEOUT_TIME = 3000;

        public ExportToFileAction() {
            super("Export to file...", "Export reviews to file", IconLoader.getIcon("/actions/export.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            BalloonBuilder balloonBuilder;
            final Component component = e.getInputEvent().getComponent();
            final Point centerPoint = new Point(component.getHeight()/ 2 ,component.getWidth()/ 2);
            if(project == null) {return;}
            ReviewSaveForm saveDialog = new ReviewSaveForm(project);

            saveDialog.show();

            if(saveDialog.isOK()) {
                final VirtualFile selectedFile = saveDialog.getFile();
                if(selectedFile == null || !selectedFile.exists()) return;
                final boolean xmlFormat = saveDialog.isXMLFormat();
                String text = ReviewManager.getInstance(project).getExportText(!xmlFormat);

                if(text == null || "".equals(text)) Messages.showInfoMessage("There are no reviews to export", "Nothing To Export");

                if(createReport(selectedFile, text)) {
                    final String htmlContent ="Reviews successfully exported to a file <br/>" +
                                              "<a href= \"" + selectedFile.getPath() + "\">Show reviews</a>";
                     balloonBuilder = JBPopupFactory.getInstance().
                                                    createHtmlTextBalloonBuilder(
                                                            htmlContent,
                                                            MessageType.INFO,
                                                            new HyperlinkAdapter() {
                                                                @Override
                                                                protected void hyperlinkActivated(HyperlinkEvent e) {
                                                                    if(xmlFormat)
                                                                        ShowFilePathAction.open(VfsUtil.virtualToIoFile(selectedFile), null);
                                                                    else
                                                                        BrowserUtil.launchBrowser(selectedFile.getPath());

                                                                }
                                                            });
                    balloonBuilder.setFadeoutTime(FADEOUT_TIME);
                } else {
                    showErrorBalloon("While saving " + selectedFile.getName() + " error occured", component, centerPoint);
                    return;
                }
            } else {
                return;
            }
            showBalloon(balloonBuilder, component, centerPoint);
    }



        private boolean  createReport(VirtualFile file, String text) {
            if(text == null) return false;
            try {
                OutputStream outputStream = file.getOutputStream(null);
                outputStream.write(text.getBytes());
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
               return false;
            }
            return true;
        }


    }

    private static final class ImportFromFileAction extends AnAction  implements DumbAware {

        public ImportFromFileAction() {
            super("Import from file...", "Import reviews from file", IconLoader.getIcon("/actions/import.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
             BalloonBuilder balloonBuilder;
            final Component component = e.getInputEvent().getComponent();
            final Point centerPoint = new Point(component.getHeight()/ 2 ,component.getWidth()/ 2);
            Project project = e.getData(PlatformDataKeys.PROJECT);
            if(project == null) {return;}
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false, false, false, false);
            FileChooserDialog chooserDialog = FileChooserFactory.getInstance().createFileChooser(descriptor, project);
            VirtualFile[] files = chooserDialog.choose(null, project);
            if(files == null || files.length != 1) {return;} //may be show warning message?
            VirtualFile virtualFile = files[0];
            try {
                String contents = new String(virtualFile.contentsToByteArray());
                final boolean htmlFile = "html".equals(virtualFile.getExtension());
                if(htmlFile) {
                    final int beginIndex = contents.indexOf("<!--");
                    final int endIndex = contents.indexOf("-->");
                    contents = contents.substring(beginIndex + 4, endIndex);
                }
                SAXBuilder builder = new SAXBuilder();
                Element root = builder.build(new StringReader(contents)).getRootElement();
                ReviewsState.State state = XmlSerializer.deserialize(root, ReviewsState.State.class);
                ReviewManager reviewManager = ReviewManager.getInstance(project);
                reviewManager.loadReviews(state.reviews, true);
                String htmlContent = /*successfullyLoaded + " of " + state.reviews.size() + */" Reviews successfully imported<br/>";
                balloonBuilder = JBPopupFactory.getInstance().
                                            createHtmlTextBalloonBuilder(htmlContent, MessageType.INFO, null);
                showBalloon(balloonBuilder, component, centerPoint);
            } catch(JDOMException e2) {
                showErrorBalloon("While importing reviews from " + virtualFile.getName() + " error occured", component, centerPoint);
            } catch(NullPointerException e2) {
                showErrorBalloon("File is empty or doesn't exist", component, centerPoint);
            } catch (IOException e2) {
                showErrorBalloon("While importing reviews " + virtualFile.getName() + " error occured", component, centerPoint);
            }
        }
    }

    private void updateUI() {
        panel.updateUI();
    }

    public JPanel createLeftMenu() {
        JPanel toolBar = new JPanel(new BorderLayout());

        DefaultActionGroup leftGroup = new DefaultActionGroup();
        leftGroup.add(new PreviousOccurenceToolbarAction(panel));
        leftGroup.addAction(new NextOccurenceToolbarAction(panel));
        leftGroup.add(new PreviewAction());
        leftGroup.add(new GroupByModuleAction());
        leftGroup.add(new GroupByFileAction());
        leftGroup.add(new SearchAction());
        leftGroup.add(new ExportToFileAction());
        leftGroup.add(new ImportFromFileAction());
        leftGroup.add(new ShowSortingFuctionsAction());

        final ActionToolbar actionToolbar = ActionManager.getInstance()
                .createActionToolbar(ActionPlaces.TODO_VIEW_TOOLBAR, leftGroup, false);
        toolBar.add(actionToolbar.getComponent());
        createSortMenu();
        return toolBar;
    }

    private static void showBalloon(BalloonBuilder balloonBuilder, Component component, Point centerPoint) {

            Balloon balloon = balloonBuilder.createBalloon();
            balloon.show(new RelativePoint(component, centerPoint), Balloon.Position.above);
        }

    private static void showErrorBalloon(String message, Component component, Point centerPoint) {
        BalloonBuilder balloonBuilder = JBPopupFactory.getInstance().
                                                        createHtmlTextBalloonBuilder(message,
                                                                MessageType.ERROR, null);
        showBalloon(balloonBuilder, component, centerPoint);
    }


}
