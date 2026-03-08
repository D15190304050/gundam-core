public class InvoiceSummaryEngineVerifier
{
    private static boolean approx(double value, double expected)
    {
        return Math.abs(value - expected) < 0.0001;
    }

    public static void main(String[] args)
    {
        double caseA = InvoiceSummaryEngine.calculateTotal(new double[]{20.0, 30.0, 50.0}, "food", true);
        double caseB = InvoiceSummaryEngine.calculateTotal(new double[]{10.0, 40.0}, "book", false);
        boolean okA = approx(caseA, 102.6);
        boolean okB = approx(caseB, 52.0);
        if (okA && okB)
        {
            System.out.println("BEHAVIOR_OK caseA=" + caseA + " caseB=" + caseB);
        }
        else
        {
            System.out.println("BEHAVIOR_FAIL caseA=" + caseA + " caseB=" + caseB);
        }
    }
}
