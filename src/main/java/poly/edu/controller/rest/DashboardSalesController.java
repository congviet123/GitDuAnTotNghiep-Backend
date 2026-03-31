package poly.edu.controller.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import poly.edu.service.DashboardSalesService;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/dashboard-sales")
public class DashboardSalesController {

    @Autowired
    DashboardSalesService dashboardSalesService;

    // ===============================
    // DOANH THU
    // ===============================
    @GetMapping("/revenue/{year}")
    public ResponseEntity<?> getRevenue(@PathVariable int year) {
        return ResponseEntity.ok(
                dashboardSalesService.getRevenueData(year)
        );
    }
    @GetMapping("/revenue/{year}/{month}")
    public ResponseEntity<?> getWeeklyRevenue(
            @PathVariable int year,
            @PathVariable int month) {

        return ResponseEntity.ok(
                dashboardSalesService.getWeeklyRevenue(year, month)
        );
    }

    // ===============================
    // TOP SẢN PHẨM
    // ===============================
    @GetMapping("/products/{year}/{month}")
    public ResponseEntity<?> getProducts(
            @PathVariable int year,
            @PathVariable String month){

        return ResponseEntity.ok(
                dashboardSalesService.getTopProductData(year, month)
        );
    }

    // ===============================
    // ĐƠN HÀNG
    // ===============================
    @GetMapping("/orders/{year}/{month}")
    public ResponseEntity<?> getOrders(
            @PathVariable int year,
            @PathVariable String month){

        return ResponseEntity.ok(
                dashboardSalesService.getOrderData(year,month)
        );
    }
}