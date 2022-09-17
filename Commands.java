package gitlet;

import java.io.Serializable;
import java.io.File;
import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;


/**
 * Commands class for Gitlet, a class that stores the
 * all the commands that are used in Gitlet.
 *
 * @author Minh Nguyen
 */

public class Commands implements Serializable {

    /**
     * Constructor that initializes the working directory.
     */
    public Commands() {
        cwd = System.getProperty("user.dir");
        File lastSave = Utils.join(cwd, ".gitlet/saves");
        if (lastSave.exists()) {
            Commands prev = Utils.readObject(lastSave, Commands.class);
            branches = prev.branches;
            headBranch = prev.headBranch;
            headCommit = prev.headCommit;
            stage = prev.stage;
            commitList = prev.commitList;
            intersection = prev.intersection;
        }
    }

    public void save() {
        File newSave = Utils.join(cwd, ".gitlet", "saves");
        Utils.writeObject(newSave, this);
    }

    /**
     * The command init().
     * Creates a new Gitlet version-control system in
     * the current directory that start with an initial commit.
     */
    public void init() {


        File gitletDirectory = Utils.join(cwd, ".gitlet");
        if (gitletDirectory.exists()) {
            System.out.println("Gitlet version-control system "
                    + "already exists in the current directory.");
            System.exit(0);
        } else {
            gitletDirectory.mkdir();
        }


        HashMap<String, String> emptyBlob = new HashMap<>();
        Commits initialCommit = new Commits("initial commit", null, emptyBlob);


        File initial = Utils.join(cwd, ".gitlet/" + initialCommit.getHashId());
        Utils.writeObject(initial, initialCommit);
        commitList.add(initialCommit.getHashId());


        branches = new TreeMap<String, String>();
        headBranch = "master";
        branches.put(headBranch, initialCommit.getHashId());
        headCommit = branches.get(headBranch);

        stage = new Staging();
        save();
    }

    /**
     * The command add().
     * Adds a copy of the file as it currently exists to the staging area.
     *
     * @param fileName The name of the file being added.
     */
    public void add(String fileName) {
        File addFile = new File(fileName);


        if (addFile.exists()) {


            Blobs file = new Blobs(fileName, cwd);
            String fileHash = file.getHash();

            File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
            Commits parentCommit = Utils.readObject
                    (pathToParentCommit, Commits.class);

            if (!parentCommit.getBlob().isEmpty()
                    && parentCommit.getBlob().get(fileName) != null
                    && parentCommit.getBlob().get(fileName).equals(fileHash)) {

                if (stage.getTracked().containsKey(fileName)) {
                    stage.getTracked().remove(fileName);
                }
                if (stage.getRemoved().containsKey(fileName)) {
                    stage.getRemoved().remove(fileName);
                }

                save();

            } else {
                if (stage.getRemoved().containsKey(fileName)) {
                    stage.getRemoved().remove(fileName);
                }


                File pathToBlob = Utils.join(cwd, ".gitlet/" + fileHash);
                Utils.writeContents(pathToBlob, file.getContent());

                stage.addFile(fileName, file.getHash());

                save();
            }
        } else {
            System.out.println("File does not exist.");
            System.exit(0);
        }
    }

