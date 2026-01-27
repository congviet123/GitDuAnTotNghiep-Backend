package poly.edu.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import poly.edu.entity.Category;
import poly.edu.repository.CategoryRepository;
import poly.edu.service.CategoryService;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Category create(Category category) {
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Tên danh mục '" + category.getName() + "' đã tồn tại.");
        }
        return categoryRepository.save(category);
    }

    @Override
    public Category update(Category category) {
        // Kiểm tra tồn tại
        Category existingCategory = categoryRepository.findById(category.getId())
                .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại."));

        // Kiểm tra trùng tên (Nếu tên mới khác tên cũ mà lại trùng trong DB)
        if (!existingCategory.getName().equals(category.getName()) 
                && categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Tên danh mục '" + category.getName() + "' đã tồn tại.");
        }
        
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Integer id) {
        if (!categoryRepository.existsById(id)) {
            throw new RuntimeException("Danh mục không tồn tại.");
        }
        try {
            categoryRepository.deleteById(id);
        } catch (Exception e) {
            // Bắt lỗi ràng buộc khóa ngoại (Nếu danh mục đang chứa sản phẩm)
            throw new RuntimeException("Không thể xóa danh mục này vì đang chứa sản phẩm.");
        }
    }
}