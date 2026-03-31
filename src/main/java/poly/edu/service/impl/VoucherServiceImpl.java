package poly.edu.service.impl;

import poly.edu.entity.Voucher;
import poly.edu.entity.dto.VoucherDTO;
import poly.edu.repository.VoucherRepository;
import poly.edu.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class VoucherServiceImpl implements VoucherService {
    
    @Autowired
    private VoucherRepository voucherRepository;
    
    private VoucherDTO convertToDTO(Voucher voucher) {
        VoucherDTO dto = new VoucherDTO();
        dto.setCode(voucher.getCode());
        dto.setName(voucher.getName());
        dto.setDescription(voucher.getDescription());
        
        // Xác định type và value
        if (voucher.getDiscountPercent() != null && voucher.getDiscountPercent() > 0) {
            dto.setType("percentage");
            dto.setValue(BigDecimal.valueOf(voucher.getDiscountPercent()));
        } else {
            dto.setType("fixed");
            dto.setValue(voucher.getDiscountAmount());
        }
        
        dto.setMinOrderValue(voucher.getMinCondition());
        
        if (voucher.getStartDate() != null) {
            dto.setStartDate(voucher.getStartDate().toLocalDate());
        }
        if (voucher.getEndDate() != null) {
            dto.setExpiryDate(voucher.getEndDate().toLocalDate());
        }
        
        dto.setUsageLimit(voucher.getQuantity());
        dto.setUsedCount(voucher.getUsedCount());
        dto.setPerUserLimit(voucher.getPerUserLimit());
        dto.setStatus(voucher.getActive() ? "published" : "draft");
        dto.setVisibility(voucher.getVisibility() ? "public" : "private");
        
        if (voucher.getCreatedAt() != null) {
            dto.setCreatedAt(voucher.getCreatedAt().toLocalDate());
        }
        
        return dto;
    }
    
    private Voucher convertToEntity(VoucherDTO dto) {
        Voucher voucher = new Voucher();
        voucher.setCode(dto.getCode().toUpperCase());
        voucher.setName(dto.getName());
        voucher.setDescription(dto.getDescription());
        
        if ("percentage".equals(dto.getType())) {
            voucher.setDiscountPercent(dto.getValue().intValue());
            voucher.setDiscountAmount(BigDecimal.ZERO);
        } else {
            voucher.setDiscountPercent(0);
            voucher.setDiscountAmount(dto.getValue());
        }
        
        voucher.setMinCondition(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : BigDecimal.ZERO);
        
        if (dto.getStartDate() != null) {
            voucher.setStartDate(dto.getStartDate().atStartOfDay());
        }
        if (dto.getExpiryDate() != null) {
            voucher.setEndDate(dto.getExpiryDate().atStartOfDay());
        }
        
        voucher.setQuantity(dto.getUsageLimit() != null ? dto.getUsageLimit() : 0);
        voucher.setUsedCount(dto.getUsedCount() != null ? dto.getUsedCount() : 0);
        voucher.setPerUserLimit(dto.getPerUserLimit());
        voucher.setActive("published".equals(dto.getStatus()));
        voucher.setVisibility("public".equals(dto.getVisibility()));
        
        return voucher;
    }
    
    @Override
    public List<VoucherDTO> findAll() {
        return voucherRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<VoucherDTO> findByCode(String code) {
        return voucherRepository.findByCode(code.toUpperCase())
                .map(this::convertToDTO);
    }
    
    @Override
    public VoucherDTO create(VoucherDTO dto) {
        String code = dto.getCode().toUpperCase();
        if (voucherRepository.existsByCode(code)) {
            throw new RuntimeException("Mã voucher đã tồn tại: " + code);
        }
        dto.setCode(code);
        Voucher voucher = convertToEntity(dto);
        voucher = voucherRepository.save(voucher);
        return convertToDTO(voucher);
    }
    
    @Override
    public VoucherDTO update(String code, VoucherDTO dto) {
        Voucher existing = voucherRepository.findById(code.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher: " + code));
        
        existing.setName(dto.getName());
        existing.setDescription(dto.getDescription());
        
        if ("percentage".equals(dto.getType())) {
            existing.setDiscountPercent(dto.getValue().intValue());
            existing.setDiscountAmount(BigDecimal.ZERO);
        } else {
            existing.setDiscountPercent(0);
            existing.setDiscountAmount(dto.getValue());
        }
        
        existing.setMinCondition(dto.getMinOrderValue() != null ? dto.getMinOrderValue() : BigDecimal.ZERO);
        
        if (dto.getStartDate() != null) {
            existing.setStartDate(dto.getStartDate().atStartOfDay());
        }
        if (dto.getExpiryDate() != null) {
            existing.setEndDate(dto.getExpiryDate().atStartOfDay());
        }
        
        existing.setQuantity(dto.getUsageLimit() != null ? dto.getUsageLimit() : 0);
        existing.setPerUserLimit(dto.getPerUserLimit());
        existing.setActive("published".equals(dto.getStatus()));
        existing.setVisibility("public".equals(dto.getVisibility()));
        
        existing = voucherRepository.save(existing);
        return convertToDTO(existing);
    }
    
    @Override
    public void delete(String code) {
        if (!voucherRepository.existsById(code.toUpperCase())) {
            throw new RuntimeException("Không tìm thấy voucher: " + code);
        }
        voucherRepository.deleteById(code.toUpperCase());
    }
    
    @Override
    public boolean existsByCode(String code) {
        return voucherRepository.existsByCode(code.toUpperCase());
    }
    
    @Override
    public List<VoucherDTO> findActiveVouchers() {
        return voucherRepository.findActiveVouchers(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<VoucherDTO> findPublicActiveVouchers() {
        return voucherRepository.findPublicActiveVouchers(LocalDateTime.now()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}