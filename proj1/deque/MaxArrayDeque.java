package deque;

import java.util.Comparator;
import java.util.Iterator;

public class MaxArrayDeque<T> extends ArrayDeque{
    private Comparator<T> comparator; // comparator 是一个指向 Comparator<T> 接口的引用

    /**
     * creates a MaxArrayDeque with the given Comparator
     * constructor
     */
    public MaxArrayDeque(Comparator<T> c) {
        comparator = c;
    }

    /**
     * returns the maximum element in the deque as governed by the previously given Comparator.
     * If the MaxArrayDeque is empty, simply return null
     */
    public T max() {
        return max(comparator);
    }

    /**
     * returns the maximum element in the deque as governed by the parameter Comparator c.
     * If the MaxArrayDeque is empty, simply return null.
     */
    public T max(Comparator<T> c) {
        if (isEmpty()) {
            return null;
        }

        Iterator<T> iterator = iterator();

        T maxItem = iterator.next();

        while (iterator.hasNext()) {
            T current = iterator.next();
            if (c.compare(current, maxItem) > 0) {
                maxItem = current;
            }
        }

        return maxItem;
    }
}
