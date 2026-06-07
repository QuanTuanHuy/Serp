/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.enums;

public enum OrderStatus {
    CREATED,
    ASSIGNED_TO_PICKUP, // Đã gán cho tài xế/chuyến xe đi lấy hàng (tài xế chuẩn bị đi)
    PICKING_UP,         // Tài xế đang trên đường đến chỗ khách để lấy hàng
    PICKUP_FAILED,      // Lấy hàng thất bại (khách không nghe máy, hàng chưa đóng gói xong...) - Có thể cho phép lấy lại
    PICKED_UP,          // Tài xế đã lấy hàng thành công (hàng đang nằm trên xe lấy hàng)
    PENDING_ORIGIN_POST_OFFICE_INBOUND, // Xe đã về bưu cục, chờ trưởng bưu cục/admin quét nhập kho
    AT_ORIGIN_POST_OFFICE, // Đang ở bưu cục gửi (đã xác nhận nhập kho)
    OUTBOUND_READY_FROM_PO, // Đã vào danh sách bàn giao từ bưu cục gửi sang hub
    INBOUND_AT_ORIGIN_HUB, // Hub chặng 2 đã scan nhận đơn
    BAGGING_IN_PROGRESS, // Đang xử lý gom đơn vào bag
    BAGGED, // Đơn đã được gán vào bag
    BAG_SEALED, // Bag chứa đơn đã được niêm phong
    BAG_IN_TRANSIT,
    INBOUND_AT_DESTINATION_HUB,
    INBOUND_AT_DESTINATION_POST_OFFICE,
    CANCELLED,
    LOST_OR_DAMAGED,
}
