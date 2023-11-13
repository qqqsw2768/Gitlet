package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    @Test
    public void testThreeAddThreeRemove() {
        BuggyAList<Integer> buggyAList = new BuggyAList<>();
        AListNoResizing<Integer> aListNoResizing = new AListNoResizing<>();

        buggyAList.addLast(4);
        aListNoResizing.addLast(4);
        buggyAList.addLast(5);
        aListNoResizing.addLast(5);
        buggyAList.addLast(6);
        aListNoResizing.addLast(6);

        for (int i = 0; i < 3; i++) {
            assertEquals(buggyAList.removeLast(), aListNoResizing.removeLast());
            assertEquals(aListNoResizing.size(), buggyAList.size());
            for (int j = 0; j < buggyAList.size(); j++) {
                assertEquals(buggyAList.get(j), aListNoResizing.get(j));
            }
        }
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        BuggyAList<Integer> broken = new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
                System.out.println("addLast(" + randVal + ")");
            } else if (operationNumber == 1) {
                // size
                int size = correct.size();
                int size2 = broken.size();
                System.out.println("correct size: " + size);
                System.out.println("broken size: " + size2);
            } else if (operationNumber == 2) {
                // removeLast
                if (correct.size() != 0 && broken.size() != 0) {
                    assertEquals(correct.removeLast(), broken.removeLast());
                }
            } else if (operationNumber == 3) {
                // getLast
                if (correct.size() != 0 && broken.size() != 0) {
                    assertEquals(correct.getLast(), broken.getLast());
                }
            }
        }
    }
}
