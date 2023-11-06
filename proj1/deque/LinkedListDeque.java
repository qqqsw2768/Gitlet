package deque;

public class LinkedListDeque<T> {

    private class StuffNode {
        public T item;
        public StuffNode before;  // Counterclockwise for linkedList only for addLast
        public StuffNode next;

        public StuffNode(T i, StuffNode b, StuffNode n) { // constructor
            item = i;
            before = b;
            next = n;
        }
    }

    private StuffNode Link; // mark the node's pointer
    private StuffNode first; // addFirst
    private StuffNode last; // addLast
    private int size;


    // Creates an empty linked list deque.
    public LinkedListDeque() {
        Link = new StuffNode(null, null, null);
        size = 0;
        first = Link;
        last = Link;
    }

    public void addFirst(T item) {
        first.before = new StuffNode(item, last, first);
        first = first.before;
        last.next = first;
        size++;
    }

    public void addLast(T item) {
        first.before = new StuffNode(item, last, first);
        last.next = first.before;
        last = last.next;
        size++;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // size must take constant time.
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last,
     * separated by a space. Once all the items have been printed,
     * print out a new line
     * */
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
    public T removeFirst() {
        if (!isEmpty() && first != Link) { // special Link node: only addLast no addFirst
            StuffNode temp = first;
            first = first.next;
            first.before = last;
            last.next = first;
            size--;

            return temp.item;
        } else if (!isEmpty() && first == Link) {
            first = first.next;

            StuffNode temp = first;
            first = first.next;
            first.before = Link;
            last.next = Link;
            Link.next = first;
            first = Link;  //
            size--;

            return temp.item;
        } else {
            return null;
        }

    }

    /**Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     * */
    public T removeLast() {
        if (!isEmpty() && last != Link) { // special Link node: only addFist no addLast
            StuffNode temp = last;
            last = last.before;
            last.next = first;
            size--;

            return temp.item;
        } else if (!isEmpty() && last == Link) {
            last = last.before;

            StuffNode temp = last;

            last = last.before;
            last.next = Link;
            Link.before = last;
            last = Link;
            size--;

            return temp.item;
        } else {
            return null;
        }

    }

    /** get must use iteration, not recursion. 0 from the first*/
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

//    public Iterator<T> iterator() {
//        // todo
//    }

//    public boolean equals(Object o) {
//        // todo
//        return false;
//    }
}
