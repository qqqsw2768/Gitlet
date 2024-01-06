package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AListTest {

    @Test
    public void testGet() {
        ArrayDeque<Integer> L = new ArrayDeque<>();
        L.addFirst(99);
        assertEquals(99, (int)L.get(7));
        L.addFirst(36);
        assertEquals(99, (int)L.get(7));
        assertEquals(36, (int)L.get(6));
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        ArrayDeque<Integer> broken = new ArrayDeque<>();

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
                    assertEquals(correct.getLast(), broken.get(broken.size() - 1));
                }
            }
        }
    }

    @Test
    public void addFirst() {
        ArrayDeque<Integer> list = new ArrayDeque<>();
        int N = 8;
        for (int i = 0; i < 4; i++) {
            list.addFirst(i);
        }

        int j = 0;
        for (int i = 0; i < N; i++) {
            assertEquals(i, (int)list.removeLast());
        }

        for (int i = 0; i < N; i++) {
            list.addFirst(i);
        }
        for (int i = 0; i < N; i++) {
            assertEquals(i, (int)list.removeLast());
        }
    }

    @Test
    public void addLast() {
        ArrayDeque<Integer> list = new ArrayDeque<>();
        int N = 8;
        for (int i = 0; i < N; i++) {
            list.addLast(i);
        }
        int j = 0;
        for (int i = N - 1; i >= 0; i--) {
            assertEquals(i, (int)list.removeLast());
        }

        for (int i = 0; i < N; i++) {
            list.addLast(i);
        }
        for (int i = 0; i < N; i++) {
            assertEquals(i, (int)list.removeFirst());
        }
    }


}
