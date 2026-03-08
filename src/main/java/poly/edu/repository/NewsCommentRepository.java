package poly.edu.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import poly.edu.entity.NewsComment;

@Repository
public interface NewsCommentRepository extends JpaRepository<NewsComment, Long> {

    List<NewsComment> findByNewsId(Long newsId);

    @EntityGraph(attributePaths = { "user" })
    Page<NewsComment> findAllByNewsId(Long newsId, Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<NewsComment> findByNewsIdAndParentIsNullAndIsVisibleTrue(Long newsId, Pageable pageable);

    @EntityGraph(attributePaths = { "user" })
    Page<NewsComment> findByParentIdAndIsVisibleTrue(Long parentId, Pageable pageable);
}
