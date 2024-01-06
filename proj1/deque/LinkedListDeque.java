package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T> {

    private class StuffNode {
        private T item;
        private StuffNode before;  // Counterclockwise for linkedList only for addLast
        private StuffNode next;

        public StuffNode(T i, StuffNode b, StuffNode n) { // constructor
            item = i;
            before = b;
            next = n;
        }
    }

    private StuffNode link; // mark the node's pointer
    private StuffNode first; // addFirst
    private StuffNode last; // addLast
    private int size;


    // Creates an empty linked list deque.
    public LinkedListDeque() {
        link = new StuffNode(null, null, null);
        size = 0;
        first = link;
        last = link;
    }

    @Override
    public void addFirst(T item) {
        first.before = new StuffNode(item, last, first);
        first = first.before;
        last.next = first;
        size++;
    }

    @Override
    public void addLast(T item) {
        first.before = new StuffNode(item, last, first);
        last.next = first.before;
        last = last.next;
        size++;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    // size must take constant time.
    @Override
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last,
     * separated by a space. Once all the items have been printed,
     * print out a new line
     * */
    @Override
    public void printDeque() {
        for (int i = 0; i <= size; i++) {
            if (first.item != null) {
                System.out.print(first.item + " ");
            }
            first = first.next;
        }
        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null
     * */
    @Override
    public T removeFirst() {
        if (!isEmpty() && first != link) { // special Link node: only addLast no addFirst
            StuffNode temp = first;
            first = first.next;
            first.before = last;
            last.next = first;
            size--;

            return temp.item;
        } else if (!isEmpty() && first == link) {
            first = first.next;

            StuffNode temp = first;
            first = first.next;
            first.before = link;
            last.next = link;
            link.next = first;
            first = link;  //
            size--;

            return temp.item;
        } else {
            return null;
        }

    }

    /**Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     * */
    @Override
    public T removeLast() {
        if (!isEmpty() && last != link) { // special Link node: only addFist no addLast
            StuffNode temp = last;
            last = last.before;
            last.next = first;
            size--;

            return temp.item;
        } else if (!isEmpty() && last == link) {
            last = last.before;

            StuffNode temp = last;

            last = last.before;
            last.next = link;
            link.before = last;
            last = link;
            size--;

            return temp.item;
        } else {
            return null;
        }

    }

    /** get must use iteration, not recursion. 0 from the first*/
    @Override
    public T get(int index) {
        StuffNode temp = first;
        for (int i = 0; i <= size; i++) {
            if (temp.item == null) { // pass the Link Node
                index++;
            }
            if (i == index) {
                return temp.item;
            } else {
                temp = temp.next;
            }
        }
        return null;
    }

    /** Same as get, but uses recursion
     * */
    public T getRecursive(int index) {
        if (index < 0 || index >= size) {
            return null;
        }

        return getRecursiveHelper(first, index);
    }

    private T getRecursiveHelper(StuffNode node, int targetIndex) {
        if (node == null) {
            return null; // 达到链表末尾，没有找到目标元素
        }

        if (targetIndex == 0) {
            if (node.item != null) {
                return node.item; // 找到目标元素
            } else {
                return getRecursiveHelper(node.next, targetIndex); // 跳过特殊节点
            }
        } else {
            return getRecursiveHelper(node.next, targetIndex - 1); // 继续递归
        }
    }


    public Iterator<T> iterator() {
        return new LinkedListIterator();
    }

    private class LinkedListIterator implements Iterator<T> {
        private  int curSize;

        public LinkedListIterator() {
            this.curSize = 0;
        }

        @Override
        public boolean hasNext() {
            return curSize < size();
        }

        @Override
        public T next() {
            T retuenItem = get(curSize);
            curSize++;
            return retuenItem;
        }
    }

    /**
     * Returns whether or not the parameter o is equal to the Deque.
     * o is considered equal if it is a Deque and if it contains the same contents
     * (as goverened by the generic T’s equals method) in the same order.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (!(o instanceof Deque)) {
            return false;
        }

        Deque<T> obj = (Deque<T>) o;
        if (obj.size() != this.size()) {
            return false;
        }
        for (int i = 0; i < obj.size(); i++) {
            T itemFromObj = obj.get(i);
            T itemFromThis = this.get(i);
            if (!itemFromThis.equals(itemFromObj)) {
                return false;
            }
        }
        return true;
    }
}
