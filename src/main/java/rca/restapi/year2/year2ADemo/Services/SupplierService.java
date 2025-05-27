package rca.restapi.year2.year2ADemo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rca.restapi.year2.year2ADemo.Repositories.SupplierRepository;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Models.Supplier.SupplierStatus;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SupplierService {
    @Autowired
    private SupplierRepository supplierRepository;
    
    public Page<Supplier> getAllSuppliers(Pageable pageable) {
        return supplierRepository.findAll(pageable);
    }
    
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }
    
    public Supplier addSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    
    public List<Supplier> findByAddressContaining(String address) {
        return supplierRepository.findByAddressContaining(address);
    }
    
    public List<Supplier> findBySupplierNameContaining(String name) {
        return supplierRepository.findBySupplierNameContaining(name, Pageable.unpaged()).getContent();
    }
    
    public Supplier updateSupplier(Long id, Supplier updatedSupplier) {
        Supplier existingSupplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        
        existingSupplier.setSupplierName(updatedSupplier.getSupplierName());
        existingSupplier.setAddress(updatedSupplier.getAddress());
        existingSupplier.setPhoneNumber(updatedSupplier.getPhoneNumber());
        existingSupplier.setEmail(updatedSupplier.getEmail());
        existingSupplier.setContactPerson(updatedSupplier.getContactPerson());
        existingSupplier.setCategory(updatedSupplier.getCategory());
        existingSupplier.setDescription(updatedSupplier.getDescription());
        
        return supplierRepository.save(existingSupplier);
    }
    
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
    
    public Supplier updateSupplierStatus(Long id, SupplierStatus status) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id));
        
        supplier.setStatus(status);
        return supplierRepository.save(supplier);
    }
    
    public List<Supplier> findByStatus(SupplierStatus status) {
        return supplierRepository.findAll().stream()
                .filter(supplier -> supplier.getStatus() == status)
                .collect(Collectors.toList());
    }
    
    public List<Supplier> findByCategory(String category) {
        return supplierRepository.findAll().stream()
                .filter(supplier -> category.equals(supplier.getCategory()))
                .collect(Collectors.toList());
    }
}
