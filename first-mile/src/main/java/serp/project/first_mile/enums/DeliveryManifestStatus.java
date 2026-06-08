/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.enums;

public enum DeliveryManifestStatus {
    CREATED,          // Đã tạo, chưa xuất phát
    IN_PROGRESS,      // Courier đang giao hàng
    COMPLETED,        // Tất cả đơn đã xử lý (giao/thất bại)
    CANCELLED
}
