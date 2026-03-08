package poly.edu.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import poly.edu.entity.Import;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface ImportRepository extends JpaRepository<Import, Integer> {

	@Query("""
		    SELECT i FROM Import i, Supplier s
		    WHERE s.id = i.supplierId
		    AND (
		        :keyword IS NULL OR
		        LOWER(TRIM(i.accountUsername)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))
		        OR STR(i.id) LIKE CONCAT('%', TRIM(:keyword), '%')
		        OR LOWER(TRIM(s.name)) LIKE LOWER(CONCAT('%', TRIM(:keyword), '%'))
		    )
		    AND (:start IS NULL OR i.importDate >= :start)
		    AND (:end IS NULL OR i.importDate <= :end)
		""")
		List<Import> search(
		        @Param("keyword") String keyword,
		        @Param("start") LocalDateTime start,
		        @Param("end") LocalDateTime end
		);

}