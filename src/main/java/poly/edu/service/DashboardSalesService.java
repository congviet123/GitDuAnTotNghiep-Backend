package poly.edu.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardSalesService {

    // ===============================
    // DOANH THU
    // ===============================
    Map<String, Object> getRevenueData(int year);

    List<BigDecimal> getWeeklyRevenue(int year,int month);

    // ===============================
    // SẢN PHẨM
    // ===============================
//    List<Map<String, Object>> getTopProductData();
    List<Map<String,Object>> getTopProductData(int year, String month);

    // ===============================
    // ĐƠN HÀNG
    // ===============================
    Map<String, Long> getOrderData(int year, String month);

	
}