package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.edu.entity.NewsShare;

@Repository
public interface NewsShareRepository extends JpaRepository<NewsShare, Long> {

}
