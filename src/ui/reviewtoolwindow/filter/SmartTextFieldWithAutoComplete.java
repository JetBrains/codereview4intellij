package ui.reviewtoolwindow.filter;

import com.intellij.codeInsight.lookup.*;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.lang.Language;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.IdeActions;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentAdapter;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextLanguage;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.codeStyle.NameUtil;
import com.intellij.ui.EditorTextField;
import com.intellij.util.Function;
import com.intellij.util.LocalTimeCounter;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.text.Matcher;
import org.jetbrains.annotations.Nullable;
import reviewresult.ReviewManager;
import reviewresult.ReviewStatus;
import utils.Util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 9/19/11
 * Time: 12:55 PM
 */
public class SmartTextFieldWithAutoComplete extends EditorTextField implements DumbAware{
    private List<LookupElement> variants;
    private String delimiter = " ";
    private String inactivePrefix = "";

    public SmartTextFieldWithAutoComplete(final Project project) {
        super(createDocument(project), project, PlainTextLanguage.INSTANCE.getAssociatedFileType());
        new VariantsCompletionAction();


        final String[] allVariants = Searcher.getInstance(project).getFilterKeywords();
        setVariants(allVariants);
        setRequestFocusEnabled(true);
        setOneLineMode(true);
        addDocumentListener(new DocumentAdapter() {
            @Override
            public void documentChanged(DocumentEvent e) {
                //String text = e.getDocument().getText().toLowerCase();
                String text = extractSuffix();
                String[] variants = null;
                if (text.endsWith("author:")) {
                    variants = ReviewManager.getInstance(getProject()).getAuthors();
                    setDelimiter("\"");
                }
                if (text.endsWith("status:")) {
                    variants = ReviewStatus.getVariants();
                    setDelimiter(":");
                }
                if (text.endsWith("tag:")) {
                    variants = ReviewManager.getInstance(getProject()).getAvailableTags();
                    setDelimiter("\"");
                }
                if (text.endsWith(" ") || "".equals(text)/*|| text.endsWith("\"")*/) {
                    variants = Searcher.getInstance(project).getFilterKeywords();
                    setDelimiter(" ");
                }

                if (variants != null) {
                    setVariants(variants);
                }
            }
        });

    }

    public String extractSuffix() {
        String text = getText();
        if(!"".equals(inactivePrefix)) {
            int prefixStart = Util.find(text, inactivePrefix, false);
            if(prefixStart >= 0) {
                text = text.substring(prefixStart + inactivePrefix.length());
            } else {
                inactivePrefix = "";
            }
        }
        return text;
    }

    private static Document createDocument(@Nullable final Project project) {
        if (project == null) {
          return EditorFactory.getInstance().createDocument("");
        }

        final Language language = PlainTextLanguage.INSTANCE;
        final PsiFileFactory factory = PsiFileFactory.getInstance(project);
        final FileType fileType = language.getAssociatedFileType();
        assert fileType != null;

        final long stamp = LocalTimeCounter.currentTime();
        final PsiFile psiFile = factory.createFileFromText("Dummy." + fileType.getDefaultExtension(), fileType, "", stamp, true, false);
        final Document document = PsiDocumentManager.getInstance(project).getDocument(psiFile);
        assert document != null;
        return document;
    }

    public void setInactivePrefix(String inactivePrefix) {
        this.inactivePrefix = inactivePrefix;
    }

    private class VariantsCompletionAction extends AnAction {
    private VariantsCompletionAction() {
      final AnAction action = ActionManager.getInstance().getAction(IdeActions.ACTION_CODE_COMPLETION);
      if (action != null) {
        registerCustomShortcutSet(action.getShortcutSet(), SmartTextFieldWithAutoComplete.this);
      }
    }

    public void actionPerformed(final AnActionEvent e) {
      showLookup();
    }
  }

    public void showLookup() {
        if (LookupManager.getInstance(getProject()).getActiveLookup() != null) return;
        final Editor editor = getEditor();
        assert editor != null;

        editor.getSelectionModel().removeSelection();
        final String lookupPrefix = getCurrentLookupPrefix(getCurrentTextPrefix());
        final LookupImpl lookup =
          (LookupImpl)LookupManager.getInstance(getProject()).createLookup(editor,
                                                                           calcLookupItems(lookupPrefix),
                                                                           lookupPrefix != null ? lookupPrefix : "",
                                                                           LookupArranger.DEFAULT);
        lookup.showLookup();
        lookup.addLookupListener(new LookupAdapter() {
            @Override
            public void itemSelected(LookupEvent event) {
                if(delimiter.equals("\"")) {
                    SmartTextFieldWithAutoComplete.this.setText(getText() + "\"");
                }
            }

        });
    }

    public void setVariants(@Nullable final String[] variants) {
        this.variants = (variants == null)
           ? Collections.<LookupElement>emptyList()
           : ContainerUtil.map(variants, new Function<String, LookupElement>() {
            public LookupElement fun(final String s) {
                return LookupElementBuilder.create(s);
            }
        });
    }

    private LookupElement[] calcLookupItems(@Nullable final String lookupPrefix) {
        if (lookupPrefix == null) {
          return new LookupElement[0];
        }

        final List<LookupElement> items = new ArrayList<LookupElement>();
        if (lookupPrefix.length() == 0) {
            items.addAll(variants);
        } else {
            final Matcher matcher = NameUtil.buildMatcher(lookupPrefix, 0, true, true);

            for (LookupElement variant : variants) {
                if (matcher.matches(variant.getLookupString())) {
                    items.add(variant);
                }
            }
        }

    Collections.sort(items, new Comparator<LookupElement>() {
      public int compare(final LookupElement item1,
                         final LookupElement item2) {
        return item1.getLookupString().compareTo(item2.getLookupString());
      }
    });

    return items.toArray(new LookupElement[items.size()]);
    }

    @Nullable
    protected String getCurrentLookupPrefix(final String currentTextPrefix) {
    return currentTextPrefix;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    private String getCurrentTextPrefix() {
        String text = extractSuffix();
        int beginIndex = text.lastIndexOf(delimiter) + 1;
        return text.substring(beginIndex);
    }
}
