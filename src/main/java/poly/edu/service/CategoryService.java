package poly.edu.service;

import poly.edu.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAll();
    Category create(Category category);
    Category update(Category category);
    void delete(Integer id);
}