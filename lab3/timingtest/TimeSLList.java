package timingtest;
import edu.princeton.cs.algs4.Stopwatch;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        AList<Integer> Ns = new AList();
        AList<Double> times = new AList<>();
        AList<Integer> ops = new AList<>();

        // input N
        for (int i = 1000; i <= 128000; i *= 2) {
            Ns.addLast(i);
        }

        for (int i = 0; i < Ns.size(); i++) {
            int N = Ns.get(i);
//            int M = ops.get(i);
//            int temp = 0;
            SLList<Integer> slList = new SLList<>();

            // Add N items to the SLList
            for (int j = 0; j < N; j++) {
                slList.addLast(j);
            }

            // Start the timer
            Stopwatch stopwatch = new Stopwatch();

            int op = 0;
            // Perform N getLast N
            for (int j = 0; j < N; j++) {
                slList.getLast();
                op += 1;
            }

            // Check the timer
            double timeInSec = stopwatch.elapsedTime();

            ops.addLast(op);
            // Remove the
            times.addLast(timeInSec);
        }

        printTimingTable(Ns, times, ops);
    }

}
