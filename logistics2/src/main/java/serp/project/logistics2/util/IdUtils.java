package serp.project.logistics2.util;

import java.util.UUID;

public class IdUtils {

    public static String generateProductId() {
        return generateIdWithPrefix("PRD");
    }

    public static String generateOrderId() {
        return generateIdWithPrefix("ORD");
    }

    public static String generateOrderItemId() {
        return generateIdWithPrefix("ORI");
    }

    public static String generateCustomerId() {
        return generateIdWithPrefix("CUS");
    }

    public static String generateCategoryId() {
        return generateIdWithPrefix("CAT");
    }

    public static String generateInvoiceId() {
        return generateIdWithPrefix("INV");
    }

    public static String generateSupplierId() {
        return generateIdWithPrefix("SUP");
    }

    public static String generateAddressId() {
        return generateIdWithPrefix("ADR");
    }

    public static String generateDeliveryBillId() {
        return generateIdWithPrefix("DBI");
    }

    public static String generateFacilityId() {
        return generateIdWithPrefix("FAC");
    }

    public static String generateInventoryItemDetailId() {
        return generateIdWithPrefix("IID");
    }

    public static String generateInventoryItemId() {
        return generateIdWithPrefix("INI");
    }

    public static String generateInvoiceItemId() {
        return generateIdWithPrefix("IVI");
    }

    public static String generateOrderItemBillingId() {
        return generateIdWithPrefix("OIB");
    }

    public static String generatePriceId() {
        return generateIdWithPrefix("PRI");
    }

    public static String generateShipmentId() {
        return generateIdWithPrefix("SHP");
    }

    public static String generateOutboundShipmentId() {
        return generateIdWithPrefix("OBS");
    }

    public static String generateOutboundShipmentItemId() {
        return generateIdWithPrefix("OSI");
    }

    public static String generateDeliverySlipId() {
        return generateIdWithPrefix("DLS");
    }

    public static String generateDeliveryItemId() {
        return generateIdWithPrefix("DLI");
    }

    public static String generateDeliveryPlanId() {
        return generateIdWithPrefix("DLP");
    }

    public static String generateVehicleShipperId() {
        return generateIdWithPrefix("VSP");
    }

    public static String generateVehicleId() {
        return generateIdWithPrefix("VEH");
    }

    public static String generateRouteId() {
        return generateIdWithPrefix("ROU");
    }

    public static String generateRouteStopId() {
        return generateIdWithPrefix("RSP");
    }

    public static String generateIdWithPrefix(String prefix) {
        String uuid = UUID.randomUUID().toString().replace("-", "").toUpperCase();
        return prefix + uuid;
    }

}
