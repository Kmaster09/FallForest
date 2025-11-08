import java.io.*;
import java.util.Random;

/**
 * FallForest.java
 *
 * Usage:
 *   java FallForest        // uses default windStrength = 2
 *   java FallForest 3      // sets windStrength = 3
 *
 * This program simulates leaves falling (autumn) and growing (spring)
 * on a small recursive tree structure. Output is both printed to the console
 * and written to leaf_fall_log.txt (created in working directory).
 *
 * See the comments at the bottom for reflections required by the assignment.
 */

/* Package-private Branch class as requested */
class Branch {
    private int leaves;
    private Branch[] subBranches;
    private final Random rand = new Random();

    /**
     * Create a branch with 'leaves' leaves and 'subCount' immediate sub-branches.
     * Each sub-branch is constructed with a small random amount of leaves and
     * a small random number of their own sub-branches.
     */
    public Branch(int leaves, int subCount) {
        this.leaves = Math.max(0, leaves);
        subBranches = new Branch[Math.max(0, subCount)];
        for (int i = 0; i < subBranches.length; i++) {
            // small random children: 1-3 leaves, 0-1 sub-branches (similar to provided code)
            subBranches[i] = new Branch(rand.nextInt(3) + 1, rand.nextInt(2));
        }
    }

    /**
     * Recursive autumn behavior: leaves fall from this branch and then from sub-branches.
     * Uses FallForest.windStrength to decide how many leaves fall each invocation.
     *
     * @param level depth level for indentation/visualization
     */
    public void fallLeaves(int level) {
        // base: nothing to do if this branch and all children have no leaves and no subbranches
        if (leaves <= 0 && subBranches.length == 0) return;

        // Leaves fall from this branch: up to windStrength leaves per call
        int permitted = Math.max(1, FallForest.windStrength); // ensure at least 1
        for (int i = 0; i < permitted && leaves > 0; i++) {
            String indent = " ".repeat(level * 2);
            System.out.println(indent + "🍂  A leaf falls from branch level " + level);
            leaves--;
            // gentle pause for animation
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                // If interrupted, stop the animation but continue logic
            }
        }

        // If there are still leaves left on this branch, recurse on same branch
        // (this models continued wind events hitting the same branch).
        if (leaves > 0) {
            // Recurse on the same branch to continue shedding leaves.
            // This recursion will eventually stop when leaves <= 0.
            fallLeaves(level);
        }

        // Now recurse into sub-branches
        for (Branch b : subBranches) {
            if (b != null) b.fallLeaves(level + 1);
        }
    }

    /**
     * Recursive spring behavior: grows / regrows leaves across the tree.
     * This will add a small random amount of leaves to each branch.
     *
     * @param level depth level for indentation/visualization
     */
    public void growLeaves(int level) {
        // Simulate growth: each branch gains 1-3 leaves (random), and sub-branches also grow.
        int grown = rand.nextInt(3) + 1;
        leaves += grown;
        String indent = " ".repeat(level * 2);
        System.out.println(indent + "🌱  Branch level " + level + " grows " + grown + " leaves (total now " + leaves + ")");
        try {
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }

        for (Branch b : subBranches) {
            if (b != null) b.growLeaves(level + 1);
        }
    }
}

public class FallForest {
    // Global wind strength: number of leaves that can fall per call on a branch
    // Can be changed via command-line arg, or left at default 2.
    public static int windStrength = 2;

    // We'll "tee" System.out to both console and a file by replacing System.out.
    private static PrintStream originalOut;

    public static void main(String[] args) {
        // parse optional wind strength from args
        if (args != null && args.length > 0) {
            try {
                int w = Integer.parseInt(args[0]);
                if (w > 0) windStrength = w;
            } catch (NumberFormatException nfe) {
                // ignore and use default
            }
        }

        // set up log file and teeing of System.out
        originalOut = System.out;
        FileOutputStream fos = null;
        PrintStream fileStream = null;
        try {
            fos = new FileOutputStream("leaf_fall_log.txt");
            fileStream = new PrintStream(fos, true, "UTF-8");
            TeeOutputStream tos = new TeeOutputStream(originalOut, fileStream);
            PrintStream tee = new PrintStream(tos, true, "UTF-8");
            System.setOut(tee); // now all System.out prints go to console + file
        } catch (IOException e) {
            // If log file can't be created, continue printing to console only.
            System.err.println("Warning: could not create leaf_fall_log.txt. Continuing without file logging.");
        }

        System.out.println("The forest prepares for autumn...");
        System.out.println("(windStrength = " + windStrength + ")");
        // create a small tree as in the assignment
        Branch tree = new Branch(3, 2);
        // Autumn: leaves fall
        tree.fallLeaves(0);
        System.out.println("The forest sleeps for winter.");
        System.out.println();

        // Spring: regenerate leaves
        System.out.println("Spring arrives — new growth begins...");
        tree.growLeaves(0);
        System.out.println("The forest awakens for a new year.");

        // flush and close file stream if we created it; but keep console open
        // Restore original System.out and close the file stream (if any).
        try {
            System.out.flush();
            // If we replaced System.out with tee, restore to original
            if (originalOut != null && System.out != originalOut) {
                System.setOut(originalOut);
            }
            if (fileStream != null) fileStream.close();
            if (fos != null) fos.close();
        } catch (Exception e) {
            // ignore cleanup exceptions for brevity
        }

        // Let user know where the log is (printed to console originalOut)
        originalOut.println("Log saved to leaf_fall_log.txt (in working directory) — and printed above.");
    }

    /**
     * TeeOutputStream writes all bytes to two underlying streams.
     * Used to mirror console output into a file.
     */
    private static class TeeOutputStream extends OutputStream {
        private final OutputStream out1;
        private final OutputStream out2;

        public TeeOutputStream(OutputStream a, OutputStream b) {
            this.out1 = a;
            this.out2 = b;
        }

        @Override
        public void write(int b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void write(byte[] b) throws IOException {
            out1.write(b);
            out2.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            out1.write(b, off, len);
            out2.write(b, off, len);
        }

        @Override
        public void flush() throws IOException {
            out1.flush();
            out2.flush();
        }

        @Override
        public void close() throws IOException {
            try { out1.close(); } catch (IOException ignored) {}
            try { out2.close(); } catch (IOException ignored) {}
        }
    }
}
