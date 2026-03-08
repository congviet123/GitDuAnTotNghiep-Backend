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

    @Query(value = """
                SELECT * FROM news WHERE title COLLATE Vietnamese_CI_AI LIKE %:title%
            """, nativeQuery = true)
    Page<News> searchdByTitle(@Param(value = "title") String title, Pageable pageable);
}
