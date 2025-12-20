package serp.project.sales.util;

public class CalculatorUtils {

    public static long calculateTotalAmount(
            long price,
            int quantity,
            long discount,
            float tax) {
        long subtotal = price * quantity;
        long afterDiscount = subtotal - discount;
        long taxAmount = Math.round(afterDiscount * tax / 100);
        return afterDiscount + taxAmount;
    }

    public static long calculateTotalAmount(
            long price,
            int quantity,
            float discount,
            float tax) {
        long subtotal = price * quantity;
        long discountAmount = Math.round(subtotal * discount / 100);
        long afterDiscount = subtotal - discountAmount;
        long taxAmount = Math.round(afterDiscount * tax / 100);
        return afterDiscount + taxAmount;
    }

}
