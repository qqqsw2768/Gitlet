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
    private TreeMap<String, String> fileToBlobMap;

    public StagingArea() {
        this.fileToBlobMap = new TreeMap<>();
    }

    /**
     * add file to stagingArea of addition: make the mapping of StagingArea -> blob
     * @param blob
     */
    public StagingArea addStageAdd(Blob blob) {
        this.fileToBlobMap.put(blob.getPlainName(), blob.getHashId());
        return this;
    }

    public StagingArea(Blob blob) {
        TreeMap<String, String> treeMap= new TreeMap<>();
        treeMap.put(blob.getPlainName(), blob.getHashId());
        this.fileToBlobMap = treeMap;
    }

    /**
     * Get blob object by hashId, Deserialization
     * @return a Blob object
     */
    public static StagingArea newStageRm(String hashId) {
        File file = new File(BLOB_DIR, hashId);
        Blob blob = readObject(file, Blob.class);
        return new StagingArea(blob);
    }

    /**
     * Get blob object by hashId, Deserialization
     * @return a Blob object
     */
    public StagingArea addStageRm(String hashId) {
        File file = new File(BLOB_DIR, hashId);
        Blob blob = readObject(file, Blob.class);
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
        String add = readContentsAsString(ADD); // if the file is null the `readObject` will fail
        String rm = readContentsAsString(RM);

        if (stagePath.equals("add") && !add.isEmpty()) {
            stagingArea = readObject(ADD, StagingArea.class);
        } else if (stagePath.equals("rm") && !rm.isEmpty()) {
            stagingArea = readObject(Repository.RM, StagingArea.class);
        }
        return stagingArea;
    }

    /**
     * Get the treeMap from addStaging
     * @param stagePath
     * @return
     */
    public static TreeMap<String, String> getMapFrom(String stagePath) {
        return getStageFromFile(stagePath).getFileToBlobMap();
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
     * Clear special file in addition staging if exists
     * @param fileName
     */
    public static void deleteFileInADD(String fileName) {
        StagingArea stagingArea = getStageFromFile("add");
        if (stagingArea != null) {
            TreeMap<String, String> map = stagingArea.getFileToBlobMap();
            if (map.containsKey(fileName)) {
                Blob.deleteBlobInStage(stagingArea, fileName);
                map.remove(fileName);
            }
        }
    }

    public TreeMap<String, String> getFileToBlobMap() {
        return fileToBlobMap;
    }
}
