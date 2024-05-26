package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Repository.BLOB_DIR;
import static gitlet.Utils.*;

/** Represents a gitlet blob object.
 *
 *  @author
 */

public class Blob implements Serializable {
    /** Persist the file:
     * hash the content of file(if it's exists), use the hash name the persisted file
     * */

    private String hashId;
    private String plainName;
    private String plainContent;

    /**
     * Constructor, make the content of file to be a blob: File -> Blob
     * @param file the file need to blob
     */
    public Blob(File file) {
        this.plainName = file.toString();
        this.plainContent = Utils.readContentsAsString(file);
        this.hashId = Utils.sha1(plainName, plainContent);
    }

    /**
     * store the blob: blob -> file
     * name it by its hash
     */
    public void saveBlob() {
        File fileToBlob = join(BLOB_DIR, this.getHashId()); // the path that the blob be stored
        writeObject(fileToBlob, this); // persist the blob
    }

    /**
     * Delete the fileName mapping of blob
     * @param fileName
     */
    public static void deleteBlob(StagingArea stagingArea, String fileName) {
        TreeMap<String,String> treeMap = stagingArea.getFileToBlobMap();
        String hashId = treeMap.get(fileName);
        System.out.println(hashId);
        File file = join(BLOB_DIR, hashId);
        file.delete();
    }

    public String getHashId() {
        return hashId;
    }

    public String getPlainName() {
        return plainName;
    }

    public String getPlainContent() {
        return plainContent;
    }
}
