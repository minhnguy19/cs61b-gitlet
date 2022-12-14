# Gitlet Design Document
Author: Minh Nguyen 

## 1. Classes and Data Structures

### Commands

A class that stores the all the commands that are used in Gitlet. 

#### Fields
  1. TreeMap<String, String> branches: A map representing the branches and their respective commits. 
  2. String headCommit: the hash of the most recent commit file.
  3. String headName: the name of the head branch.
  4. String CWD: current working directory.

### Staging

A class that keeps track of files that should be tracked in the next commit, and should not be tracked in the next commmit. 

#### Fields

1.  HashMap<String, String> tracked: A map of files and their hash id that are tracked in the stage.
3.  HashMap<String, String> removed: A map of all files that are removed.

### Commits

A class that captures all the information of commits being made throughout time. It also keeps track of the branches, including the head branch.

#### Fields
  1. String message: the log mesasge given by the user.
  2. String time: the timestamp of the commit.
  3. String hashId: the hash id of the commit. 
  4. String parentId: the hash id of the parent commit. 
  5. HashMap<String, String> blob: the blobs referenced with the commit. 
  

### Blobs

This class create the Blob object, which has the name, content, and hash ID associated with the file. 

 #### Fields
  1. String name: the name of the Blob.
  2. String content: the content of the Blob.
  3. Sring hash: the hashID of the Blob.

## 2. Algorithms

### Commands Class
1. innit(): Creates a new Gitlet version-control system in the current directory that start with an initial commit. 
    * Make initial commit here.
    * The timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970.
    * Making a ".gitlet" directory here. Throw error if it already exists.
    * Create persistence for the initial commit.
    * Put the initial commit into the commit branch.
2. add(String filename): Adds a copy of the file as it currently exists to the staging area.
3. commit(String message): Saves a snapshot of tracked files in the current commit and staging area so they can be restored at a later time, creating a new commit. The staging area should be clear after a commit. 
4. rm(String file name): Unstage the file if it is currently staged for addition. If the file is tracked in the current commit, stage it for removal and remove the file from the working directory if the user has not already done so (do not remove it unless it is tracked in the current commit).
5. log(): Starting at the current head commit, display information about each commit backwards along the commit tree until the initial commit, following the first parent commit links, ignoring any second parents found in merge commits. 
6. global-log(): Like log, except displays information about all commits ever made.
7. find(String commitMessage): Prints out the ids of all commits that have the given commit message, one per line. If there are multiple such commits, it prints the ids out on separate lines. 
8. status(): Displays what branches currently exist, and marks the current branch with a *. Also displays what files have been staged for addition or removal. 
9. checkout(String[] args): 
    * If args: (String fileName): Takes the version of the file as it exists in the head commit, the front of the current branch, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
    * If args: (String commitId, String --, String fileName): Takes the version of the file as it exists in the commit with the given id, and puts it in the working directory, overwriting the version of the file that's already there if there is one. The new version of the file is not staged.
    * If args: (String branchName): Takes all files in the commit at the head of the given branch, and puts them in the working directory, overwriting the versions of the files that are already there if they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD). Any files that are tracked in the current branch but are not present in the checked-out branch are deleted. The staging area is cleared, unless the checked-out branch is the current branch. 
10. branch(String branchName): Creates a new branch with the given name, and points it at the current head node. 
11. rm-branch(String branchName): Deletes the branch with the given name. This only means to delete the pointer associated with the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
12. reset(String commitId): Checks out all the files tracked by the given commit. Removes tracked files that are not present in that commit. Also moves the current branch's head to that commit node. 
13. merge(String branchName): Merges files from the given branch into the current branch. Any files that have been modified in the given branch since the split point, but not modified in the current branch since the split point should be changed to their versions in the given branch (checked out from the commit at the front of the given branch). These files should then all be automatically staged. 

### Staging Class
1. getTracked(): Get the map of tracked files.
2. getRemoved(): Get the map of the removed files. 


### Blobs Class
1. getName(): Get the name of the file.
2. getContent(): Get the content of the file.
3. getHash(): Get the hashId of the file. 

### Commits Class
1. getTime(): Get the time of the commit. 
2. getParentId: Get the hash id of the parent. 
3. getMesasge: Get the message of the commit. 
4. getHashId: Get the hash id of the commit.
5. getBlob: Get the blob of the commit. 


## 3. Persistence

We save the state of the commits tree after each commit by serializing the commits using their hashId and saving them to files named after their hashId on disk. This can be done with writeObject method from the Utils class.
