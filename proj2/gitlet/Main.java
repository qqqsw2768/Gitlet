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
    public static void main(String[] args) {
        // when args is empty
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                try {
                    init();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "add": //todo: check init before executing the below
                ifInitialize();
                try {
                    add(args[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "commit":
                ifInitialize();
                if (args.length < 2) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                try {
                    commit(args[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "rm":
                ifInitialize();
                rm(args[1]);
                break;
            case "log":
                ifInitialize();
                log();
                break;
            case "global-log":
                ifInitialize();
                globalLog();
                break;
            case "find":
                ifInitialize();
                find(args[1]);
                break;
            case "branch":
                ifInitialize();
                branch(args[1]);
                break;
            case "checkout": // TODO:  [commit id] may be abbreviated as for checkout
                ifInitialize();
                try {
                    if (args[1].equals("--")){
                        checkout(args[2]);
                    } else if (args.length == 4) {
                        if (!args[2].equals("--")) {
                            System.out.println("Incorrect operands.");
                        }
                        checkout(args[1], args[3]);
                    } else {
                        checkoutBranch(args[1]);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "status":
                ifInitialize();
                status();
                break;
            case"rm-branch":
                ifInitialize();
                if (args.length >= 2) {
                    rmBranch(args[1]);
                } else {
                    System.out.println("Need branch name");
                }
                break;
            case"reset":
                ifInitialize();
                try {
                    reset(args[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case"merge":
                ifInitialize();
                try {
                    merge(args[1]);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
    }
}
