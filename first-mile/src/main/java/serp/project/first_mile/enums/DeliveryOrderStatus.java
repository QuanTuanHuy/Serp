/*
Author: SERP Project
Description: Part of Serp Project
*/

package serp.project.first_mile.enums;

public enum DeliveryOrderStatus {
    PENDING,         // Chờ giao
    OUT_FOR_DELIVERY,// Đang giao trong batch này
    DELIVERED,       // Giao thành công
    FAILED,          // Thất bại lần này
    RESCHEDULED,     // Được gán sang manifest khác để thử lại
    RETURNED         // Hoàn trả cho người gửi
}
