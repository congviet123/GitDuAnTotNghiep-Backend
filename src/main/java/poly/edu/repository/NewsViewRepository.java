package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import poly.edu.entity.NewsView;

@Repository
public interface NewsViewRepository extends JpaRepository<NewsView, Long> {

}
