package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import poly.edu.entity.Import;

import java.time.LocalDateTime;
import java.util.List;

public interface ImportRepository extends JpaRepository<Import, Integer> {

    @Query("""
        SELECT i FROM Import i
        WHERE
        (:keyword IS NULL OR
            CAST(i.id AS string) LIKE %:keyword% OR
            i.accountUsername LIKE %:keyword%
        )
        AND (:start IS NULL OR i.importDate >= :start)
        AND (:end IS NULL OR i.importDate <= :end)
    """)
    List<Import> search(String keyword, LocalDateTime start, LocalDateTime end);
}
