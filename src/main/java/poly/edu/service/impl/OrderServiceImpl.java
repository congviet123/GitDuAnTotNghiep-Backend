package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import poly.edu.entity.Order;
import poly.edu.entity.OrderDetail;
import poly.edu.entity.User;
import poly.edu.entity.dto.CartItemDTO;
import poly.edu.entity.dto.OrderCreateDTO;
import poly.edu.repository.OrderDetailRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.ProductRepository;
import poly.edu.repository.UserRepository;
import poly.edu.service.OrderService;
import poly.edu.service.ShoppingCartService;
import poly.edu.entity.dto.OrderListDTO;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ShoppingCartService cartService;
    
    // --- Helper: Map Entity -> DTO ---
    private OrderListDTO mapToDto(Order order) {
        OrderListDTO dto = new OrderListDTO();
        dto.setId(order.getId());
        dto.setCreateDate(order.getCreateDate());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        
        if (order.getAccount() != null) {
            dto.setAccountFullname(order.getAccount().getFullname());
        }
        return dto;
    }

    // --- 1. ĐẶT HÀNG ---
    @Override
    @Transactional
    public Order placeOrder(String username, OrderCreateDTO orderDTO) {
        User user = userRepository.findById(username)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
        
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> details = new ArrayList<>();
        List<Integer> purchasedProductIds = new ArrayList<>();

        for (CartItemDTO item : orderDTO.getItems()) {
            totalAmount = totalAmount.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            
            OrderDetail detail = new OrderDetail();
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPrice());
            
            detail.setProduct(productRepository.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm ID " + item.getProductId() + " không còn tồn tại.")));
            
            details.add(detail);
            purchasedProductIds.add(item.getProductId());
        }

        Order order = new Order();
        order.setAccount(user);
        order.setShippingAddress(orderDTO.getShippingAddress());
        order.setNotes(orderDTO.getNotes());
        order.setTotalAmount(totalAmount);
        order.setStatus("PENDING"); 
        
        Order savedOrder = orderRepository.save(order);
        
        for (OrderDetail detail : details) {
            detail.setOrder(savedOrder);
            orderDetailRepository.save(detail);
        }
        
        // Xóa sản phẩm đã mua khỏi giỏ
        cartService.removeItems(purchasedProductIds);
        
        return savedOrder;
    }

    // --- 2. LỊCH SỬ ĐƠN HÀNG (USER) ---
    @Override
    @Transactional(readOnly = true) 
    public List<OrderListDTO> findOrdersByUsername(String username) {
        List<Order> orders = orderRepository.findByAccountUsernameOrderByCreateDateDesc(username);
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    // --- 3. CHI TIẾT ĐƠN HÀNG ---
    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Integer orderId) {
        // Sử dụng query optimized trong Repository
        return orderRepository.findByIdWithDetails(orderId);
    }
    
    // --- 4. DANH SÁCH ĐƠN HÀNG (ADMIN) ---
    @Override
    @Transactional(readOnly = true)
    public List<OrderListDTO> findAllOrders() { 
        List<Order> orders = orderRepository.findAllOrdersSimple(); 
        return orders.stream().map(this::mapToDto).collect(Collectors.toList());
    }
    
    // --- 5. CẬP NHẬT TRẠNG THÁI ---
    @Override
    @Transactional
    public Order updateStatus(Integer orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại."));
        
        if (newStatus == null || newStatus.trim().isEmpty()) {
            throw new RuntimeException("Trạng thái mới không hợp lệ.");
        }
        
        order.setStatus(newStatus.toUpperCase().trim()); 
        return orderRepository.save(order);
    }

    // --- 6. BÁO CÁO DOANH THU (DASHBOARD) ---
    @Override
    public List<Double> getMonthlyRevenue(Integer year) {
        List<Double> revenueList = new ArrayList<>(Collections.nCopies(12, 0.0));
        List<Object[]> results = orderRepository.getMonthlyRevenue(year);

        for (Object[] row : results) {
            int month = (int) row[0];
            double total = (double) row[1];
            if (month >= 1 && month <= 12) {
                revenueList.set(month - 1, total);
            }
        }
        return revenueList;
    }
}