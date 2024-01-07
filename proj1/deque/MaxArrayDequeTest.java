package deque;

import org.junit.Test;
import java.util.Comparator;

import static org.junit.Assert.assertEquals;

public class MaxArrayDequeTest {
    @Test
    public void getMaxItem() {
        IntComparator intComparator = new IntComparator();
        MaxArrayDeque<Integer> maxArrayDeque = new MaxArrayDeque<>(intComparator);
        for (int i = 0; i <= 10000; i++) {
            maxArrayDeque.addFirst(i);
        }

        int actual = maxArrayDeque.max();
        assertEquals(10000, actual);

        int actual2 = maxArrayDeque.max(intComparator);
        assertEquals(10000, actual2);
    }

    private static class IntComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1 - o2; // positive: o1 > o2
        }
    }

    @Test
    public void maxWithStringLengthComparatorTest() {
        StringLengthComparator stringLengthComparator = new StringLengthComparator();
        MaxArrayDeque<String> maxArrayDeque= new MaxArrayDeque<>(stringLengthComparator);

        maxArrayDeque.addFirst("HelloWorld");
        maxArrayDeque.addFirst("Hello World!");

        assertEquals("Hello World!", maxArrayDeque.max());
        assertEquals("Hello World!", maxArrayDeque.max(stringLengthComparator));
    }

    private static class StringLengthComparator implements Comparator<String>{
        @Override
        public int compare(String d1, String d2){
            return d1.length() - d2.length();
        }
    }
}
