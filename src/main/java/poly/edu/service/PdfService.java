package poly.edu.service;

import poly.edu.entity.Order;
import java.util.List;

public interface PdfService {
    
    /**
     * Tạo file PDF cho 1 hóa đơn (có tùy chọn khổ giấy)
     */
    byte[] generateInvoice(Order order, String paperSize);

    /**
     * Tạo file PDF tổng hợp cho nhiều hóa đơn (In hàng loạt)
     */
    byte[] generateBulkInvoices(List<Order> orders, String paperSize);

}