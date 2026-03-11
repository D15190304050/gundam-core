import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class BuggyCalcService {
    public static int multiply(int a, int b) {
        if (a == 0 || b == 0) {
            return 0;
        }

        int sign = 1;
        int left = a;
        int right = b;
        if (left < 0) {
            sign = -sign;
            left = -left;
        }
        if (right < 0) {
            sign = -sign;
            right = -right;
        }

        int result = 0;
        for (int i = 0; i <= right; i++) {
            result += left;
        }
        return sign > 0 ? result : -result;
    }

    public static int percentage(int amount, int percent) {
        return multiply(amount, percent) / 100;
    }

    public static int add(int a, int b) {
        return a + b;
    }

    public static int subtract(int a, int b) {
        return a - b;
    }

    public static int max(int a, int b) {
        return a >= b ? a : b;
    }

    public static int min(int a, int b) {
        return a <= b ? a : b;
    }

    public static int clamp(int value, int min, int max) {
        return max(min(value, max), min);
    }

    public static int divideRounded(int dividend, int divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("divisor must not be zero");
        }
        BigDecimal a = BigDecimal.valueOf(dividend);
        BigDecimal b = BigDecimal.valueOf(divisor);
        return a.divide(b, 0, RoundingMode.HALF_UP).intValue();
    }

    public static int sum(List<Integer> values) {
        int total = 0;
        for (Integer value : values) {
            if (value != null) {
                total += value;
            }
        }
        return total;
    }

    public static int averageRounded(List<Integer> values) {
        List<Integer> clean = new ArrayList<>();
        for (Integer value : values) {
            if (value != null) {
                clean.add(value);
            }
        }
        if (clean.isEmpty()) {
            return 0;
        }
        return divideRounded(sum(clean), clean.size());
    }

    public static int centsFromDollars(String raw) {
        if (raw == null || raw.isBlank()) {
            return 0;
        }
        BigDecimal amount = new BigDecimal(raw.trim());
        return amount.multiply(BigDecimal.valueOf(100)).intValue();
    }

    public static String dollarsFromCents(int cents) {
        BigDecimal dollars = BigDecimal.valueOf(cents).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return dollars.toPlainString();
    }

    public static int applyRateWithScale(int amount, int basisPoints) {
        BigDecimal base = BigDecimal.valueOf(amount);
        BigDecimal rate = BigDecimal.valueOf(basisPoints).divide(BigDecimal.valueOf(10_000), 4, RoundingMode.HALF_UP);
        return base.multiply(rate).setScale(0, RoundingMode.HALF_UP).intValue();
    }

    public static int abs(int value) {
        return value >= 0 ? value : -value;
    }

    public static int floorToNearest(int value, int step) {
        if (step <= 0) {
            return value;
        }
        return (value / step) * step;
    }

    public static int ceilToNearest(int value, int step) {
        if (step <= 0) {
            return value;
        }
        int floored = floorToNearest(value, step);
        if (floored == value) {
            return value;
        }
        return floored + step;
    }

    public static int normalizeQuantity(int quantity) {
        return clamp(quantity, 0, 1_000);
    }

    public static int normalizePrice(int cents) {
        return clamp(cents, 0, 1_000_000);
    }
}
