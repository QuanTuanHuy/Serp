package serp.project.payment_service.service;

import serp.project.payment_service.dto.transaction.TransactionHistoryFilterRequest;
import serp.project.payment_service.dto.transaction.TransactionHistoryResponse;
import serp.project.payment_service.dto.zalopay.*;
import serp.project.payment_service.entity.ZaloPayTransaction;
import serp.project.payment_service.entity.ZaloPayRefundTransaction;

/**
 * Service xử lý tích hợp ZaloPay
 */
public interface ZaloPayService {
    
    /**
     * Tạo đơn hàng thanh toán ZaloPay
     * 
     * @param request Thông tin đơn hàng
     * @return Response chứa thông tin đơn hàng và URL thanh toán
     */
    CreateOrderResponse createOrder(CreateOrderRequest request);
    
    /**
     * Xử lý callback từ ZaloPay khi thanh toán hoàn tất
     * 
     * @param callbackRequest Callback data từ ZaloPay
     * @return Response xác nhận đã nhận callback
     */
    String handleCallback(ZaloPayCallbackRequest callbackRequest);
    
    /**
     * Truy vấn trạng thái đơn hàng
     * 
     * @param request Request chứa app_trans_id
     * @return Response chứa trạng thái đơn hàng
     */
    QueryOrderResponse queryOrderStatus(QueryOrderRequest request);
    
    /**
     * Lấy danh sách ngân hàng được hỗ trợ từ ZaloPay
     * 
     * @return Response chứa danh sách các ngân hàng theo từng payment method
     */
    GetBankListResponse getBankList();
    
    /**
     * Thực hiện hoàn tiền giao dịch
     * 
     * @param request Request chứa thông tin hoàn tiền
     * @return Response chứa kết quả hoàn tiền
     */
    RefundResponse refundOrder(RefundRequest request);
    
    /**
     * Truy vấn trạng thái hoàn tiền
     * 
     * @param request Request chứa m_refund_id
     * @return Response chứa trạng thái hoàn tiền
     */
    QueryRefundResponse queryRefundStatus(QueryRefundRequest request);
    
    /**
     * Gửi thông báo thanh toán thành công
     * 
     * @param transaction Transaction đã thanh toán thành công
     */
    void sendPaymentSuccessNotification(ZaloPayTransaction transaction);
    
    /**
     * Gửi thông báo thanh toán thất bại
     * 
     * @param transaction Transaction thất bại
     * @param failureReason Lý do thất bại
     */
    void sendPaymentFailedNotification(ZaloPayTransaction transaction, String failureReason);
    
    /**
     * Gửi thông báo hoàn tiền thành công
     * 
     * @param refundTransaction Refund transaction đã thành công
     * @param originalTransaction Transaction gốc
     */
    void sendRefundSuccessNotification(ZaloPayRefundTransaction refundTransaction, 
                                       ZaloPayTransaction originalTransaction);
    
    /**
     * Gửi thông báo hoàn tiền thất bại
     * 
     * @param refundTransaction Refund transaction thất bại
     * @param originalTransaction Transaction gốc
     * @param failureReason Lý do thất bại
     */
    void sendRefundFailedNotification(ZaloPayRefundTransaction refundTransaction,
                                      ZaloPayTransaction originalTransaction,
                                      String failureReason);
    
    /**
     * Lấy lịch sử giao dịch với bộ lọc
     * 
     * @param filter Các tham số lọc
     * @return Response chứa danh sách giao dịch với phân trang
     */
    TransactionHistoryResponse getTransactionHistory(TransactionHistoryFilterRequest filter);
}
