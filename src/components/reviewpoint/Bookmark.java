 /*
   2  * Copyright 2000-200  JetBrains s.r.o.
      *
      * Licensed under the Apache License, Version 2.0 (the "License");
      * you may not use this file except in compliance with the License.
      * You may obtain a copy of the License at
      *
      * http://www.apache.org/licenses/LICENSE-2.0
      *
  1   * Unless required by applicable law or agreed to in writing, software
  11  * distributed under the License is distributed on an "AS IS" BASIS,
  12  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  1   * See the License for the specific language governing permissions and
  1   * limitations under the License.
  1   */
  
 package components.reviewpoint;

 import com.intellij.ide.IdeBundle;
 import com.intellij.ide.structureView.StructureViewBuilder;
 import com.intellij.ide.structureView.StructureViewModel;
 import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
 import com.intellij.lang.LanguageStructureViewBuilder;
 import com.intellij.navigation.ItemPresentation;
 import com.intellij.navigation.NavigationItem;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.editor.RangeMarker;
 import com.intellij.openapi.editor.ex.MarkupModelEx;
 import com.intellij.openapi.editor.markup.GutterIconRenderer;
 import com.intellij.openapi.editor.markup.HighlighterLayer;
 import com.intellij.openapi.editor.markup.RangeHighlighter;
 import com.intellij.openapi.fileEditor.FileDocumentManager;
 import com.intellij.openapi.fileEditor.OpenFileDescriptor;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.Comparing;
 import com.intellij.openapi.util.IconLoader;
 import com.intellij.openapi.util.text.StringUtil;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiDocumentManager;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;

 import javax.swing.*;
 import java.awt.*;

 public class Bookmark {
       private static final Icon TICK = IconLoader.getIcon("/gutter/check.png");
       private final VirtualFile myFile;
       private final OpenFileDescriptor myTarget;
       private final RangeHighlighter myHighlighter;
       private final Project myProject;
     
       private String myDescription;
     
       public Bookmark(Project project, VirtualFile file, String description) {
         this(project, file, -1 , description);
       }
     
       public Bookmark(Project project, VirtualFile file, int line, String description) {
         myFile = file;
         myProject = project;
         myDescription = description;
     
         if (line >= 0) {
           Document document = getDocument();
           if (document == null) {
             myHighlighter = null;
           }
           else {
             MarkupModelEx markup = (MarkupModelEx)document.getMarkupModel(myProject);
             myHighlighter = markup.addPersistentLineHighlighter(line, HighlighterLayer.ERROR + 1, null);
     
     
             if (myHighlighter != null) {
               myHighlighter.setGutterIconRenderer(new MyGutterIconRenderer());
     
               myHighlighter.setErrorStripeMarkColor(Color.black);
               myHighlighter.setErrorStripeTooltip(StringUtil.escapeXml(getNotEmptyDescription()));
             }
           }
         }
         else {
           myHighlighter = null;
         }
     
         myTarget = new OpenFileDescriptor(project, file, line, -1, true);
       }
     
       public Document getDocument() {
         return FileDocumentManager.getInstance().getDocument(getFile());
       }
     
       public void release() {
         if (myHighlighter != null) {
           myHighlighter.dispose();
         }
       }
     
       public Icon getIcon() {
         return TICK;
       }
     
       public String getDescription() {
         return myDescription;
       }
     
       public void setDescription(String description) {
         myDescription = description;
       }

       public VirtualFile getFile() {
         return myFile;
       }
     
       @Nullable
       public String getNotEmptyDescription() {
         return isDescriptionEmpty() ? null : myDescription;
       }
     
       public boolean isDescriptionEmpty() {
         return myDescription == null || myDescription.trim().length() == 0;
       }
     
       OpenFileDescriptor getTarget() {
         return myTarget;
       }
     
       public boolean isValid() {
         if (!getFile().isValid() || (myHighlighter != null && !myHighlighter.isValid())) {
           return false;
         }
     
         // There is a possible case that target document line that is referenced by the current bookmark is removed. We assume
         // that corresponding range marker becomes invalid then.
         RangeMarker rangeMarker = myTarget.getRangeMarker();
         return rangeMarker == null || rangeMarker.isValid();
       }
     
       public void navigate() {
         myTarget.navigate(true);
       }
     
       public int getLine() {
         if (myHighlighter != null && myHighlighter.isValid()) {
           return myHighlighter.getDocument().getLineNumber(myHighlighter.getStartOffset());
         }
         return myTarget.getLine();
       }
     
       @Override
       public String toString() {
         return getQualifiedName();
       }
     
       public String getQualifiedName() {
         String presentableUrl = myFile.getPresentableUrl();
         if (myFile.isDirectory() || myHighlighter == null) return presentableUrl;
     
         PsiDocumentManager.getInstance(myProject).commitAllDocuments();
         final PsiFile psiFile = PsiManager.getInstance(myProject).findFile(myFile);
     
         if (psiFile == null) return presentableUrl;
     
         StructureViewBuilder builder = LanguageStructureViewBuilder.INSTANCE.getStructureViewBuilder(psiFile);
         if (builder instanceof TreeBasedStructureViewBuilder) {
           StructureViewModel model = ((TreeBasedStructureViewBuilder)builder).createStructureViewModel();
           Object element = model.getCurrentEditorElement();
           if (element instanceof NavigationItem) {
             ItemPresentation presentation = ((NavigationItem)element).getPresentation();
             if (presentation != null) {
               presentableUrl = ((NavigationItem)element).getName() + " " + presentation.getLocationString();
             }
           }
         }
     
         return IdeBundle
           .message("bookmark.file.X.line.Y", presentableUrl, myHighlighter.getDocument().getLineNumber(myHighlighter.getStartOffset()) + 1);
       }

       private class MyGutterIconRenderer extends GutterIconRenderer {
         @NotNull
         public Icon getIcon() {
           return Bookmark.this.getIcon();
         }
     
         public String getTooltipText() {
           return StringUtil.escapeXml(getNotEmptyDescription());
         }
     
         @Override
         public boolean equals(Object obj) {
           return obj instanceof MyGutterIconRenderer &&
                  Comparing.equal(getTooltipText(), ((MyGutterIconRenderer)obj).getTooltipText()) &&
                  Comparing.equal(getIcon(), ((MyGutterIconRenderer)obj).getIcon());
         }
     
         @Override
          public int hashCode() {
           return getIcon().hashCode();
        }
    }
}
