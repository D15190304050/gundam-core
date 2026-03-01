public class BuggyCalculatorVerifier {
    public static void main(String[] args) {
        boolean first = BuggyCalculator.add(7, 5) == 12;
        boolean second = BuggyCalculator.add(3, -2) == 1;
        if (first && second) {
            System.out.println("BEHAVIOR_OK");
        } else {
            System.out.println("BEHAVIOR_FAIL add(7,5)=" + BuggyCalculator.add(7, 5)
                + " add(3,-2)=" + BuggyCalculator.add(3, -2));
        }
    }
}
