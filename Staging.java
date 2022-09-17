package gitlet;
import java.io.Serializable;

import java.util.TreeMap;


/** Staging class for Gitlet, a class that keeps track of
 * files that should be tracked in the next commit,
 *  and should not be tracked in the next commmit.
 *  @author Minh Nguyen
 */

public class Staging implements Serializable {

    /** Constructor that create the stage branches. */
    public Staging() {
        tracked = new TreeMap<>();
        removed = new TreeMap<>();
        modifiedList = new TreeMap<>();
        untracked = new TreeMap<>();
    }

    /** Get the map of tracked files.
     * @return the tracked files. */
    public TreeMap<String, String> getTracked() {
        return tracked;
    }

    /** Get the map of removed files.
     * @return the removed files. */
    public TreeMap<String, String> getRemoved() {
        return removed;
    }

    /** Get the map of modified but untracked files.
     * @return the modified files. */
    public TreeMap<String, String> getModified() {
        return modifiedList;
    }

    /** Get the map of all untracked files.
     * @return the untracked files. */
    public TreeMap<String, String> getUntracked() {
        return untracked;
    }

    /** Add the file to be tracked.
     * @param fileName The name of the file.
     * @param hashId The hashId of the file. */
    public void addFile(String fileName, String hashId) {
        tracked.put(fileName, hashId);
    }

    /** Add the file ot be removed.
     * @param fileName The name of the file.
     * @param hashId The hashId of the file.
     */
    public void addRemovedFile(String fileName, String hashId) {
        removed.put(fileName, hashId);
    }

    /** Add the files that are modified.
     * @param fileName The name of the file.
     * @param message Either deleted or modified.
     */
    public void addModified(String fileName, String message) {
        modifiedList.put(fileName, message);
    }

    /** Add the files that are untracked.
     * @param fileName The name of the file.
     * @param hashId The hashId of the file.
     */
    public void addUntracked(String fileName, String hashId) {
        untracked.put(fileName, hashId);
    }

    /** A map of files and their hash id that are tracked in the stage. */
    private TreeMap<String, String> tracked;

    /** A set of files and their hash id that are removed. */
    private TreeMap<String, String> removed;

    /** A map of all modified but not staged for commit files. */
    private TreeMap<String, String> modifiedList;

    /** A set of all untracked files. */
    private TreeMap<String, String> untracked;

}