    /**
     * The command commit.
     * Saves a snapshot of tracked files in the current commit and staging area
     * so they can be restored at a later time, creating a new commit.
     * The staging area should be clear after a commit.
     *
     * @param message The commit message.
     */
    public void commit(String message) {
        if (stage.getTracked().isEmpty() && stage.getRemoved().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        if (message.isEmpty()) {
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }


        File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit = Utils.
                readObject(pathToParentCommit, Commits.class);

        HashMap<String, String> currentBlobs = new HashMap<>();
        if (parentCommit.getBlob() != null) {
            currentBlobs.putAll(parentCommit.getBlob());

        }
        currentBlobs.putAll(stage.getTracked());
        for (String fileToRemove : stage.getRemoved().keySet()) {
            currentBlobs.remove(fileToRemove);
        }
        Commits newCommit = new
                Commits(message, parentCommit.getHashId(), currentBlobs);
        File pathToCommit = Utils.join(cwd, ".gitlet/" + newCommit.getHashId());
        Utils.writeObject(pathToCommit, newCommit);

        branches.put(headBranch, newCommit.getHashId());
        headCommit = branches.get(headBranch);
        commitList.add(newCommit.getHashId());

        stage.getTracked().clear();
        stage.getRemoved().clear();
        stage.getUntracked().clear();
        stage.getModified().clear();

        save();
    }

    /**
     * The command checkout.
     *
     * @param args The various arguments associated with checkout.
     */
    public void checkout(String[] args) {
        if (args.length != 3 && args.length != 2 && args.length != 4) {
            System.out.println("Incorrect Operands");
            System.exit(0);
        } else if ((args.length == 3 && !args[1].equals("--"))
                || (args.length == 4 && !args[2].equals("--"))) {
            System.out.println("Incorrect Operands");
            System.exit(0);
        } else {
            if (args.length == 3) {
                String file = args[2];
                File pathToParentCommit = Utils.join
                        (cwd, ".gitlet/" + headCommit);
                Commits parentCommit =
                        Utils.readObject(pathToParentCommit, Commits.class);
                if (!parentCommit.getBlob().isEmpty()
                        && !parentCommit.getBlob().containsKey(file)) {
                    System.out.println("File does not exist in that commit.");
                    System.exit(0);
                }
                File pathToCommitFile = Utils.join(cwd,
                        ".gitlet/" + parentCommit.getBlob().get(file));
                File replaceFile = Utils.join(cwd, file);
                Utils.writeContents(replaceFile,
                        Utils.readContentsAsString(pathToCommitFile));
                save();
            } else if (args.length == 4) {
                String commitId = args[1];
                String file = args[3];
                int count = 0;
                for (String s : commitList) {
                    if (s.startsWith(commitId)) {
                        commitId = s;
                        count = count + 1;
                    }
                }
                if (count == 0) {
                    System.out.println("No commit with that id exists.");
                    System.exit(0);
                }
                File pathToRefCommit = Utils.join(cwd, ".gitlet/" + commitId);
                Commits refCommit = Utils.
                        readObject(pathToRefCommit, Commits.class);
                if (!refCommit.getBlob().containsKey(file)) {
                    System.out.println("File does not exist in that commit.");
                    System.exit(0);
                }
                File pathToCommitFile = Utils.join(cwd,
                        ".gitlet/" + refCommit.getBlob().get(file));
                File replaceFile = Utils.join(cwd, file);
                Utils.writeContents(replaceFile,
                        Utils.readContentsAsString(pathToCommitFile));
                save();
            } else {
                String branchName = args[1];
                checkoutBranch(branchName);
            }
        }
    }

    /**
     * Helper for checkout-branch.
     *
     * @param branchName The name of the branch
     */
    public void checkoutBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (branchName.equals(headBranch)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }
        File pathToBranchCommit = Utils.join
                (cwd, ".gitlet/" + branches.get(branchName));
        Commits branchCommit = Utils.
                readObject(pathToBranchCommit, Commits.class);
        File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);
        List<String> allFiles = Utils.plainFilenamesIn(cwd);
        for (String file : allFiles) {
            if (branchCommit.getBlob().containsKey(file)
                    && !parentCommit.getBlob().containsKey(file)
                    && !stage.getTracked().containsKey(file)) {
                System.out.println("There is an untracked "
                        + "file in the way;"
                        + " delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String fileName : parentCommit.getBlob().keySet()) {
            if (!branchCommit.getBlob().containsKey(fileName)) {
                Utils.restrictedDelete(fileName);
            }
        }
        for (String fileName : branchCommit.getBlob().keySet()) {
            File pathToFile = Utils.join(cwd,
                    ".gitlet/" + branchCommit.getBlob().get(fileName));
            Utils.writeContents(Utils.join(cwd, fileName),
                    Utils.readContentsAsString(pathToFile));
        }
        headBranch = branchName;
        headCommit = branches.get(branchName);
        save();
    }

    /**
     * The command log().
     * Starting at the current head commit, display information
     * about each commit backwards along the commit tree until
     * the initial commit, following the first parent commit links,
     * ignoring any second parents found in merge commits.
     */
    public void log() {
        File pathToParentCommit =
                Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);
        while (parentCommit != null) {
            System.out.println("===");
            System.out.println("commit " + parentCommit.getHashId());
            if (parentCommit.isMerged()) {
                System.out.println("Merge: "
                        + parentCommit.getParent1Id().substring(0, 7)
                        + " "
                        + parentCommit.getParent2Id().substring(0, 7));
            }
            System.out.println("Date: " + parentCommit.getTime());
            System.out.println(parentCommit.getMessage());
            System.out.println();
            if (parentCommit.getParentId() != null) {
                pathToParentCommit = Utils.join
                        (cwd, ".gitlet/" + parentCommit.getParentId());
                parentCommit = Utils.readObject
                        (pathToParentCommit, Commits.class);
            } else {
                return;
            }
        }
    }

