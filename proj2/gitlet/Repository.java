package gitlet;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static gitlet.Commit.*;
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

    /**
     * The command: add
     * Add file to stagingArea
     * @param fileName
     */
    public static void add(String fileName) {
        File fileToAdd = new File(fileName);

        /** if the file not exits, escape */
        if (!fileToAdd.exists()) {
            System.out.println("The file is not existed.");
            return;
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

        //TODO let stagingArea persistence be stagingArea place
        // persist the object of blob and stagingArea
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

        if (addtionStage == null && removalStage == null) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        String timestamp = dateFormat(new Date());

        TreeMap<String, String> nameToBlobMap = getHEAD().getNameToBlob();// get old commit's map

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
        setHEAD(commit, getCurBracnchName());
        updateBranch(commit);

        // persist
        commit.saveCommit();

        //clear add rm
        clearStage();
    }

    /**
     * The command: rm
     *
     * @param fileName the fileName need to remove
     */
    public static void rm(String fileName) {
        int flag = 0;
        StagingArea addtionStage = getStageFromFile("add");
        File fileCWD = new File(fileName);

        //  Check if there is key in Addition stage/ CURRENT COMMIT that need to be removed from rm
        TreeMap<String, String> commit = getHEAD().getNameToBlob();
        if (addtionStage != null) {
            TreeMap<String,String> addition = addtionStage.getFileToBlobMap();
            if (addition.containsKey(fileName)) {
                Blob.deleteBlobInStage(addtionStage, fileName); // delete the blob
                addition.remove(fileName);
                flag = 1;
            }
        }

        if (commit.containsKey(fileName)) { // if file is tracked by current commit, then add it to rmStage & delete it
            String hashId = commit.get(fileName);
            StagingArea rmStage = getStageFromFile("rm");
            if (rmStage == null) {
                StagingArea stagingArea = newStageRm(hashId);
                writeObject(RM, stagingArea);
            } else {
                rmStage.addStageRm(hashId);
                writeObject(RM, rmStage);
            }

            if (!Utils.restrictedDelete(fileCWD)) {
                System.out.println("Delete file failed.");
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
        for(String i: allCommit) {
            Commit curCommit = getCommitByHashId(i);
            if(curCommit.getMessage().contains(msg)) {
                System.out.println(curCommit.getHashId());
            }
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
        deleteFileInADD(fileName);
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
        deleteFileInADD(fileName);
    }

    /**
     * The command: checkout
     * Usage: checkout [branch name]
     * @param branchName
     */
    public static void checkoutBranch(String branchName) throws IOException {
        if (branchName.equals(getCurBracnchName())) {
            System.out.println("No need to checkout the current branch.");
        }

        Commit checkout = getBranchByName(branchName);
        checkStatus();
        if (!restFiles.isEmpty() || !stagedFilesAdd.isEmpty() || !modified.isEmpty()) {
            System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
            System.exit(0);
        }

        deleteAllFilesFrom(getHEAD());
        createAllFilesFrom(checkout);

        setHEAD(checkout, branchName);
        updateBranch(checkout);
    }

    /**
     * The command: status
     */
    public static void status() {
        checkStatus();
        printStatus();
    }

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

    public static List<String> stagedFilesAdd = new ArrayList<>();
    public static List<String> stagedFilesRm = new ArrayList<>();
    public static List<String> modified = new ArrayList<>();
    public static List<String> deleted = new ArrayList<>();
    public static List<String> restFiles = new ArrayList<>(); // Untracked files

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

}
