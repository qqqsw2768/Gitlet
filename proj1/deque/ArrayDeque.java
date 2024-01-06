package deque;

import java.util.Iterator;

public class ArrayDeque<T> {
    private T[] arrayT;
    private int size; // the size that not including null, count the numbers that insert into array
    private int sizeNum = 8; // the size of the whole array (including null) "INITIAL"

    private int nextFirst; // the next position of addFirst
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
            resize(sizeNum * 2, 1);
        }
        arrayT[nextFirst] = item;
        size = size + 1;
        sumFirst = sumFirst + 1;
        nextFirst = (nextFirst + sizeNum - 1) % sizeNum; // circle the array
    }

    public void addLast(T item) {
        if (size == sizeNum) { // if last circle the queue is full and the next circle they will on this condition
            resize(sizeNum * 2, 1);
        }
        arrayT[nextLast] = item;
        size = size + 1;
        sumLast = sumLast + 1;
        nextLast = (nextLast + 1) % sizeNum;
    }

    /** Resizes the underlying array to the target capacity */
    private void resize(int capacity, int flag) { // flag: addResize->1 removeResive->0
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
        if (flag == 1) {
            sizeNum = sizeNum * 2;
        } else{
            sizeNum = sizeNum / 2;
        }
        nextLast = size;
        nextFirst = sizeNum - 1;
    }

    /** For the removeFirst that "sumFirst == 0" */
    private void resizeFirst() {
        T[] temp = (T[]) new Object[sizeNum];
        System.arraycopy(arrayT, 1, temp, 0, sumLast - 1);
        arrayT = temp;
        sumLast--;
        nextLast--;
    }

    /** 补全First部分删除留下的空格
     * 0 0 0 0 0 0 0 0 0 0 0 (10位
     * 0 0 0 0 0 0 0 0 0 8 0
     * 将“8” 移到后面去
     */
    private void resizeLast() {
        T[] temp = (T[]) new Object[sizeNum];
        System.arraycopy(arrayT, nextFirst, temp, nextFirst + 1, sumFirst);
        arrayT = temp;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean isFull() {
        return size == sizeNum;
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

        if (isFull() && nextLast == 0) {
            nextLast = sizeNum;
        }

        if (sumFirst == 0) {
            T temp = get(0);
            arrayT[0] = null;
            size--;
            resizeFirst();
            checkUseOfMomery();

            return temp;
        }

        if (isFull() && nextFirst == sizeNum - 1) {
            nextFirst = -1;
        }
        T temp = get(nextFirst + 1);
        arrayT[nextFirst + 1] = null;
        size = size - 1;
        sumFirst--;
        nextFirst++;
        checkUseOfMomery();

        return temp;
    }

    /**Removes and returns the item at the back of the deque.
     * If no such item exists, returns null.
     * */
    public T removeLast() {
        if (isEmpty()) {
            return null;
        }
        if (isFull() && nextLast == 0) {
            nextLast = sizeNum;
        }
        if (isFull() && nextFirst == sizeNum - 1 && sumLast == 0) { // manually set the size when the 'real' array is full
            nextFirst = -1;
        }
        if (sumLast == 0) { // for the all addFirst no addLast
            int tempLast = sizeNum - 1;
            T temp = get(tempLast);
            nextFirst++;
            sumFirst--;
            size--;
            resizeLast();
            checkUseOfMomery();

            return temp;
        }
        T temp = get(nextLast - 1);
        arrayT[nextLast - 1] = null;
        size--;
        nextLast--;
        sumLast--;

        checkUseOfMomery();
        return temp;
    }

    /** get must use iteration, not recursion. 0 from the first */
    public T get(int index) {
        if (isEmpty()) {
            return null;
        }
        return arrayT[index];
    }

    public void checkUseOfMomery() {
        if ((size < arrayT.length / 4) && (size > 0) && sizeNum > 8) {
            resize(arrayT.length / 2, 0);
        }
    }

        public Iterator<T> iterator() {
            return new ArrayIterator();
        }

        private class ArrayIterator implements Iterator<T> {
            private  int curSize;

            public ArrayIterator() {
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

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null) {
                return false;
            }
            if (!(o instanceof Deque)){
                return false;
            }

            Deque<T> obj = (Deque<T>)o;
            if (obj.size() != this.size()){
                return false;
            }
            for(int i = 0; i < obj.size(); i += 1){
                T itemFromObj =  obj.get(i);
                T itemFromThis = this.get(i);
                if (!itemFromObj.equals(itemFromThis)){
                    return false;
                }
            }
            return true;
        }
}