    /**
     * The command global log().
     * Like log, except displays information about all commits ever made.
     */
    public void globalLog() {
        for (String commit : commitList) {
            File commitFile = Utils.join(cwd, ".gitlet/" + commit);
            Commits theCommit = Utils.readObject(commitFile, Commits.class);
            System.out.println("===");
            System.out.println("commit " + theCommit.getHashId());
            System.out.println("Date: " + theCommit.getTime());
            System.out.println(theCommit.getMessage());
            System.out.println();
        }
    }

    /**
     * The command rm.
     * Unstage the file if it is currently staged for addition.
     * If the file is tracked in the current commit,
     * stage it for removal and remove the file from the
     * working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     *
     * @param fileName the name of the file
     */
    public void rm(String fileName) {
        File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);
        if (stage.getTracked().containsKey(fileName)) {
            stage.getTracked().remove(fileName);
            save();
        } else if (parentCommit.getBlob().containsKey(fileName)) {
            stage.addRemovedFile(fileName, "abc");

            if (stage.getTracked().containsKey(fileName)) {
                stage.getTracked().remove(fileName);
            }

            if (stage.getUntracked().containsKey(fileName)) {
                stage.getUntracked().remove(fileName);
            }
            File fileToRemove = Utils.join(cwd, fileName);
            if (fileToRemove.exists()) {
                Utils.restrictedDelete(fileToRemove);
            }
            save();
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }

    }

    /**
     * The command rm-branch(String branchName).
     * Deletes the branch with the given name.
     * This only means to delete the pointer associated with the branch;
     * it does not mean to delete all commits that were
     * created under the branch, or anything like that.
     *
     * @param branchName the name of the branch
     */
    public void rmBranch(String branchName) {
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(headBranch)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        branches.remove(branchName);
        save();
    }

    /**
     * The command status().
     * Displays what branches currently exist,
     * and marks the current branch with a *.
     * Also displays what files have been staged for addition or removal.
     */
    public void status() {
        System.out.println("=== Branches ===");
        for (String branchesName : branches.keySet()) {
            if (branchesName.equals(headBranch)) {
                System.out.println("*" + branchesName);
            } else {
                System.out.println(branchesName);
            }
        }
        System.out.println();

        System.out.println("=== Staged Files ===");
        for (String fileNames : stage.getTracked().keySet()) {
            System.out.println(fileNames);
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        for (String fileNames : stage.getRemoved().keySet()) {
            System.out.println(fileNames);
        }
        System.out.println();

        modifiedHelper();
        System.out.println("=== Modifications Not Staged For Commit ===");
        for (String fileNames : stage.getModified().keySet()) {
            System.out.println(fileNames
                    + " (" + stage.getModified().get(fileNames) + ")");
        }
        System.out.println();

        untrackedHelper();
        System.out.println("=== Untracked Files ===");
        for (String fileNames : stage.getUntracked().keySet()) {
            System.out.println(fileNames);
        }
        System.out.println();
    }

    /**
     * Helper function to find the modified but not staged commit files.
     */
    public void modifiedHelper() {
        List<String> allFiles = Utils.plainFilenamesIn(cwd);
        File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);
        for (String file : allFiles) {
            Blobs newFile = new Blobs(file, cwd);
            if (parentCommit.getBlob().containsKey(file)
                    && (!newFile.getHash().equals
                    (parentCommit.getBlob().get(file)))
                    && !stage.getTracked().containsKey(file)
                    && !newFile.getContent().contains("HEAD")) {
                stage.addModified(file, "modified");
            }
        }
        for (String modifiedFiles : stage.getModified().keySet()) {
            File checkFile = Utils.join(cwd, modifiedFiles);
            if (!checkFile.exists()) {
                stage.addModified(modifiedFiles, "deleted");
            }
        }
        save();
    }

    /**
     * Helper function to find the untracked files.
     */
    public void untrackedHelper() {
        List<String> allFiles = Utils.plainFilenamesIn(cwd);
        File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);
        for (String file : allFiles) {
            if (!parentCommit.getBlob().containsKey(file)
                    && !stage.getTracked().containsKey(file)) {
                Blobs untrackedFile = new Blobs(file, cwd);
                stage.addUntracked(file, untrackedFile.getHash());
            }
        }
        save();
    }

    /**
     * The command find(String commitMessage).
     * Prints out the ids of all commits that have the given commit message
     * one per line. If there are multiple such commits,
     * it prints the ids out on separate lines.
     *
     * @param commitMessage the commit message
     */
    public void find(String commitMessage) {
        boolean found = false;
        for (String commitIds : commitList) {
            File pathToCommit = Utils.join(cwd, ".gitlet/" + commitIds);
            Commits theCommit =
                    Utils.readObject(pathToCommit, Commits.class);
            if (theCommit.getMessage().equals(commitMessage)) {
                found = true;
                System.out.println(commitIds);
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
        save();
    }

    /**
     * The command branch(String branchName)
     * Creates a new branch with the given name,
     * and points it at the current head node.
     *
     * @param branchName the name of the branch
     */
    public void branch(String branchName) {
        if (branches.containsKey(branchName)) {
            System.out.println("A branch with that name already exists");
            System.exit(0);
        } else {
            branches.put(branchName, headCommit);
            intersection.put(branchName, headCommit);
            save();
        }
    }

    /**
     * The command reset(String commitId).
     * Checks out all the files tracked by the given commit.
     * Removes tracked files that are not present in that commit.
     * Also moves the current branch's head to that commit node.
     *
     * @param commitId The commitId
     */
    public void reset(String commitId) {

        File pathToParentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);
        if (!commitList.contains(commitId)) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        File pathToRefCommit = Utils.join(cwd, ".gitlet/" + commitId);
        Commits refCommit = Utils.
                readObject(pathToRefCommit, Commits.class);

        List<String> allFiles = Utils.plainFilenamesIn(cwd);
        for (String file : allFiles) {
            if (!parentCommit.getBlob().containsKey(file)
                    && !stage.getTracked().containsKey(file)
                    && refCommit.getBlob().containsKey(file)) {
                System.out.println("There is an untracked "
                        + "file in the way; "
                        + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }

        for (String file : refCommit.getBlob().keySet()) {
            File pathToCommitFile = Utils.join(cwd,
                    ".gitlet/" + refCommit.getBlob().get(file));
            File replaceFile = Utils.join(cwd, file);
            Utils.writeContents(replaceFile,
                    Utils.readContentsAsString(pathToCommitFile));
        }

        for (String file : parentCommit.getBlob().keySet()) {
            if (!refCommit.getBlob().containsKey(file)) {
                Utils.restrictedDelete(file);
            }
        }
        headCommit = commitId;
        branches.put(headBranch, headCommit);
        stage.getTracked().clear();
        save();
    }

    /**
     * The command merge(String branchName).
     * Merges files from the given branch into the current branch.
     * Any files that have been modified in the
     * given branch since the split point,
     * but not modified in the current branch since the split point
     * should be changed to their versions in the given branch
     * (checked out from the commit at the front of the given branch).
     * These files should then all be automatically staged.
     *
     * @param branchName The name of the given branch.
     */
    public void merge(String branchName) {
        mergeFailures(branchName);
        boolean conflict = false;
        File pathToCurrentCommit = Utils.join(cwd, ".gitlet/" + headCommit);
        Commits currentCommit =
                Utils.readObject(pathToCurrentCommit, Commits.class);

        File pathToBranchCommit = Utils.join
                (cwd, ".gitlet/" + branches.get(branchName));
        Commits branchCommit = Utils.
                readObject(pathToBranchCommit, Commits.class);
        String splitPoint = splitPointHelper(currentCommit, branchCommit);
        File pathToSplitPoint = Utils.join(cwd, ".gitlet/" + splitPoint);
        Commits splitPointCommit = Utils.
                readObject(pathToSplitPoint, Commits.class);
        if (splitPointCommit.getHashId().equals(branchCommit.getHashId())) {
            System.out.println("Given branch "
                    + "is an ancestor of the current branch.");
            System.exit(0);
        }
        if (splitPointCommit.getHashId().equals(currentCommit.getHashId())) {
            for (String fileName : currentCommit.getBlob().keySet()) {
                if (!branchCommit.getBlob().containsKey(fileName)) {
                    Utils.restrictedDelete(fileName);
                }
            }
            for (String fileName : branchCommit.getBlob().keySet()) {
                File pathToFile = Utils.join(cwd,
                        ".gitlet/" + branchCommit.getBlob().get(fileName));
                Utils.writeContents(Utils.join(cwd, fileName),
                        Utils.readContentsAsString(pathToFile));
            }
            headBranch = branchName;
            headCommit = branches.get(branchName);
            save();
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        conflict = mergeWork(currentCommit, branchCommit, splitPointCommit);

        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
        mergeCommit(currentCommit, branchCommit, branchName);
    }

    /**
     * The function that does the merge commit.
     *
     * @param currentCommit the current commit
     * @param branchCommit  the given branch commit.
     * @param branchName    the name of the given branch.
     */
    public void mergeCommit(Commits currentCommit,
                            Commits branchCommit, String branchName) {
        HashMap<String, String> currentBlobs = new HashMap<>();
        if (currentCommit.getBlob() != null) {
            currentBlobs.putAll(currentCommit.getBlob());
        }
        currentBlobs.putAll(stage.getTracked());
        for (String fileToRemove : stage.getRemoved().keySet()) {
            currentBlobs.remove(fileToRemove);
        }
        Commits newCommit = new
                Commits("Merged " + branchName + " into "
                + headBranch + ".", currentCommit.getHashId(),
                branchCommit.getHashId(), currentBlobs);
        File pathToCommit = Utils.join(cwd,
                ".gitlet/" + newCommit.getHashId());
        Utils.writeObject(pathToCommit, newCommit);
        branches.put(headBranch, newCommit.getHashId());
        headCommit = branches.get(headBranch);
        commitList.add(newCommit.getHashId());
        stage.getTracked().clear();
        stage.getRemoved().clear();
        stage.getUntracked().clear();
        stage.getModified().clear();
        save();
    }

    /**
     * The function that has the work in merge.
     *
     * @param currentCommit    the current commit
     * @param branchCommit     the given branch commit
     * @param splitPointCommit the splitpoint commit
     * @return the boolean value of
     * whether there is a merge conflict
     */
    public boolean mergeWork(Commits currentCommit,
                             Commits branchCommit, Commits splitPointCommit) {
        boolean conflict = false;
        for (String fileName : currentCommit.getBlob().keySet()) {
            if (splitPointCommit.getBlob().containsKey(fileName)
                    && branchCommit.getBlob().containsKey(fileName)) {
                if ((!branchCommit.getBlob().get(fileName).
                        equals(splitPointCommit.getBlob().get(fileName)))
                        && currentCommit.getBlob().get(fileName).
                        equals(splitPointCommit.getBlob().get(fileName))) {
                    checkoutMerge(branchCommit, fileName);
                }
                if (!splitPointCommit.getBlob().get(fileName).
                        equals(currentCommit.getBlob().get(fileName))
                        && !splitPointCommit.getBlob().get(fileName).
                        equals(branchCommit.getBlob().get(fileName))
                        && !branchCommit.getBlob().get(fileName).
                        equals(currentCommit.getBlob().get(fileName))) {
                    mergeHelper(fileName, currentCommit, branchCommit);
                    conflict = true;
                }
            } else if (!branchCommit.getBlob().containsKey(fileName)
                    && splitPointCommit.getBlob().containsKey(fileName)
                    && !splitPointCommit.getBlob().get(fileName).
                    equals(currentCommit.getBlob().get(fileName))) {
                mergeHelper(fileName, currentCommit, branchCommit);
                conflict = true;
            }
        }
        for (String fileName : branchCommit.getBlob().keySet()) {
            if (!splitPointCommit.getBlob().containsKey(fileName)
                    && !currentCommit.getBlob().containsKey(fileName)) {
                checkoutMerge(branchCommit, fileName);
            }
            if (!splitPointCommit.getBlob().containsKey(fileName)
                    && currentCommit.getBlob().containsKey(fileName)
                    && !currentCommit.getBlob().get(fileName).
                    equals(branchCommit.getBlob().get(fileName))) {
                mergeHelper(fileName, currentCommit, branchCommit);
                conflict = true;
            }
            if (!currentCommit.getBlob().containsKey(fileName)
                    && splitPointCommit.getBlob().containsKey(fileName)
                    && !splitPointCommit.getBlob().get(fileName).
                    equals(branchCommit.getBlob().get(fileName))) {
                mergeHelper(fileName, currentCommit, branchCommit);
                conflict = true;
            }
        }
        for (String fileName : splitPointCommit.getBlob().keySet()) {
            if (!branchCommit.getBlob().containsKey(fileName)
                    && splitPointCommit.getBlob().get(fileName).
                    equals(currentCommit.getBlob().get(fileName))) {
                rm(fileName);
            }
        }
        return conflict;
    }

    /**
     * The method that does checkout in merge.
     *
     * @param branchCommit the branch commit
     * @param fileName     the name of the file being checked out
     */
    public void checkoutMerge(Commits branchCommit, String fileName) {
        String[] checkoutArray = new String[4];
        checkoutArray[0] = "checkout";
        checkoutArray[1] = branchCommit.getHashId();
        checkoutArray[2] = "--";
        checkoutArray[3] = fileName;
        checkout(checkoutArray);
        Blobs file = new Blobs(fileName, cwd);
        stage.addFile(fileName, file.getHash());
    }

    /**
     * The helper function for merge.
     *
     * @param fileName      the name of the file
     * @param currentCommit the currentCommit
     * @param branchCommit  the branchCommit
     */
    public void mergeHelper(String fileName, Commits currentCommit,
                            Commits branchCommit) {
        String currentFileContent = "";
        String givenFileContent = "";
        String top = "<<<<<<< HEAD\n";
        File currentFile = Utils.join(cwd,
                ".gitlet/" + currentCommit.getBlob().get(fileName));
        if (currentFile.exists()) {
            currentFileContent = Utils.readContentsAsString(currentFile);
        }
        String middle = "=======\n";
        File givenFile = Utils.join(cwd,
                ".gitlet/" + branchCommit.getBlob().get(fileName));
        if (givenFile.exists()) {
            givenFileContent = Utils.readContentsAsString(givenFile);
        }
        String end = ">>>>>>>\n";
        String result = top + currentFileContent
                + middle + givenFileContent + end;
        File toFile = Utils.join(cwd, fileName);
        Utils.writeContents(toFile, result);
        Blobs file = new Blobs(fileName, cwd);
        stage.addFile(fileName, file.getHash());
    }

    /**
     * The conditions for failure cases of merge.
     *
     * @param branchName The name of the given branch.
     */
    public void mergeFailures(String branchName) {
        if (!stage.getTracked().isEmpty()
                || !stage.getRemoved().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
        if (!branches.containsKey(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branchName.equals(headBranch)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        File pathToParentCommit = Utils.join(cwd,
                ".gitlet/" + headCommit);
        Commits parentCommit =
                Utils.readObject(pathToParentCommit, Commits.class);

        File pathToBranchCommit = Utils.join(cwd,
                ".gitlet/" + branches.get(branchName));
        Commits branchCommit = Utils.
                readObject(pathToBranchCommit, Commits.class);

        List<String> allFiles = Utils.plainFilenamesIn(cwd);
        for (String file : allFiles) {
            if (!parentCommit.getBlob().
                    containsKey(file)
                    && !stage.getTracked().containsKey(file)
                    && branchCommit.getBlob().containsKey(file)) {
                System.out.println("There is an untracked "
                        + "file in the way; delete it,"
                        + " or add and commit it first.");
                System.exit(0);
            }
        }
    }

    /**
     * Splitpoint helper.
     *
     * @param currentCommit the current commit
     * @param branchCommit  the branch commit
     * @return return the hashId of the splitPoint.
     */
    public String splitPointHelper
    (Commits currentCommit, Commits branchCommit) {
        ArrayDeque<Commits> commitsArrayDeque = new ArrayDeque<>();
        HashSet<String> setOfAllCommits = new HashSet<>();
        String splitPoint = "";
        commitsArrayDeque.add(branchCommit);
        while (!commitsArrayDeque.isEmpty()) {
            ArrayList<String> parentList = new ArrayList<>();
            Commits n = commitsArrayDeque.remove();
            if (n != null) {
                setOfAllCommits.add(n.getHashId());
            }
            if (branchCommit.isMerged()
                    && branchCommit.getParentId() != null) {
                parentList.add(branchCommit.getParentId());
                parentList.add(branchCommit.getParent2Id());
            } else if (!branchCommit.isMerged()
                    && branchCommit.getParentId() != null) {
                parentList.add(branchCommit.getParentId());
            }
            while (parentList.size() != 0) {
                File pathToParent = Utils.join(cwd,
                        ".gitlet/" + parentList.get(0));
                Commits parent = Utils.
                        readObject(pathToParent, Commits.class);
                parentList.remove(0);
                commitsArrayDeque.add(parent);
                branchCommit = parent;
            }
        }
        ArrayDeque<Commits> bfsFromCurrentBranch = new ArrayDeque<>();
        bfsFromCurrentBranch.add(currentCommit);
        while (!bfsFromCurrentBranch.isEmpty()) {
            Commits n = bfsFromCurrentBranch.remove();
            if (setOfAllCommits.contains(n.getHashId())) {
                splitPoint = n.getHashId();
                return splitPoint;
            } else {
                ArrayList<String> parentList = new ArrayList<>();
                if (currentCommit.isMerged()
                        && currentCommit.getParentId() != null) {
                    parentList.add(currentCommit.getParentId());
                    parentList.add(currentCommit.getParent2Id());
                } else if (!currentCommit.isMerged()
                        && currentCommit.getParentId() != null) {
                    parentList.add(currentCommit.getParentId());
                }
                while (parentList.size() != 0) {
                    File pathToParent = Utils.join(cwd,
                            ".gitlet/" + parentList.get(0));
                    Commits parent = Utils.
                            readObject(pathToParent, Commits.class);
                    parentList.remove(0);
                    bfsFromCurrentBranch.add(parent);
                    currentCommit = parent;
                }
            }
        }
        return splitPoint;
    }

    /**
     * The user's current working directory.
     */
    private String cwd;

    /**
     * A map representing the branches and their respective commits.
     */
    private TreeMap<String, String> branches;

    /**
     * A map representing where the intersection of the branches occur.
     */
    private HashMap<String, String> intersection = new HashMap<>();


    /**
     * The hash of the most recent commit.
     */
    private String headCommit;

    /**
     * The name of the head branch.
     */
    private String headBranch;

    /**
     * A map of files and their hash id that are tracked in the stage.
     */
    private Staging stage;

    /**
     * A list of all commits being made.
     */
    private ArrayList<String> commitList = new ArrayList<>();

}
