package gitlet;
import java.io.Serializable;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Commits class for Gitlet, a class that captures
 * all the information of commits being made throughout time.
 * It also keeps track of the branches, including the head branch.
 *  @author Minh Nguyen
 */

public class Commits implements Serializable {

    /** Create a Commit object with the given parameters.
     * @param msg The log message given by the user.
     * @param parent The hashId of the parent commit.
     * @param blobs The blobs referenced with the commit;
     * key = file's name & value = file's hash.
     * */
    public Commits(String msg, String parent, HashMap<String, String> blobs) {
        message = msg;
        SimpleDateFormat timeStamp =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        Date current = new Date();
        time = timeStamp.format(current);
        parentId = parent;
        parent2Id = "";
        hashId = Utils.sha1(parentId + time + message);
        blob = blobs;
    }

    public Commits(String msg, String parent1,
                   String parent2, HashMap<String, String> blobs) {
        message = msg;
        SimpleDateFormat timeStamp =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        Date current = new Date();
        time = timeStamp.format(current);
        parentId = parent1;
        parent2Id = parent2;
        hashId = Utils.sha1(parentId + parent2Id + time + message);
        blob = blobs;
    }

    /** Get the time of the commit.
     * @return the time of the commit.  */
    public String getTime() {
        return time;
    }

    /** Get the hashId of the parent commit.
     * @return the hashId of the parent commit. */
    public String getParentId() {
        return parentId;
    }

    /** Get the hashId of the first parent commit.
     * @return the hashId of the parent commit. */
    public String getParent1Id() {
        return parentId;
    }

    /** Get the hashId of the second parent commit.
     * @return the hashId of the parent commit. */
    public String getParent2Id() {
        return parent2Id;
    }


    /** Get the mesasge of the commit.
     * @return the message of the commit. */
    public String getMessage() {
        return message;
    }

    /** Get the hashId of the commit.
     * @return the hashId of the commit. */
    public String getHashId() {
        return hashId;
    }

    /** Get the blobs associated with the commit.
     * @return the blobs of the commits. */
    public HashMap<String, String> getBlob() {
        return blob;
    }

    /** Show whether the commit is a merge commit.
     * @return boolean value */
    public boolean isMerged() {
        if (!parent2Id.isEmpty()) {
            return true;
        }
        return false;
    }
    /** The log message given by the user. */
    private String message;

    /** The timestamp of the commit. */
    private String time;

    /** The hashId of the commit. */
    private String hashId;

    /** The hashId of the parent commit. */
    private String parentId;

    /** The hashId of the first parent commit. */
    private String parent1Id = "";

    /** The hashId of the second parent commit. */
    private String parent2Id = "";

    /** The blobs referenced with the commit. */
    private HashMap<String, String> blob;

}