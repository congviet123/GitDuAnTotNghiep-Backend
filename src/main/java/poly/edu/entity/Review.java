package poly.edu.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor; // BỔ SUNG
import java.io.Serializable;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore; // BỔ SUNG

@Entity
@Table(name = "Review")
@Data
@NoArgsConstructor // BỔ SUNG
@AllArgsConstructor // BỔ SUNG
public class Review implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Mối quan hệ: Ngăn chặn vòng lặp JSON và lỗi Lazy Loading
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "product_id", nullable = false)
    @JsonIgnore // <--- BẮT BUỘC
    private Product product;

    // Mối quan hệ: mapping tới username (không phải user_id)
    @ManyToOne(fetch = FetchType.LAZY) 
    @JoinColumn(name = "username", nullable = false)
    @JsonIgnore // <--- BẮT BUỘC
    private User user; 

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String comment; 

    @Column(nullable = false)
    private Integer rating; 

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "review_date") // Đổi tên cột cho phù hợp
    private Date reviewDate = new Date();
    
    // BỔ SUNG: Trường Transient để VueJS hiển thị tên người đánh giá
    @Transient
    public String getReviewerFullname() {
        // Cần đảm bảo user được tải đầy đủ trong Transaction
        return user != null ? user.getFullname() : "Người dùng ẩn danh";
    }

    // TÙY CHỌN: Có thể thêm getter để đảm bảo Jackson thấy trường comment
    public String getComment() {
        return comment;
    }
}