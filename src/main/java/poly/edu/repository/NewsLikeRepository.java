package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import poly.edu.entity.NewsLike;

@Repository
public interface NewsLikeRepository extends JpaRepository<NewsLike, Long> {

    @Query(value = """
            IF EXISTS (SELECT 1 FROM News_Like WHERE username = :username AND news_id = :newsId) SELECT CAST(1 AS BIT) ELSE SELECT CAST(0 AS BIT)
            """, nativeQuery = true)
    boolean hasUserLikedNews(@Param("username") String username, @Param("newsId") Long newsId);

    @Modifying
    @Query(value = "DELETE FROM News_Like WHERE username = :username AND news_id = :newsId", nativeQuery = true)
    void deleteByUsernameAndNewsId(@Param("username") String username, @Param("newsId") Long newsId);

    boolean existsByNews_IdAndUser_Username(Long newsId, String username);
}
