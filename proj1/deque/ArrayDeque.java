package deque;

public class ArrayDeque<T> {
    private T[] arrayT;
    private int size;
    private int sizeNum = 8;

    private int nextFirst;
    private int nextLast;

    private int sumFirst; // compute the number that addFirst
    private int sumLast;

    /** Creates an empty array deque. The initial size is 8.*/
    public ArrayDeque() {
        arrayT = (T[]) new Object[sizeNum];
        size = 0;
        nextFirst = sizeNum - 1;
        nextLast = 0;
        sumFirst = 0;
        sumLast = 0;
    }

    /**  Adds an item of type T to the front of the deque. You can assume that item is never null.*/
    public void addFirst(T item) {
        if (size == sizeNum) {
            resize(sizeNum * 2);
        }
        arrayT[nextFirst] = item;
        size = size + 1;
        sumFirst = sumFirst + 1;
        nextFirst = (nextFirst + sizeNum - 1) % sizeNum; // circle the array
    }

    public void addLast(T item) {
        if (size == sizeNum) { // if last circle the queue is full and the next circle they will on this condition
            resize(sizeNum * 2);
        }
        arrayT[nextLast] = item;
        size = size + 1;
        sumLast = sumLast + 1;
        nextLast = (nextLast + 1) % sizeNum;
    }

    /** Resizes the underlying array to the target capacity */
    private void resize(int capacity) {
        T[] temp = (T[]) new Object[capacity];
        if (sumFirst != 0) {
            for (int i = 0; i < sumFirst; i++) {
                int index = sizeNum - sumFirst + i;
                temp[i] = arrayT[index];
            }
        }
        if (sumLast != 0) {
            System.arraycopy(arrayT, 0, temp, sumFirst, sumLast); // 2th para: nextLast - sumLast
        }
        arrayT = temp;
        sumLast = size; // after resizing, the whole items is the 'Last' part
        sumFirst = 0; // and no 'First' part
        sizeNum = sizeNum * 2;
        nextLast = size;
        nextFirst = sizeNum - 1;
    }

    /** For the removeFirst that "sumFirst == 0" */
    private void resize() {
        T[] temp = (T[]) new Object[sizeNum];
        System.arraycopy(arrayT, 1, temp, 0, sumLast - 1);
        arrayT = temp;
        sumLast--;
        nextLast--;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Returns the number of items in the deque.*/
    // size must take constant time.
    public int size() {
        return size;
    }

    /** Prints the items in the deque from first to last,
     * separated by a space. Once all the items have been printed,
     * print out a new line
     * */
    public void printDeque() {
        if (sumFirst != 0) { // If the queue has addFirst part then print it
            for (int i = 0; i < sumFirst; i++) {
                int index = sizeNum - sumFirst + i;
                System.out.print(arrayT[index] + " ");
            }
        }

        for (int i = 0; i < sumLast; i++) {
            System.out.print(arrayT[i] + " ");
        }

        System.out.println();
    }

    /** Removes and returns the item at the front of the deque.
     * If no such item exists, returns null
     * */
    public T removeFirst() {
        if (isEmpty()) {
            return null;
        }

        if ((size < arrayT.length / 4) && (size > 4)) {
            resize(arrayT.length / 4);
        }

        if (sumFirst == 0) {
            T temp = get(0);
            arrayT[0] = null;
            size--;
            resize();

            return  temp;
        }

        T temp = get(nextFirst + 1);
        arrayT[nextFirst + 1] = null;
        size = size - 1;
        sumFirst--;
        nextFirst++;

        return temp;
    }

    /**Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     * */
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }

        if ((size < arrayT.length / 4) && (size > 4)) {
            resize(arrayT.length / 4);
        }

        if (size == sizeNum) {
            nextLast = size;
        }

        T temp = get(nextLast - 1);
        arrayT[nextLast - 1] = null;
        size--;
        nextLast--;
        sumLast--;

        return temp;
    }

    /** get must use iteration, not recursion. 0 from the first */
    public T get(int index) {
        return arrayT[index];
    }

    //    public Iterator<T> iterator() {
    //    }

    //    public boolean equals(Object o) {
    //        return false;
    //    }
}
