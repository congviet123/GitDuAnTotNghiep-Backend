package poly.edu.service;

import poly.edu.entity.Import;
import poly.edu.entity.dto.ImportDTO;

import java.time.LocalDate;
import java.util.List;

import jakarta.transaction.Transactional;

@Transactional
public interface ImportService {

    Import create(ImportDTO dto);

    List<Import> search(String keyword, LocalDate start, LocalDate end);

    Import findById(Integer id);
}
