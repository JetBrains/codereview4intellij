package reviewresult;

import java.util.ArrayList;
import java.util.List;

/**
 * User: Alisa.Afonina
 * Date: 7/11/11
 * Time: 6:38 PM
 */
public enum ReviewStatus {
    bug,
    comment,
    info;

    public static String[] getVariants() {
        List<String> variants = new ArrayList<String>();
        for(ReviewStatus status : values()) {
            variants.add(status.name());
        }
        return variants.toArray(new String[variants.size()]);
    }
}
