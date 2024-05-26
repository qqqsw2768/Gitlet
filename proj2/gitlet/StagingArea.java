package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Repository.*;
import static gitlet.Repository.BRANCH_DIR;
import static gitlet.Utils.*;

/**
 *  store the function about stagingArea
 *  @author qqqaw
 */

public class StagingArea implements Serializable {
    /** The mapping of file and its blob hash id
     * <FILENAME, BLOBHASH>
     * */
    private TreeMap<String, String> fileToBlobMap = new TreeMap<>();

//    /** In addition or removal file */
//    private File IN_FILE;
//
//    public StagingArea(File IN_FILE) {
//        this.IN_FILE = IN_FILE;
//        this.fileToBlobMap = new TreeMap<>();
//    }


    public StagingArea() {
        this.fileToBlobMap = new TreeMap<String, String>();
    }

    /**
     * add file to stagingArea of addition: make the mapping of StagingArea -> blob
     * @param blob
     */
    public StagingArea addStage(Blob blob) {
        this.fileToBlobMap.put(blob.getPlainName(), blob.getHashId());
        return this;
    }

    /**
     * Deserialize from ADD file or RM file
     * return a object of StagingArea
     * @param stagePath
     */
    public static StagingArea getStageFromFile(String stagePath) {
        StagingArea stagingArea = null;
        String add = readContentsAsString(ADD);
        String rm = readContentsAsString(RM);

        if (stagePath.equals("add") && !add.isEmpty()) {
            stagingArea = readObject(ADD, StagingArea.class);
        } else if (stagePath.equals("rm") && !rm.isEmpty()) {
            stagingArea = readObject(Repository.RM, StagingArea.class);
        }
        return stagingArea;
    }

    /**
     * Set current branch's active pointer
     * Store in branchName/"name" file
     * @param commit
     */
    public static void setCurBranchPointer(Commit commit){
        File filePath = join(BRANCH_DIR, commit.getBranchName());
        writeObject(filePath, commit);
    }
    /**
     * If the StageArea map is empty will return 1
     * @return
     */
    public boolean isStageEmpty() {
        return this.fileToBlobMap.isEmpty();
    }

    /**
     * Clear rm & add
     */
    public static void clearStage() {
        writeObject(ADD, null);
        writeObject(RM, null);
    }

    /**
     * get the map from StagingArea
     * @return a TreeMap in StagingArea
     */
//    public TreeMap<String, String> getFileToBlobMap() {
//        return  (TreeMap<String, String>) this.fileToBlobMap.clone();
//    }

    public TreeMap<String, String> getFileToBlobMap() {
        return fileToBlobMap;
    }
}
