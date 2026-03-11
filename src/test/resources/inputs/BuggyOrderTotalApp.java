import java.util.ArrayList;
import java.util.List;

public class BuggyOrderTotalApp {
    public static void main(String[] args) {
        Order order = sampleOrder();
        int total = calculateOrderTotal(order);

        if (total == 10792) {
            System.out.println("BEHAVIOR_OK total=" + total);
        } else {
            System.out.println("BEHAVIOR_BAD total=" + total);
        }
    }

    public static int calculateOrderTotal(Order order) {
        int subtotal = calculateSubtotal(order.items);
        int shipping = calculateShipping(order, subtotal);
        int discount = calculateDiscount(order.customerTier, subtotal);
        int taxBase = subtotal + shipping - discount;
        int tax = calculateTax(taxBase, order.regionCode);
        int fee = calculateServiceFee(taxBase);
        return subtotal + shipping - discount + tax + fee;
    }

    private static int calculateSubtotal(List<LineItem> items) {
        int subtotal = 0;
        for (LineItem item : items) {
            int qty = BuggyCalcService.normalizeQuantity(item.quantity);
            int price = BuggyCalcService.normalizePrice(item.unitPriceInCents);
            int lineTotal = BuggyCalcService.multiply(qty, price);
            subtotal = BuggyCalcService.add(subtotal, lineTotal);
        }
        return subtotal;
    }

    private static int calculateShipping(Order order, int subtotal) {
        if (subtotal >= 10_000) {
            return 0;
        }
        int base = order.expeditedShipping ? 1_299 : 699;
        if ("AK".equals(order.regionCode) || "HI".equals(order.regionCode)) {
            base += 500;
        }
        return base;
    }

    private static int calculateDiscount(String customerTier, int subtotal) {
        int rate;
        if ("GOLD".equals(customerTier)) {
            rate = 12;
        } else if ("SILVER".equals(customerTier)) {
            rate = 17;
        } else {
            rate = 0;
        }

        if (subtotal > 20_000) {
            rate += 2;
        }
        return BuggyCalcService.percentage(subtotal, rate);
    }

    private static int calculateTax(int taxBase, String regionCode) {
        int rate;
        switch (regionCode) {
            case "CA" -> rate = 8;
            case "NY" -> rate = 9;
            case "TX" -> rate = 7;
            default -> rate = 5;
        }
        return BuggyCalcService.percentage(taxBase, rate);
    }

    private static int calculateServiceFee(int taxBase) {
        if (taxBase < 1_000) {
            return 0;
        }
        return BuggyCalcService.percentage(taxBase, 1);
    }

    private static Order sampleOrder() {
        List<LineItem> items = new ArrayList<>();
        items.add(new LineItem("NOTEBOOK", 1299, 2));
        items.add(new LineItem("PEN_SET", 899, 3));
        items.add(new LineItem("BAG", 4599, 1));

        Order order = new Order();
        order.customerTier = "SILVER";
        order.regionCode = "CA";
        order.expeditedShipping = false;
        order.items = items;
        return order;
    }

    static class Order {
        String customerTier;
        String regionCode;
        boolean expeditedShipping;
        List<LineItem> items;
    }

    static class LineItem {
        String sku;
        int unitPriceInCents;
        int quantity;

        LineItem(String sku, int unitPriceInCents, int quantity) {
            this.sku = sku;
            this.unitPriceInCents = unitPriceInCents;
            this.quantity = quantity;
        }
    }

    public static String explain(Order order) {
        int subtotal = calculateSubtotal(order.items);
        int shipping = calculateShipping(order, subtotal);
        int discount = calculateDiscount(order.customerTier, subtotal);
        int taxBase = subtotal + shipping - discount;
        int tax = calculateTax(taxBase, order.regionCode);
        int fee = calculateServiceFee(taxBase);
        int total = subtotal + shipping - discount + tax + fee;

        StringBuilder builder = new StringBuilder();
        builder.append("subtotal=").append(subtotal).append('\n');
        builder.append("shipping=").append(shipping).append('\n');
        builder.append("discount=").append(discount).append('\n');
        builder.append("tax=").append(tax).append('\n');
        builder.append("fee=").append(fee).append('\n');
        builder.append("total=").append(total);
        return builder.toString();
    }
}
