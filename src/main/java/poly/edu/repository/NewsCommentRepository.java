package poly.edu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import poly.edu.entity.NewsComment;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {

    List<NewsComment> findByNewsId(Long newsId);

    @Query(value = """
            SELECT * FROM news_comment WHERE news_id = :newsId AND parent_id IS NULL ORDER BY create_date DESC OFFSET :offset ROWS FETCH NEXT :limit ROWS ONLY
            """, nativeQuery = true)
    List<NewsComment> getRootComments(@Param("newsId") Long newsId, @Param("offset") int offset,
            @Param("limit") int limit);
}
