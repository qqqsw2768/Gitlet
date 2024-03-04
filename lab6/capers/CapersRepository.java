package capers;

import java.io.File;
import java.io.IOException;

import static capers.Dog.DOG_FOLDER;
import static capers.Utils.*;

/** A repository for Capers 
 * @author qqqsw
 * The structure of a Capers Repository is as follows:
 *
 * .capers/ -- top level folder for all persistent data in your lab12 folder
 *    - dogs/ -- folder containing all of the persistent data for dogs
 *    - story -- file containing the current story
 *
 * TODO: change the above structure if you do something different.
 */
public class CapersRepository {
    /** Current Working Directory. */
    static final File CWD = new File(System.getProperty("user.dir"));

    /** Main metadata folder. */
    static final File CAPERS_FOLDER = Utils.join(CWD,"capers", "story");

    /**
     * Does required filesystem operations to allow for persistence.
     * (creates any necessary folders or files)
     * Remember: recommended structure (you do not have to follow):
     *
     * .capers/ -- top level folder for all persistent data in your lab12 folder
     *    - dogs/ -- folder containing all of the persistent data for dogs
     *    - story -- file containing the current story
     *
     *    make two above dir and file, i made this so ugly
     */
    public static void setupPersistence() {
        File capersDir = new File(String.valueOf(CAPERS_FOLDER));
        File dogsDir = new File(String.valueOf(DOG_FOLDER));

        if (!capersDir.exists()) {
            capersDir.mkdirs();
        }
        if (!dogsDir.exists()) {
            dogsDir.mkdirs();
        }

        File story = new File(capersDir, "story");

        if (!story.exists()) {
            try {
                story.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Appends the first non-command argument in args
     * to a file called `story` in the .capers directory.
     * @param text String of the text to be appended to the story
     */
    public static void writeStory(String text) {
        File story = new File(CAPERS_FOLDER, "story"); // not create just invoke, like pointer
        String oldContent = Utils.readContentsAsString(story);// read out the recently added contents
        if (oldContent == null || oldContent.length() == 0) {
            writeContents(story,text); // first write and not put nextLine in the first
        } else {
            writeContents(story,oldContent, "\n", text);// join the old and new together, put them all into the file
        }
        String pirntOut = Utils.readContentsAsString(story);// read all contents, ready to print them out
        System.out.print(pirntOut);
    }

    /**
     * Creates and persistently saves a dog using the first
     * three non-command arguments of args (name, breed, age).
     * Also prints out the dog's information using toString().
     */
    public static void makeDog(String name, String breed, int age) {
        Dog dog = new Dog(name, breed, age);
        dog.saveDog();
        System.out.print(dog.toString());
    }

    /**
     * Advances a dog's age persistently and prints out a celebratory message.
     * Also prints out the dog's information using toString().
     * Chooses dog to advance based on the first non-command argument of args.
     * @param name String name of the Dog whose birthday we're celebrating.
     */
    public static void celebrateBirthday(String name) {
        Dog dog = Dog.fromFile(name);
        dog.haveBirthday();
        dog.saveDog();
    }
}
