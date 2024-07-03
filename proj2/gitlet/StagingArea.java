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
    public static void newStageRm(String hashId) {
        Blob blob = Blob.getBlobByHashId(hashId);
        StagingArea sa = new StagingArea(blob);
        writeObject(RM, sa);
    }

    /**
     * Get blob object by hashId, Deserialization
     * @return a Blob object
     */
    public StagingArea addStageRm(String hashId) {
        Blob blob = Blob.getBlobByHashId(hashId);
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
     * 1. Delete the mapping :fileName --- blob id in addition staging if exists
     * 2. Then delete the blob file in blob_dir
     * 3. Serialize the new stagingArea object
     * @param fileName
     */
    public static boolean deleteBlobInADD(String fileName) {
        StagingArea sA = getStageFromFile("add");
        if (sA != null) {
            if (sA.containsFile(fileName)) {
                sA.deleteBlob(fileName);
                writeObject(ADD, sA);
                return true;
            }
        }
        return false;
    }

    /**
     * the rm stage like above
     * @param fileName
     * @return
     */
    public static boolean deleteBlobInRm(String fileName) {
        StagingArea sA = getStageFromFile("rm");
        if (sA != null) {
            if (sA.containsFile(fileName)) {
                sA.deleteBlobMapping(fileName);
                writeObject(RM, sA);
                return true;
            }
        }
        return false;
    }


    /**
     * Delete the mapping and delete the blob file
     * @param fileName
     */
    public void deleteBlob(String fileName) {
        String hashId = fileToBlobMap.get(fileName);
        fileToBlobMap.remove(fileName);
        File file = new File(BLOB_DIR, hashId);
        file.delete();
    }

    /**
     * Delete the mapping, only mapping!!
     * @param fileName
     */
    public void deleteBlobMapping(String fileName) {
        fileToBlobMap.remove(fileName);
    }


    public boolean containsFile(String fileName) {
        return fileToBlobMap.containsKey(fileName);
    }

    public TreeMap<String, String> getFileToBlobMap() {
        return fileToBlobMap;
    }
}
