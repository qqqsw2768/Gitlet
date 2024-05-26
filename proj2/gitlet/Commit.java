package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;
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
    private String branchName;

    /**
     *
     * @param message
     * @param hashId
     * @param timestamp
     * @param parentList
     * @param nameToBlob
     */
    public Commit(String message, String hashId, String timestamp, List<String> parentList, TreeMap<String, String> nameToBlob, String branchName) {
        this.message = message;
        this.hashId = hashId;
        this.timestamp = timestamp;
        this.parentList = parentList;
        this.nameToBlob = nameToBlob;
        this.branchName = branchName;
    }

    public Commit(String message) {
        this.message = message;
    }

    public Commit() {

    }

    public void saveCommit(){
        File path = join(COMMIT_DIR, this.getHashId());
        Utils.writeObject(path, this);
    }

    /**
     * A pointer points to the current branch's "active commits"
     * Every pointer is stored in each file. e.g. current branch pointer/ the HEAD
     * @param branchName
     */
    public static void newBranch(String branchName, Commit commit) {
        File filePath = join(BRANCH_DIR, branchName);
        writeObject(filePath, commit);
    }

    /**
     * Set HEAD points to a commit
     */
    public static void setHEAD(Commit commit) {
        writeObject(HEAD, commit);// save HEAD to the file of HEAD
    }

    /**
     * @return the HEAD from file
     */
    public static Commit getHEAD() {
        return readObject(HEAD, Commit.class);
    }

    /**
     * Store current branch's name !!!
     * @param branchName
     */
    public static void setCurBranchName(String branchName){
        CUR_BRANCH = branchName;
    }

    /**
     * @return current branch's name
     */
    public static String getHeadName() {
        Commit head = getHEAD();
        return head.getBranchName();
    }

    /**
     * Get parent commit from current commit
     */
    public Commit getParentCommit() {
        List<String> parentHashId = this.getParentList();
        for (String i : parentHashId) {
            Commit parent = getCommitByHashId(i);
            if (parent.getBranchName().equals(this.branchName)) {
                return parent;
            }
        }
        return null;
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
        File commit = join(COMMIT_DIR, hashId);
        return readObject(commit, Commit.class);
    }

    public String getBranchName() {
        return branchName;
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
