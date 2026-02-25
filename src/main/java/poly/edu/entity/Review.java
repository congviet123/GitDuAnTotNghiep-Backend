package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "Review")
@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class Review implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    //  Xóa Product và thay bằng OrderDetail.
    // Database thiết kế UNIQUE cho order_detail_id nên dùng @OneToOne là chuẩn xác nhất.
    @OneToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "order_detail_id", nullable = false, unique = true)
    @JsonIgnore // BẮT BUỘC: Ngăn chặn vòng lặp JSON
    private OrderDetail orderDetail;

    // Mối quan hệ: mapping tới username (không phải user_id)
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "username", nullable = false)
    @JsonIgnore // BẮT BUỘC: Ngăn chặn vòng lặp JSON
    private User user; 

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment; 

    @Column(nullable = false)
    private Integer rating; 

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "review_date") 
    private Date reviewDate = new Date();
    
    // Trường Transient để VueJS hiển thị tên người đánh giá
    // Nhờ có getter này, Spring Boot (Jackson) sẽ tự động tạo ra một trường 
    // tên là "reviewerFullname" trong chuỗi JSON trả về cho Frontend.
    @Transient
    public String getReviewerFullname() {
        // Cần đảm bảo user được tải đầy đủ trong Transaction
        return user != null ? user.getFullname() : "Khách hàng ẩn danh";
    }

    // TÙY CHỌN: Có thể thêm getter để đảm bảo Jackson thấy trường comment
    public String getComment() {
        return comment;
    }
}