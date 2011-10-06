package ui.actions;

import com.intellij.lang.Commenter;
import com.intellij.lang.LanguageCommenters;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.undo.DocumentReference;
import com.intellij.openapi.command.undo.UndoManager;
import com.intellij.openapi.command.undo.UndoableAction;
import com.intellij.openapi.command.undo.UnexpectedUndoException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import reviewresult.Review;
import reviewresult.ReviewManager;
import utils.ReviewsBundle;
import utils.Util;

import javax.swing.*;

//import com.intellij.tasks.actions.context.UndoableCommand;

/**
 * User: Alisa.Afonina
 * Date: 8/29/11
 * Time: 3:30 PM
 */
public class ConvertToTextCommentAction extends AnAction implements DumbAware{
    private static final Icon ICON = IconLoader.getIcon("/images/note_edit.png");
    private Review review;
    @SuppressWarnings({"UnusedDeclaration"})
    public ConvertToTextCommentAction() {
    }

    public ConvertToTextCommentAction(String title) {
        super(title, title, ICON);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        DataContext dataContext = e.getDataContext();

        Project project = PlatformDataKeys.PROJECT.getData(dataContext);
        if (project == null) return;
        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if(editor != null) {
            Review review = ReviewActionManager.getInstance().getReviewForAction(e);
            if(review == null || !review.isValid()) {return;}
            this.review = review;
            final Document document = editor.getDocument();
            convert(document);
        }
    }

    private void convert(final Document document) {
        Project project = review.getProject();
        int offset = document.getLineStartOffset(review.getLineNumber());
        final PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        if(psiFile == null) return;
        final Commenter commenter = LanguageCommenters.INSTANCE.forLanguage(psiFile.getLanguage());

        String commentSuffix = commenter.getBlockCommentSuffix();
        String commentPrefix = commenter.getBlockCommentPrefix();
        String lineCommentPrefix = commenter.getLineCommentPrefix();

        String reviewText = review.getReviewText();
        if(commentSuffix == null) {
            String[] reviews = reviewText.split("\n");
            StringBuilder builder = new StringBuilder("");
            for(String reviewPart : reviews) {
                builder.append(lineCommentPrefix);
                builder.append(reviewPart);
                builder.append("\n");
            }
            reviewText = builder.toString();
        } else {
            reviewText = commentPrefix + reviewText + commentSuffix + "\n";
        }

        if(review.getLineNumber() > 0) {
            final int prevLineStart = document.getLineStartOffset(review.getLineNumber() - 1);
            final int prevLineEnd = document.getLineEndOffset(review.getLineNumber()-1);

            final String text = document.getText(new TextRange(prevLineStart, prevLineEnd));
            if(text.startsWith(lineCommentPrefix) || text.equals(commentSuffix)){
                if(Messages.showOkCancelDialog(ReviewsBundle.message("reviews.twoReviewsInLineQuestion"),
                                                ReviewsBundle.message("reviews.twoReviewsInLine"),
                                                Messages.getWarningIcon()) == Messages.CANCEL){
                        return;
                }
                if (text.equals(commentSuffix)) {
                    offset = prevLineStart;
                    reviewText = review.getReviewText();
                }
            }
        }

        final String finalReviewText = reviewText;
        final int finalOffset = offset;
        ApplicationManager.getApplication().runWriteAction( new Runnable() {
            @Override
            public void run() {
                document.insertString(finalOffset, finalReviewText);
            }
        });
        ReviewManager.getInstance(project).removeReview(review);
        final Review finalReview = review;
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            public void run() {
                if (finalReview != null) {
                    UndoManager.getInstance(review.getProject()).
                            undoableActionPerformed(
                                    new UndoReviewRemovalAction(finalReview, finalOffset, finalReviewText, document));
                }
            }
        }, "convert review", null);
    }

}

