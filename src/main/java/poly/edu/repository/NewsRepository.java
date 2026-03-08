package poly.edu.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import poly.edu.entity.News;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    Page<News> findByTitleContainingIgnoreCaseOrUser_FullnameContainingIgnoreCase(String title, String fullName, Pageable pageable);

    @Query("""
    SELECT n
    FROM News n
    JOIN n.likes l
    WHERE l.user.username = :username""")
    Page<News> findLikedNewsByUsername(String username, Pageable pageable);
}
