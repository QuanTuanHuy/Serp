/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.enums;

public enum OrderStatus {
    CREATED,
    ASSIGNED_TO_PICKUP, // Đã gán cho tài xế/chuyến xe đi lấy hàng (tài xế chuẩn bị đi)
    PICKING_UP,         // Tài xế đang trên đường đến chỗ khách để lấy hàng
    PICKUP_FAILED,      // Lấy hàng thất bại (khách không nghe máy, hàng chưa đóng gói xong...) - Có thể cho phép lấy lại
    PICKED_UP,          // Tài xế đã lấy hàng thành công (hàng đang nằm trên xe lấy hàng)
    AT_ORIGIN_POST_OFFICE, // Đang ở bưu cục gửi
    OUTBOUND_READY_FROM_PO, // Đã vào danh sách bàn giao từ bưu cục gửi sang hub
    INBOUND_AT_ORIGIN_HUB, // Hub chặng 2 đã scan nhận đơn
    BAGGING_IN_PROGRESS, // Đang xử lý gom đơn vào bag
    BAGGED, // Đơn đã được gán vào bag
    BAG_SEALED, // Bag chứa đơn đã được niêm phong
    CANCELLED,
    LOST_OR_DAMAGED,
}
