package poly.edu.repository;

import poly.edu.entity.StaticPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StaticPageRepository extends JpaRepository<StaticPage, Integer> {
    Optional<StaticPage> findBySlug(String slug);
}