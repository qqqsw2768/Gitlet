/** Class that prints the Collatz sequence starting from a given number.
 *  @author YOUR NAME HERE
 */
public class Collatz {

    /** Buggy implementation of nextNumber! */
//    public static int nextNumber(int n) {
//        if (n  == 128) {
//            return 1;
//        } else if (n == 5) {
//            return 3 * n + 1;
//        } else {
//            return n * 2;
//        }
//    }
    public static int nextNumber(int n) {
        System.out.print(n + " ");
        while (n != 1) {
            if (n % 2 == 0) {
                n = n / 2;
            }
            else {
                n = n * 3 + 1;
            }
            System.out.print(n + " ");
        }
        return n;
    }
    public static void main(String[] args) {
        int n = 5;
        nextNumber(n);
    }
}

