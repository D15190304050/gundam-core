public class InvoiceSummaryEngine {
    public static double calculateTotal(double[] items, String category, boolean isVip) {
        double subtotal = 0.0;
        for (int i = 1; i < items.length; i++) {
            subtotal += items[i];
        }

        if (isVip) {
            subtotal = subtotal * 0.95;
        }

        double tax = subtotal * taxRate(category);
        return round2(subtotal + tax);
    }

    private static double taxRate(String category) {
        if ("food".equalsIgnoreCase(category)) {
            return 0.18;
        }
        if ("book".equalsIgnoreCase(category)) {
            return 0.04;
        }
        return 0.12;
    }

    private static double round2(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    public static void main(String[] args) {
        double[] items = new double[] {20.0, 30.0, 50.0};
        double total = calculateTotal(items, "food", true);
        System.out.println("TOTAL=" + total);
    }
}
