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
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import reviewresult.ReviewManager;
import reviewresult.persistent.ReviewsState;
import ui.forms.SaveReviewsFormWrapper;
import ui.reviewtoolwindow.filter.Searcher;
import utils.ReviewsBundle;

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
        popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(
                                                                            sortToolbar.getComponent(),
                                                                            panel);
    }

    private final class GroupByModuleAction extends ToggleAction implements DumbAware {

        private GroupByModuleAction() {
             super(ReviewsBundle.message("reviews.groupByModule"),
                   ReviewsBundle.message("reviews.groupByModule"),
                   IconLoader.getIcon("/objectBrowser/showModules.png"));///actions/modul.png"));
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
             super(ReviewsBundle.message("reviews.groupByFile"),
                    ReviewsBundle.message("reviews.groupByFile"),
                   IconLoader.getIcon("/fileTypes/text.png"));
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

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled());
        }
    }

    private final class SortByAuthorAction extends ToggleAction  implements DumbAware {

        private SortByAuthorAction() {
             super(ReviewsBundle.message("reviews.sortByAuthor"),
                   ReviewsBundle.message("reviews.sortByAuthor"),
                   IconLoader.getIcon("/icons/inspector/sortByName.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isSortByAuthor();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
                //settings.setSortByAuthor(state);
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
             super(ReviewsBundle.message("reviews.sortByAuthorOfLastComment"),
                   ReviewsBundle.message("reviews.sortByAuthorOfLastComment"),
                   IconLoader.getIcon("/icons/inspector/sortByName.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isSortByLastCommenter();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
                settings.setSortByLastCommenter(!settings.isSortByLastCommenter());
                //settings.setSortByLastCommenter(state);
                panel.rebuidTree();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled() && !settings.isGroupByFile());
        }
    }

    private final class SortByDateAction extends ToggleAction  implements DumbAware {

        private SortByDateAction() {
             super(ReviewsBundle.message("reviews.sortByDateOfCreation"),
                   ReviewsBundle.message("reviews.sortByDateOfCreation"),
                   IconLoader.getIcon("/actions/analyze.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isSortByDate();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
                //settings.setSortByDate(state);
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
             super(ReviewsBundle.message("reviews.sortByOffset"),
                   ReviewsBundle.message("reviews.sortByOffset"),
                   IconLoader.getIcon("/icons/inspector/sortByCategory.png"));
        }


        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isSortByOffset();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            settings.setSortByOffset(state);
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
            super(ReviewsBundle.message("reviews.sortingFunctionsEllipsis"),
                  ReviewsBundle.message("reviews.sortingFunctionsMessage"),
                  IconLoader.getIcon("/general/secondaryGroup.png"));
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
                popup.addListener(new JBPopupAdapter() {
                    @Override
                    public void onClosed(LightweightWindowEvent event) {
                        showSort = false;
                    }
                });
            }
            else {
                settings.disableAllSorting();
                popup.dispose();
            }
        }
    }

    private final class SearchAction extends ToggleAction  implements DumbAware {

        private SearchAction() {
             super(ReviewsBundle.message("reviews.searchReviews"),
                   ReviewsBundle.message("reviews.searchReviews"), IconLoader.getIcon("/actions/find.png"));
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
            super(ReviewsBundle.message("reviews.previewReviews"),
                  ReviewsBundle.message("reviews.previewReviews"),
                  IconLoader.getIcon("/actions/preview.png"));
        }

        @Override
        public boolean isSelected(AnActionEvent e) {
            return settings.isShowPreviewEnabled();
        }

        @Override
        public void setSelected(AnActionEvent e, boolean state) {
            settings.setShowPreviewEnabled(state);
            panel.updateUI();
        }

        @Override
        public void update(AnActionEvent e) {
            e.getPresentation().setEnabled(settings.isEnabled());
        }
    }

    private static final class ExportToFileAction extends AnAction  implements DumbAware {

        private static final int FADEOUT_TIME = 3000;

        public ExportToFileAction() {
            super(ReviewsBundle.message("reviews.exportToFileEllipsis"),
                  ReviewsBundle.message("reviews.exportToFile"),
                  IconLoader.getIcon("/actions/export.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            Project project = e.getData(PlatformDataKeys.PROJECT);
            BalloonBuilder balloonBuilder;
            final Component component = e.getInputEvent().getComponent();
            final Point centerPoint = new Point(component.getHeight()/ 2 ,component.getWidth()/ 2);
            if(project == null) {return;}
            SaveReviewsFormWrapper saveDialog = new SaveReviewsFormWrapper(project);

            saveDialog.show();

            if(saveDialog.isOK()) {
                final VirtualFile selectedFile = saveDialog.getFile();
                if(selectedFile == null || !selectedFile.exists()) return;
                final boolean xmlFormat = saveDialog.isXMLFormat();
                String text = ReviewManager.getInstance(project).getExportText(!xmlFormat);

                if(text == null || "".equals(text)) Messages.showInfoMessage(
                                               ReviewsBundle.message("reviews.noReviewsToExportMessage"),
                                               ReviewsBundle.message("reviews.noReviewsToExport"));

                if(createReport(selectedFile, text)) {
                    final String htmlContent = ReviewsBundle.message("reviews.successfullyExported") + "<br/>" +
                                              "<a href= \"" + selectedFile.getPath() + "\">" +
                                              ReviewsBundle.message("reviews.showReviews") + "</a>";
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
                    showErrorBalloon( ReviewsBundle.message("reviews.savingError", selectedFile.getName()), component, centerPoint);
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
            super(ReviewsBundle.message("reviews.importFromFileEllipsis"),
                  ReviewsBundle.message("reviews.importFromFile"),
                  IconLoader.getIcon("/actions/import.png"));
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
             BalloonBuilder balloonBuilder;
            final Component component = e.getInputEvent().getComponent();
            final Point centerPoint = new Point(component.getHeight()/ 2 ,component.getWidth()/ 2);
            Project project = e.getData(PlatformDataKeys.PROJECT);
            if(project == null) {return;}
            FileChooserDescriptor descriptor = new FileChooserDescriptor(true, false, false,
                                                                         false, false, false);
            FileChooserDialog chooserDialog = FileChooserFactory.getInstance().
                                                                        createFileChooser(descriptor, project);
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
                reviewManager.loadReviews(state.getReviews(), true);
                String htmlContent = ReviewsBundle.message("reviews.successfullyImported");
                balloonBuilder = JBPopupFactory.getInstance().
                                            createHtmlTextBalloonBuilder(htmlContent, MessageType.INFO, null);
                showBalloon(balloonBuilder, component, centerPoint);
            } catch(JDOMException e2) {
                showErrorBalloon(ReviewsBundle.message("reviews.importingError", virtualFile.getName()), component, centerPoint);
            } catch(NullPointerException e2) {
                showErrorBalloon(ReviewsBundle.message("reviews.fileError"), component, centerPoint);
            } catch (IOException e2) {
                showErrorBalloon( ReviewsBundle.message("reviews.importingError" ,virtualFile.getName()), component, centerPoint);
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
