package gitlet;

import java.io.Serializable;
import java.io.File;
/** Blobs class for Gitlet, a class that creates the
 * x    Blob object, which has the name,
 *  content, and hash ID associated with the file.
 *  @author Minh Nguyen
 */

public class Blobs implements Serializable {

    /** Create the Blobs object with the given parameter.
     * @param fileName The name of the Blob file.
     * @param cwd The current wording directory.
     * */
    public Blobs(String fileName, String cwd) {
        File blobFile = new File(cwd, fileName);
        name = fileName;
        content = Utils.readContentsAsString(blobFile);
        hash = Utils.sha1(fileName + content);
    }

    /** The function that returns the name of the Blob. */
    public String getName() {
        return name;
    }

    /** The function that returns the content of the Blob. */
    public String getContent() {
        return content;
    }

    /** The function that returns the hashId of the Blob. */
    public String getHash() {
        return hash;
    }

    /** The name of the Blob. */
    private String name;

    /** The content of the Blob. */
    private String content;

    /** The hashId of the Blob. */
    private String hash;

}