package poly.edu.service;

import poly.edu.entity.dto.VoucherDTO;
import java.util.List;
import java.util.Optional;

public interface VoucherService {
    List<VoucherDTO> findAll();
    Optional<VoucherDTO> findByCode(String code);
    VoucherDTO create(VoucherDTO voucherDTO);
    VoucherDTO update(String code, VoucherDTO voucherDTO);
    void delete(String code);
    boolean existsByCode(String code);
    List<VoucherDTO> findActiveVouchers();
    List<VoucherDTO> findPublicActiveVouchers();
}