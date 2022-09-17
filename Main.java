package gitlet;
import java.io.File;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Minh Nguyen
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        Commands command = new Commands();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        switch (args[0]) {
        case "init" -> validateNumArgs(command, args, 1);
        case "checkout" -> {
            initChecker();
            command.checkout(args);
        }
        case "global-log", "status", "log" -> {
            initChecker();
            validateNumArgs(command, args, 1);
        }
        case "commit", "add", "find", "branch",
                "rm", "rm-branch", "reset", "merge" -> {
            initChecker();
            validateNumArgs(command, args, 2);
        }
        default -> {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        }
    }

    /**
     * Checks the number of arguments versus the expected number,
     * throws a RuntimeException if they do not match.
     * @param command the Command object
     * @param args Argument array from command line
     * @param n    Number of expected arguments
     */
    public static void validateNumArgs(Commands command, String[] args, int n) {
        if (args.length != n) {
            System.out.println("Incorrect Operands");
            System.exit(0);
        }
        switch (args[0]) {
        case "init" -> command.init();
        case "add" -> command.add(args[1]);
        case "commit" -> command.commit(args[1]);
        case "log" -> command.log();
        case "rm" -> command.rm(args[1]);
        case "global-log" -> command.globalLog();
        case "status" -> command.status();
        case "find" -> command.find(args[1]);
        case "branch" -> command.branch(args[1]);
        case "rm-branch" -> command.rmBranch(args[1]);
        case "reset" -> command.reset(args[1]);
        case "merge" -> command.merge(args[1]);
        default -> {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
        }
    }

    /**
     * Method that checks whether init has already been called.
     */
    public static void initChecker() {
        File gitletDirectory = Utils.join
                (System.getProperty("user.dir"), ".gitlet");
        if (!gitletDirectory.exists()) {
            System.out.println("Not in an initialized gitlet directory.");
            System.exit(0);
        }
    }
}
