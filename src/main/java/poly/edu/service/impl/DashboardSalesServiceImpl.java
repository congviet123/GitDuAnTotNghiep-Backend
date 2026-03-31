package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.service.DashboardSalesService;
import org.springframework.data.domain.PageRequest;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class DashboardSalesServiceImpl implements DashboardSalesService {

    @Autowired
    OrderRepository orderRepository;
    
    @Autowired
    ProductRepository productRepository;
    
    // ===============================
    // DOANH THU THEO NĂM                          
    // ===============================

    @Override
    public Map<String, Object> getRevenueData(int year) {

        List<Object[]> result = orderRepository.getMonthlyRevenue(year);

        List<BigDecimal> monthlyRevenue = new ArrayList<>();

        // tạo 12 tháng = 0
        for (int i = 0; i < 12; i++) {
            monthlyRevenue.add(BigDecimal.ZERO);
        }

        // gán dữ liệu từ DB
        for (Object[] row : result) {

            Integer month = ((Number) row[0]).intValue();

            BigDecimal total = BigDecimal.ZERO;

            if (row[1] != null) {
                total = new BigDecimal(row[1].toString());
            }

            monthlyRevenue.set(month - 1, total);
        }

        // tính tổng doanh thu năm
        BigDecimal totalRevenue = monthlyRevenue
                .stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // dự báo tháng tới
        BigDecimal forecast = calculateForecast(monthlyRevenue);

        Map<String, Object> data = new HashMap<>();

        data.put("monthlyRevenue", monthlyRevenue);
        data.put("totalRevenue", totalRevenue);
        data.put("forecast", forecast);

        return data;
    }

    // ===============================
    // DOANH THU THEO TUẦN              nấy tổng đơn đã giao hàng và hoàn tất
    // ===============================

    @Override
    public List<BigDecimal> getWeeklyRevenue(int year, int month) {

        List<Object[]> result =
                orderRepository.getWeeklyRevenue(year, month);

        List<BigDecimal> weeks = new ArrayList<>();

        // 4 tuần mặc định = 0
        for (int i = 0; i < 4; i++) {
            weeks.add(BigDecimal.ZERO);
        }

        for (Object[] row : result) {

            Integer week = ((Number) row[0]).intValue();

            BigDecimal total = BigDecimal.ZERO;

            if (row[1] != null) {
                total = new BigDecimal(row[1].toString());
            }

            int index = (week - 1) % 4;

            weeks.set(index, total);
        }

        return weeks;
    }

    // ===============================
    // FORECAST THÁNG TỚI
    // ===============================

    private BigDecimal calculateForecast(List<BigDecimal> monthly) {

        List<BigDecimal> last3Months = new ArrayList<>();

        // lấy 3 tháng gần nhất có doanh thu
        for (int i = monthly.size() - 1; i >= 0; i--) {

            if (monthly.get(i).compareTo(BigDecimal.ZERO) > 0) {
                last3Months.add(monthly.get(i));
            }

            if (last3Months.size() == 3) {
                break;
            }
        }

        if (last3Months.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = BigDecimal.ZERO;

        for (BigDecimal v : last3Months) {
            sum = sum.add(v);
        }

        return sum.divide(
                BigDecimal.valueOf(last3Months.size()),
                0,
                RoundingMode.HALF_UP
        );
    }
    
 // ===============================
 // TOP PRODUCT
 // ===============================
    @Override
    public List<Map<String, Object>> getTopProductData(int year, String month) {

        List<Map<String,Object>> result = new ArrayList<>();

        // ===============================
        // 1. TOP SẢN PHẨM BÁN CHẠY (THEO KG)
        // ===============================

        List<Object[]> best =
                orderRepository.getTopSellingProducts(year, month);

        int count = 0;
        for(Object[] row : best){
            if(count >= 3) break;
            Map<String,Object> item = new HashMap<>();
            item.put("name", row[0]);   // tên sản phẩm
            item.put("value", row[1]);  // kg bán
            item.put("type","best");
            result.add(item);
            count++;
        }

        // ===============================
        // 2. SẢN PHẨM TỒN KHO NHIỀU NHẤT
        // ===============================

        List<Object[]> stock =
        		productRepository.getTopStockProducts(year, month);

        count = 0;
        for(Object[] row : stock){
            Map<String,Object> item = new HashMap<>();
            item.put("name", row[0]);     // tên sản phẩm
            item.put("value", row[1]);    // số kg tồn
            item.put("type","slow");
            result.add(item);
            count++;
        }
        return result;
    }

 // ===============================
 // ORDER STATS
 // ===============================

    @Override
    public Map<String, Long> getOrderData(int year, String month) {

        List<Object[]> data =
                orderRepository.getOrderStatistics(year, month);

        Map<String,Long> result = new HashMap<>();

        long completed = 0;
        long refunded = 0;

        for(Object[] row : data){

            String status = row[0].toString();
            long count = ((Number)row[1]).longValue();

            if(status.equals("COMPLETED")){
                completed += count;
            }

            if(status.equals("CANCELLED_REFUNDED")){
                refunded += count;
            }
        }

        result.put("completed",completed);
        result.put("refunded",refunded);

        return result;
    }
    

}