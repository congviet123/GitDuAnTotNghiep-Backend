package poly.edu.service;

import poly.edu.entity.Address;
import java.util.List;

public interface AddressService {
    List<Address> findAllByUsername(String username);
    Address create(String username, Address address);
    Address update(String username, Long id, Address address);
    void delete(String username, Long id);
    Address findById(Long id);
}