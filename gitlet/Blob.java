package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import static gitlet.Repository.*;
import static gitlet.Utils.*;

/** Represents a gitlet blob object.
 *
 *  @author qqqsw
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
     * Deserialization
     * Let the blob reverse to file --- make a new file
     * @throws IOException
     */
    public void newFileFromBlob() throws IOException {
        File file = new File(this.getPlainName()); // delete the cwd
        if (!file.exists()) {
            file.createNewFile();
        }
        writeContents(file, this.getPlainContent());
    }

    /**
     * Get Blob's content by it's ID
     */
    public static String getBlobContent(String bolbId) {
        Blob blob = getBlobByHashId(bolbId);
        return blob.getPlainContent();
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
