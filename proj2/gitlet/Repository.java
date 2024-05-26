package gitlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import gitlet.Commit.*;

import static gitlet.Commit.*;
import static gitlet.StagingArea.clearStage;
import static gitlet.StagingArea.setCurBranchPointer;
import static gitlet.Utils.*;
import gitlet.StagingArea.*;
import jdk.jshell.execution.Util;

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
    public static String CUR_BRANCH;


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
        String timestamp = new Date(0).toString();

        List<String> parentList = new ArrayList<>();

        Commit firstCommit = new Commit("initial commit", hash, timestamp, parentList, new TreeMap<>(), "master");
        firstCommit.saveCommit();

        Commit.newBranch("master", firstCommit);
        setHEAD(firstCommit);
        Commit.setCurBranchName("master");
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
        //TODO: read a already stagingArea, now only allow to add single file, second addition will overwrite before
        File fileToAdd = new File(fileName);

        /** if the file not exits, escape */
        if (!fileToAdd.exists()) {
            System.out.println("The file is not existed.");
            return;
        }

        // make new blob
        Blob blob = new Blob(fileToAdd);
        StagingArea stagingArea = new StagingArea();
        stagingArea.addStage(blob);

        //TODO let stagingArea persistence be stagingArea place
        // persist the object of blob and stagingArea
        writeObject(ADD, stagingArea);
        blob.saveBlob();
    }


    /**
     * The command: commit
     * Get stagingArea info form deserializing the dir : ADD/RM .if there is nothing: abort
     * load everything in commit object but hashId, then compute it
     */
    public static void commit(String msg) {
        StagingArea addtionStage = StagingArea.getStageFromFile("add");
        StagingArea removalStage = StagingArea.getStageFromFile("rm");

        if (addtionStage.isStageEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }

        String timestamp = new Date().toString();

        // get old commit's map
        TreeMap<String, String> nameToBlobMap = getHEAD().getNameToBlob();

        System.out.println(getHEAD().getMessage());

        // remove mapping in the removal stage
        if (removalStage != null) {
            TreeMap<String, String> removal = removalStage.getFileToBlobMap();
            for (String key : removal.keySet()) {
                nameToBlobMap.remove(key);
            }
        }

        // add mapping in the addition stage
        TreeMap<String,String> addition = addtionStage.getFileToBlobMap();
        for(Map.Entry<String, String> i : addition.entrySet()) {
            nameToBlobMap.put(i.getKey(), i.getValue());
        }

        // update parent list
        List<String> parentList = new ArrayList<>();
        parentList.add(getHEAD().getHashId());

        // hash everything except the hashId itself
        String hashId = Utils.sha1(msg, timestamp, nameToBlobMap.toString(), parentList.toString());
        Commit commit = new Commit(msg, hashId, timestamp, parentList, nameToBlobMap, getHeadName());

        // update HEAD & curBranch's active pointer
        setHEAD(commit);
        setCurBranchPointer(commit);

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
        StagingArea addtionStage = StagingArea.getStageFromFile("add");
        File fileCWD = join(CWD, fileName);

        //  Check if there is key in Addition stage/ CURRENT COMMIT that need to be removed from rm
        TreeMap commit = getHEAD().getNameToBlob();
        if (addtionStage != null) {
            TreeMap<String,String> addition = addtionStage.getFileToBlobMap();
            if (addition.containsKey(fileName)) {
                Blob.deleteBlob(addtionStage, fileName); // delete the blob
                addition.remove(fileName);
                flag = 1;
            }
        }

        if (commit.containsKey(fileName)) {
            commit.remove(fileName);
            if (!Utils.restrictedDelete(fileCWD)) {
                System.out.println("Delete file failed.");;
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
            curCommit = curCommit.getParentCommit();
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
}
