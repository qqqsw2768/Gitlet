package gitlet;

import java.io.File;
import java.io.IOException;

import static gitlet.Repository.*;;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author qqqsw
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) throws IOException {
        // when args is empty
        if (args.length == 0) {
            Utils.error("Must have at least one argument");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                init();
                break;
            case "add":
                add(args[1]);
                break;
            case "commit":
                commit(args[1]); // message
                break;
            case "rm":
                rm(args[1]);
                break;
            case "log":
                log();
                break;
            case "global-log":
                globalLog();
                break;
            case "find":
                find(args[1]);
        }
    }
}
