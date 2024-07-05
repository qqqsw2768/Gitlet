package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Commit.*;
import static gitlet.Blob.*;
import static gitlet.StagingArea.*;
import static gitlet.Utils.*;

/** Represents a gitlet repository.
 *
 *  @author qqqsw
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));

    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The commits and blobs directory */
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");

    /** Store commits */
    public static final File COMMIT_DIR = join(OBJECT_DIR, "commits");

    /** Store commits */
    public static final File BLOB_DIR = join(OBJECT_DIR, "blobs");

    /** The file stores branch head */
    public static final File BRANCH_DIR = join(GITLET_DIR, "branchHeads");

    /** The entire file(not dir) stores HEAD pointer */
    public static final File HEAD = join(GITLET_DIR, "HEAD");

    /** The staging area for addition (File) */
    public static final File ADD = join(GITLET_DIR, "add");

    /** The staging area for removal */
    public static final File RM = join(GITLET_DIR, "rm");

    /** Store the current branch's name, just name, because HEAD doesn't store the cur name */
    public static final File CUR_BRANCH = join(GITLET_DIR, "CurBranch");

    /** the List that be used by checkIn */
    public static List<String> stagedFilesAdd = new ArrayList<>();
    public static List<String> stagedFilesRm = new ArrayList<>();
    public static List<String> modified = new ArrayList<>();
    public static List<String> deleted = new ArrayList<>(); //  The tracked files but be used the command `rm`
    public static List<String> restFiles = new ArrayList<>(); // Untracked files

    /** Creates a new Gitlet version-control system in the current directory.
     * init current cwd:
     * create dirs we all need
     * set new branch: master(default)/ HEAD
     * commit the initial log(empty)
     */
    public static void init() throws IOException {
        if (!createDirAndFile()) {
            System.exit(0);
        }

        String hash = Utils.sha1();
        String timestamp = dateFormat(new Date(0));

        List<String> parentList = new ArrayList<>();

        Commit firstCommit = new Commit("initial commit", hash, timestamp, parentList, new TreeMap<>());
        firstCommit.saveCommit();

        Commit.newBranch("master", firstCommit);
        setHEAD(firstCommit, "master");
    }

    /**
     * Create files and dirs the system needed
     */
    private static boolean createDirAndFile() throws IOException {

        // if files not exists it will return: "A Gitlet balabala..."
        if (!GITLET_DIR.exists()) {
            GITLET_DIR.mkdirs();
            COMMIT_DIR.mkdirs();
            BLOB_DIR.mkdirs();
            BRANCH_DIR.mkdirs();
            HEAD.createNewFile();
            ADD.createNewFile();
            RM.createNewFile();
        } else {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return false;
        }

        return true;
    }

    public static void ifInitialize() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    /**
     * The command: add
     * Add file to stagingArea
     * @param fileName
     */
    public static void add(String fileName) throws IOException {
        File fileToAdd = new File(fileName);

        if (deleteBlobInRm(fileName)) {
            getHEAD().makeFileFromBlob(fileName);
            return;
        }

        /** if the file not exits, escape */
        if (!fileToAdd.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }

        // make new blob
        Blob blob = new Blob(fileToAdd);
        StagingArea oldSa = getStageFromFile("add");
        checkInCommit(blob);

        if (oldSa == null) { //todo optimize the way store stagingArea
            StagingArea stagingArea = new StagingArea(blob);
            writeObject(ADD, stagingArea);
        } else {
            oldSa.addStageAdd(blob);
            writeObject(ADD, oldSa);
        }

        blob.saveBlob();
    }


    /**
     * The command: commit
     * Get stagingArea info form deserializing the dir : ADD/RM .if there is nothing: abort
     * load everything in commit object but hashId, then compute it
     */
    public static void commit(String msg) throws IOException {
        StagingArea addtionStage = StagingArea.getStageFromFile("add");
        StagingArea removalStage = StagingArea.getStageFromFile("rm");

        if ((addtionStage == null || addtionStage.isStageEmpty()) &&
                (removalStage == null || removalStage.isStageEmpty())) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        String timestamp = dateFormat(new Date());
        Commit curCommit = getHEAD();

        TreeMap<String, String> nameToBlobMap = curCommit.getNameToBlob();// get old commit's map

        // remove mapping in the removal stage
        if (removalStage != null) {
            TreeMap<String, String> removal = removalStage.getFileToBlobMap();
            for (String key : removal.keySet()) {
                nameToBlobMap.remove(key);
            }
        }

        // add mapping in the addition stage
        if (addtionStage != null) {
            TreeMap<String,String> addition = addtionStage.getFileToBlobMap();
            for(String i : addition.keySet()) {
                nameToBlobMap.put(i, addition.get(i));
            }
        }

        // update parent list
        List<String> parentList = new ArrayList<>();
        parentList.add(getHEAD().getHashId());

        // hash everything except the hashId itself
        String hashId = Utils.sha1(msg, timestamp, nameToBlobMap.toString(), parentList.toString());
        Commit commit = new Commit(msg, hashId, timestamp, parentList, nameToBlobMap);

        // update HEAD & curBranch's active pointer
        setHEAD(commit, getCurBranchName());
        updateBranch(commit);

        // persist
        commit.saveCommit();

        //clear add rm
        clearStage();
    }

    public static void mergeCommit() {

    }
    /**
     * The command: rm
     *
     * @param fileName the fileName need to remove
     */
    public static void rm(String fileName) {
        int flag = 0;
        File fileCWD = new File(fileName);
        Commit commit = getHEAD();

        //  Check if there is key in Addition stage/ CURRENT COMMIT that need to be removed from rm
        if (deleteBlobInADD(fileName)) { // delete the blob in addition Stage
            flag = 1;
            return;
        }

        if (commit.containsFile(fileName)) { // if file is tracked by current commit, then add it to rmStage & delete it
            String hashId = commit.getValueInMap(fileName);
            StagingArea rmStage = getStageFromFile("rm");
            if (rmStage == null) {
                newStageRm(hashId);
            } else {
                rmStage.addStageRm(hashId);
                writeObject(RM, rmStage);
            }

            if (fileCWD.exists()) {
                Utils.restrictedDelete(fileCWD);
            }
        }
        else if (flag == 0){
            System.out.println("No reason to remove the file.");
        }
    }

    /**
     * The command: log
     * Iterate the commit from HEAD until initial commit
     */
    public static void log() {
        Commit curCommit = getHEAD();
        String initial = Utils.sha1();
        while (!curCommit.getHashId().equals(initial)) {
            curCommit.printCommit();
            curCommit = curCommit.getFirstParent();
        }
        curCommit.printCommit();
    }

    /**
     * The command: global-log
     * Print all commits ever made without order
     */
    public static void globalLog() {
        List<String> allCommit = Utils.plainFilenamesIn(COMMIT_DIR);
        for(String i: allCommit) {
            getCommitByHashId(i).printCommit();
        }
    }

    /**
     * The command: find
     * Find the commits that including :msg
     */
    public static void find(String msg) {
        List<String> allCommit = Utils.plainFilenamesIn(COMMIT_DIR);
        int findAtleast1 = 0;
        for(String i: allCommit) {
            Commit curCommit = getCommitByHashId(i);
            if(curCommit.getMessage().contains(msg)) {
                System.out.println(curCommit.getHashId());
                findAtleast1++;
            }
        }

        if (findAtleast1 == 0) {
            System.out.println("Found no commit with that message.");
        }
    }

    /**
     * The command: branch
     * Make a new branch
     * NOT immediately switch to the newly created branch
     * @param branchName
     */
    public static void branch(String branchName) {
        newBranch(branchName, getHEAD());
    }

    /**
     * The command: checkout
     * Usage: checkout -- [file name]
     * Takes the version of the file as it exists in the head commit and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * @param fileName
     */
    public static void checkout(String fileName) throws IOException {
        Commit curCommit = getHEAD();
        curCommit.makeFileFromBlob(fileName);
        deleteBlobInADD(fileName);
    }

    /**
     * The command: checkout
     * Usage: checkout [commit id] -- [file name]
     * Takes the version of the file as it exists in the commit with the given id,
     * and puts it in the working directory,
     * overwriting the version of the file that’s already there if there is one.
     * The new version of the file is not staged.
     * @param commitId
     * @param fileName
     */
    public static void checkout(String commitId, String fileName) throws IOException {
        Commit commit = getCommitByHashId(commitId);
        commit.makeFileFromBlob(fileName);
        deleteBlobInADD(fileName);
    }

    /**
     * The command: checkout
     * Usage: checkout [branch name]
     * @param branchName
     */
    public static void checkoutBranch(String branchName) throws IOException {
        if (branchName.equals(getCurBranchName())) {
            System.out.println("No need to checkout the current branch.");
        }

        Commit checkout = getBranchByName(branchName);
        switchCwd1To2(getHEAD(), checkout, branchName);
    }

    /**
     * Set CWD from `first` commit to `second` commit
     * Set `second` commit as HEAD
     * @param first delete the files in "first"
     * @param second create the files in "second"
     * @param branchName in this branch set the HEAD pointer
     * @throws IOException
     */
    private static void switchCwd1To2(Commit first, Commit second, String branchName) throws IOException {
        checkStatus();
        if (!restFiles.isEmpty() || !stagedFilesAdd.isEmpty() || !modified.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        deleteAllFilesFrom(first);
        createAllFilesFrom(second);

        setHEAD(second, branchName);
    }


    /**
     * The command: status
     */
    public static void status() {
        checkStatus();
        printStatus();
    }

    /**
     * Check if the 5 lists are empty, and if they are not, it means that
     * the type of special files corresponding to the list name exists.
     * eg: stagedFilesAdd list
     */
    public static void checkStatus() {
        restFiles = new ArrayList<>(Utils.plainFilenamesIn(CWD));
        List<String> rm = null;

        getRmList();
        checkIn("add"); // must behind printRmList, because need the Rm list in it
        checkIn("commit");
    }

    /**
     * Print the status
     */
    private static void printStatus() {
        printBranchName();
        System.out.println("=== Staged Files ===");
        printList(stagedFilesAdd);

        System.out.println("=== Removed Files ===");
        printList(stagedFilesRm);

        System.out.println("=== Modifications Not Staged For Commit ===");
        printList(deleted, "deleted");
        printList(modified, "modified");

        System.out.println("=== Untracked Files ===");
        printList(restFiles);
    }

    private static void getRmList() {
        StagingArea stagingArea = getStageFromFile("rm");
        if (stagingArea != null) {
            TreeMap<String, String> treeMap = stagingArea.getFileToBlobMap();
            for (String i : treeMap.keySet()) {
                stagedFilesRm.add(i);
            }
        }
    }

    private static void printList(List<String> list) {
        if (list == null) {
            System.out.println();
            return;
        }
        for (String i : list) {
            System.out.println(i);
        }
        System.out.println();
    }

    private static void printList(List<String> list, String flag) {
        if (list == null) {
            System.out.println();
            return;
        }
        for (String i : list) {
            System.out.println(i + " (" + flag + ")");
        }
        if (!flag.equals("deleted")) {
            System.out.println();
        }
    }

    /**
     * traverse the commit treemap or addStaging treemap to check modified files
     * & deleted files
     *
     * @param addOrCommit
     */
    public static void checkIn(String addOrCommit) {
        TreeMap<String, String> treeMap;
        StagingArea stagingArea = getStageFromFile("add");

        if (addOrCommit.equals("add") && stagingArea != null) {
            treeMap = stagingArea.getFileToBlobMap();
        } else if (addOrCommit.equals("add") && stagingArea == null) {
            return;
        } else {
            treeMap = getHEAD().getNameToBlob();
        }

        Iterator<String> iterator = treeMap.keySet().iterator();
        while (iterator.hasNext()) {
            String i = iterator.next();
            if (restFiles.contains(i)) { // the file exists in CWD
                File file = new File(i);
                Blob blob = new Blob(file);
                String newHashId = blob.getHashId();
                String oldHashId = treeMap.get(i);

                if (newHashId.equals(oldHashId)) { // not modified the added file in CWD
                    if (addOrCommit.equals("add")) {
                        stagedFilesAdd.add(i); // in addStaging not modified
                    }
                } else { // the file is modified
                    if (modified == null || !modified.contains(i)) {
                        modified.add(i);
                    }
                }
            } else { // the file not exists in CWD
                if (!stagedFilesRm.contains(i)) {
                    deleted.add(i); // Determine if it is in rmStage; if so, do not add it to the deleted list
                }
            }
            restFiles.remove(i); // Only leave the untracked files
        }

        sortList(stagedFilesAdd);
        sortList(modified);
        sortList(deleted);
        sortList(restFiles);
    }

    /**
     * Sort the list
     * @param list
     */
    private static void sortList(List<String> list) {
        if (list != null) {
            Collections.sort(list);
        }
    }

    /**
     * Usage: java gitlet.Main rm-branch [branch name]
     * Delete the branch, not delete any commit, just the pointer of branch
     * @param branchName the branch that need to be deleted
     */
    public static void rmBranch(String branchName) {
        deleteBranchByName(branchName);
    }

    /**
     * Usage: java gitlet.Main reset [commit id]
     * @param commitId
     */
    public static void reset(String commitId) throws IOException {
        Commit changeTo = getCommitByHashId(commitId);
        switchCwd1To2(getHEAD(), changeTo, getCurBranchName());
        updateBranch(changeTo);
    }

    /**
     * merge!!
     * Merge every files exits in 3 commits: HEAD, Other Branch's Active Head, Split Point
     * @param branchName
     */
    public static void merge(String branchName) throws IOException {
        boolean conflictFlag = false;
        if (getCurBranchName() == branchName) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        checkStatus();
        if (!stagedFilesAdd.isEmpty() || !stagedFilesRm.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        if (!restFiles.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        Commit splitPoint = getSplitPoint(branchName);
        Commit branchHead = getBranchByName(branchName);

        TreeMap<String, String> allFiles = new TreeMap<>();

        TreeMap<String, String> splitMap = splitPoint.getNameToBlob();
        TreeMap<String, String> otherBranchMap = branchHead.getNameToBlob();
        TreeMap<String, String> headMap = getHEAD().getNameToBlob();

        if (splitMap.equals(otherBranchMap)) {
            message("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (getHEAD().equals(splitPoint)) { // fast-forwarded
            String master = getCurBranchName();
            checkoutBranch(branchName);
            setHEAD(branchHead, master);
            updateBranch(branchHead);
            message("Current branch fast-forwarded.");
        }

        allFiles.putAll(splitMap);
        allFiles.putAll(otherBranchMap);
        allFiles.putAll(headMap);

        TreeMap<String, String> newMap = new TreeMap<>();

        for (String fileName : allFiles.keySet()) {
            boolean inSplit = splitMap.containsKey(fileName);
            boolean inHead = headMap.containsKey(fileName);
            boolean inOther = otherBranchMap.containsKey(fileName);

            boolean modifiedOther = isModified(fileName, splitMap, otherBranchMap);
            boolean modifiedHead = isModified(fileName, splitMap, headMap);

            if (inSplit) {
                if (inHead && inOther && !modifiedHead && modifiedOther) { // split: A; HEAD:A; other:!A -- !A
                    newMap.put(fileName, otherBranchMap.get(fileName));
//                    addInMerge(headMap.get(fileName));
                    continue;
                } else if (inHead && inOther && modifiedHead && !modifiedOther) { // split: B; HEAD:!B; other:B -- !B
                    newMap.put(fileName, headMap.get(fileName));
                    continue;
                } else if (inHead && !inOther && !modifiedHead) { // split: D; HEAD:D; other:X -- X
                    rm(fileName);
                    continue;
                }
            } else {
                if (!inHead && inOther) { // split: X; HEAD:X; other:F -- F
                    newMap.put(fileName, otherBranchMap.get(fileName));
//                    addInMerge(otherBranchMap.get(fileName));
                    continue;
                } else if (inHead && !inOther) {
                    newMap.put(fileName, headMap.get(fileName));
                    continue;
                }
            }

            if (inHead && inOther && isModified(fileName, headMap, otherBranchMap) // Conflict
                    || inSplit && !inHead && inOther && modifiedOther
                    || inSplit && inHead && !inOther && modifiedHead) {
                String blobId = makeConflictFile(fileName, headMap, otherBranchMap);
                newMap.put(fileName, blobId);
                conflictFlag = true;
            } else if (modifiedHead && modifiedOther && !isModified(fileName, headMap, otherBranchMap)) { // modified in same way
                newMap.put(fileName, headMap.get(fileName));
            }
        }

        Commit mergeCommit = commitInMerge(branchName, newMap);
        deleteAllFilesFrom(getHEAD());
        createAllFilesFrom(mergeCommit);
        setHEAD(mergeCommit, getCurBranchName());
        updateBranch(mergeCommit);

        if (conflictFlag) {
            message("Encountered a merge conflict.");
        }
    }

    /**
     * Check the blob if is different from the split point
     * @return Is modified: ture; Not modified: false
     */
    public static boolean isModified(String fileName, TreeMap<String, String> split, TreeMap<String, String> curMap) {
        String blobIdInSplit = split.get(fileName);
        String blobIdInCurMap = curMap.get(fileName);
        if (blobIdInCurMap == null || blobIdInSplit == null) {
            return false;
        }
        if (getBlobContent(blobIdInSplit).equals(getBlobContent(blobIdInCurMap))) {
            return false;
        }
        return true;
    }

    /**
     * Add file to stagingArea using by merge command
     * @param
     */
    public static void addInMerge(String value) {
        StagingArea oldSa = getStageFromFile("add");
        Blob blob = getBlobByHashId(value);

        if (oldSa == null) {
            StagingArea stagingArea = new StagingArea(blob);
            writeObject(ADD, stagingArea);
        } else {
            oldSa.addStageAdd(blob);
            writeObject(ADD, oldSa);
        }

        blob.saveBlob();
    }

    public static Commit commitInMerge(String branchName,  TreeMap<String, String> map) throws IOException {
        String timestamp = dateFormat(new Date());


        // update parent list
        List<String> parentList = new ArrayList<>();
        parentList.add(getHEAD().getHashId());
        parentList.add(getBranchByName(branchName).getHashId());

        String msg = "Merged " + branchName + " into " + getCurBranchName();
        // hash everything except the hashId itself
        String hashId = Utils.sha1(msg, timestamp, map.toString(), parentList.toString());
        Commit commit = new Commit(msg, hashId, timestamp, parentList, map);

        // update HEAD & curBranch's active pointer

        commit.saveCommit();
        return commit;
    }

    /**
     * Merge the files into a new file in CWD
     * @param filename the file need to be merged
     * @param curMap Current head map
     * @param givenMap Given branch map
     * @return the new file's blob hashID
     */
    public static String makeConflictFile(String filename, TreeMap<String, String> curMap, TreeMap<String, String> givenMap) throws IOException {
        File newFile = new File(filename);

        if (!newFile.exists()) {
            newFile.createNewFile();
        }

        if (!curMap.containsKey(filename)) {
            writeContents(newFile, "<<<<<<< HEAD\n", "=======\n", getBlobContent(givenMap.get(filename)), ">>>>>>>\n");
        } else if (!givenMap.containsKey(filename)) {
            writeContents(newFile, "<<<<<<< HEAD\n", getBlobContent(curMap.get(filename)), "=======\n",  ">>>>>>>\n");
        } else {
            writeContents(newFile, "<<<<<<< HEAD\n", getBlobContent(curMap.get(filename)), "=======\n",  getBlobContent(givenMap.get(filename)), ">>>>>>>\n");
        }

        Blob newBlob = new Blob(newFile);
        newBlob.saveBlob();
        return newBlob.getHashId();
    }


}

