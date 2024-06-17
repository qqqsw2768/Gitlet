package gitlet;

import java.io.IOException;

import static gitlet.Repository.*;

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
            case "add": //todo: check init before executing the below
                add(args[1]);
                break;
            case "commit":
                commit(args[1]); // message
                // TODO failure case
                break;
            case "rm":
                rm(args[1]);
                break;
            case "log": // TODO: print the merge :the 5th commitId
                log();
                break;
            case "global-log":
                globalLog();
                break;
            case "find":
                find(args[1]);
                break;
            case "branch":
                branch(args[1]);
                break;
            case "checkout":
                if (args[1].equals("--")){
                    checkout(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    checkout(args[1], args[3]);
                } else {
                    checkoutBranch(args[1]);
                }
                break;
            case "status":
                status();
                break;
            default:
                System.out.println("Unknown command: " + firstArg);
                break;
        }
    }
}
