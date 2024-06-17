package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.TreeMap;

import static gitlet.Repository.BLOB_DIR;
import static gitlet.Repository.CWD;
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
     * Store the blob: blob -> file, Serialization
     * name it by its hash
     */
    public void saveBlob() {
        File file = join(BLOB_DIR, this.getHashId()); // the path that the blob be stored
        if (file.exists()) { // no modified in CWD compare to addStaging
            System.exit(0);
        }
        writeObject(file, this); // persist the blob
    }

    /**
     * Delete the blob in the stagingArea by it's mapping fileName
     * @param fileName
     */
    public static void deleteBlobInStage(StagingArea stagingArea, String fileName) {
        TreeMap<String,String> treeMap = stagingArea.getFileToBlobMap();
        String hashId = treeMap.get(fileName);
        System.out.println(hashId);
        File file = join(BLOB_DIR, hashId);
        file.delete();
    }

    /**
     * Deserialization
     * Let the blob reverse to file --- make a new file
     * @throws IOException
     */
    public void newFileFromBlob() throws IOException {
        File file = new File(CWD, this.getPlainName());
        if (file.exists()) {
            writeContents(file, this.getPlainContent());
        } else {
            file.createNewFile();
            writeContents(file, this.getPlainContent());
        }
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
