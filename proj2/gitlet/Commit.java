package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet commit object and some helper methods.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author qqqsw
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /** The list of the parents' hashId of this Commit. */
    private List<String> parentList;

    /** The hashId of this commit*/
    private String hashId;

    /** The relatively time when the commit was committed , take Epoch*/
    private String timestamp;

    /** The mapping stores fileName <--> blobName */
    private TreeMap<String, String> nameToBlob = new TreeMap<>();

    /** Store this commit belong to which branch */
//    private String branchName;

    /**
     *
     * @param message
     * @param hashId
     * @param timestamp
     * @param parentList
     * @param nameToBlob
     */
    public Commit(String message, String hashId, String timestamp, List<String> parentList, TreeMap<String, String> nameToBlob) {
        this.message = message;
        this.hashId = hashId;
        this.timestamp = timestamp;
        this.parentList = parentList;
        this.nameToBlob = nameToBlob;
//        this.branchName = branchName;
    }

    public Commit(String message) {
        this.message = message;
    }

    /**
     * Store the commit in COMMIT_DIR
     */
    public void saveCommit(){
        File path = new File(COMMIT_DIR, this.getHashId());
        Utils.writeObject(path, this);
    }

    /**
     * Format date
     */
    public static String dateFormat(Date time) {
        SimpleDateFormat ft = new SimpleDateFormat("EEE MMM d HH:mm:ss y Z", Locale.US);
        String timestamp = ft.format(time);

        return timestamp;
    }

    /**
     * A pointer points to the current branch's "active commits"
     * Every pointer is stored in each file. e.g. current branch pointer/ the HEAD
     * If a branch name is been used, exit
     * @param branchName
     */
    public static void newBranch(String branchName, Commit commit) {
        File filePath = new File(BRANCH_DIR, branchName);
        if (filePath.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        writeObject(filePath, commit);
    }

    /**
     * Set HEAD points to a commit
     */
    public static void setHEAD(Commit commit, String curBranch) throws IOException {
        setCurBranchName(curBranch);
        writeObject(HEAD, commit);// save HEAD to the file of HEAD
    }

    /**
     * @return the HEAD from file
     */
    public static Commit getHEAD() {
        return readObject(HEAD, Commit.class);
    }

    /**
     * Update current branch's active pointer
     * Store in branchName/"name" file
     * @param commit
    */
    public static void updateBranch(Commit commit){
        File filePath = new File(BRANCH_DIR, getCurBracnchName());
        if (filePath == null) {
            System.out.println();
        }
        writeObject(filePath, commit);
    }

    /**
     * Store current branch's name
     * @param name
     */
    public static void setCurBranchName(String name) throws IOException {
        if (!CUR_BRANCH.exists()) {
            CUR_BRANCH.createNewFile();
        }
        writeContents(CUR_BRANCH, name);
    }

    /**
     * @return current branch's (HEAD point to) name
     */
    public static String getCurBracnchName() {
        String name = readContentsAsString(CUR_BRANCH);
        return name;
    }

    public static TreeMap<String, String> getHeadMap() {
        return getHEAD().getNameToBlob();
    }

    /**
     * Print all branches' name, head first marked by *
     */
    public static void printBranchName() {
        String head = getCurBracnchName();
        List<String> allBranch = Utils.plainFilenamesIn(BRANCH_DIR);
        System.out.println("=== Branches ===");
        System.out.println("*" + head);
        for (String i : allBranch) {
            if (!i.equals(head)) {
                System.out.println(i);
            }
        }
        System.out.println();
    }

    /**
     * Get first parent commit from current commit,
     */
    public Commit getFirstParent() {
        List<String> list = this.getParentList();
        String first = list.get(0);
        return  getCommitByHashId(first);
    }

    public void printCommit() {
        //TODO for merge commits
        // merge: a b (2 parent)
        System.out.println("===");
        System.out.println("commit " + this.getHashId());
        System.out.println("Date: " + this.getTimestamp());
        System.out.println(this.getMessage());
        System.out.println();
    }

    /**
     * Get commit object by its hashId
     * @param hashId
     * @return Commit
     */
    public static Commit getCommitByHashId(String hashId) {
        File commit = new File(COMMIT_DIR, hashId);
        if (!commit.exists()) {
            System.out.println("No commit with that id exists.");
        }
        return readObject(commit, Commit.class);
    }

    /**
     * Get branch pointer(a commit object) by its name
     * @param branchName
     * @return Commit
     */
    public static Commit getBranchByName(String branchName) {
        File pointer = new File(BRANCH_DIR, branchName);
        if (!pointer.exists()) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        return readObject(pointer, Commit.class);
    }

    /**
     *  Detect file's blob by it's name from commit then put it to CWD
     * @param fileName
     */
    public void makeFileFromBlob(String fileName) throws IOException {
        TreeMap<String, String> treeMap = this.getNameToBlob();
        if (treeMap.containsKey(fileName)) {
            Blob blob = getBlobByHashId(treeMap.get(fileName));
            blob.newFileFromBlob();
        } else {
            System.out.println("File does not exist in that commit.");
        }
    }

    /**
     * Deserialize the blob
     * @param blobId
     * @throws IOException
     */
    public static Blob getBlobByHashId(String blobId) {
        File filePath = new File(BLOB_DIR, blobId);
        return readObject(filePath, Blob.class);
    }

    /**
     * Takes all files in one commit and put them to CWD
     * @throws IOException
     */
    public static void createAllFilesFrom(Commit commit) throws IOException {
        TreeMap<String, String> treeMap = commit.getNameToBlob();
        for (String hashId : treeMap.values()) {
            getBlobByHashId(hashId).newFileFromBlob();
        }
    }
    public static void deleteAllFilesFrom(Commit commit) {
        TreeMap<String, String> treeMap = commit.getNameToBlob();
        for (String i : treeMap.keySet()) {
            File file = new File(CWD, i);
            Utils.restrictedDelete(file);
        }
    }

    /**
     * Check deference between new addition and current commit
     * @param blob
     */
    public static void checkInCommit(Blob blob) {
        TreeMap<String, String> commitMap = getHEAD().getNameToBlob();
        String fileName = blob.getPlainName();
        String blobId = blob.getHashId();
        String commitId = commitMap.get(fileName);

        if (commitMap.containsKey(fileName) && commitId.equals(blobId)) { // no modified in current commit
            System.exit(0);
        }
    }

    public String getMessage() {
        return message;
    }

    public List<String> getParentList() {
        return parentList;
    }

    public String getHashId() {
        return hashId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public TreeMap<String, String> getNameToBlob() {
        return nameToBlob;
    }
}
