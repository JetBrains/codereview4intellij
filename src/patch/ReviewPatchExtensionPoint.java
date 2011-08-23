package patch;

import com.intellij.openapi.diff.impl.patch.PatchEP;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reviewresult.ReviewManager;

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


    @Nullable
    @Override
    public CharSequence provideContent(@NotNull String path) {
        StringBuilder result = new StringBuilder();
        for(Project project : ProjectManager.getInstance().getOpenProjects()) {
            VirtualFile baseDir = project.getBaseDir();
            if(baseDir == null) return null;
            VirtualFile file = baseDir.findFileByRelativePath(path);
            if(file == null) return null;
            result.append(ReviewManager.getInstance(project).getExportTextForFile(VfsUtil.getRelativePath(file, baseDir, '/')));
        }
        return new StringBuilder(result);
    }

    @Override
    public void consumeContent(@NotNull String path, @NotNull CharSequence content) {
        for(Project project : ProjectManager.getInstance().getOpenProjects()) {
            ReviewManager.getInstance(project).importReviewsForFile(path, content.toString());
        }
    }

}
