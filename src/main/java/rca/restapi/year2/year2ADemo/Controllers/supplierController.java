package rca.restapi.year2.year2ADemo.Controllers;

import jakarta.servlet.ServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Repositories.SupplierRepository;
import rca.restapi.year2.year2ADemo.Services.SupplierService;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Models.Supplier.SupplierStatus;
import rca.restapi.year2.year2ADemo.Utils.ApiResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/suppliers")
public class supplierController {

    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private SupplierRepository supplierRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Supplier>>> getAllSuppliers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "supplierName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        // Create a Pageable instance based on the page, size, and sorting parameters
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));

        // Fetch paginated suppliers
        Page<Supplier> suppliersPage = supplierService.getAllSuppliers(pageable);

        // Wrap the response in an API response object
        ApiResponse<Page<Supplier>> response = new ApiResponse<>("Suppliers fetched successfully", true, suppliersPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> getSupplier(@PathVariable Long id, ServletRequest servletRequest) {
        Optional<Supplier> supplier = supplierService.getSupplierById(id);
        if (supplier.isPresent()) {
            ApiResponse<Supplier> response = new ApiResponse<>("Supplier found", true, supplier.get());
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Supplier> response = new ApiResponse<>("Supplier not found", false, null);
            return ResponseEntity.status(404).body(response); // Handle with 404 if not found
        }
    }
    
    @GetMapping("/search/address")
    public ResponseEntity<ApiResponse<List<Supplier>>> searchSupplierByAddress(@RequestParam String address) {
        List<Supplier> suppliers = supplierService.findByAddressContaining(address);
        if (!suppliers.isEmpty()) {
            ApiResponse<List<Supplier>> response = new ApiResponse<>("Suppliers found", true, suppliers);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<List<Supplier>> response = new ApiResponse<>("No suppliers found with that address", false, null);
            return ResponseEntity.status(404).body(response);
        }
    }
    
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponse<List<Supplier>>> searchByName(@RequestParam String name) {
        List<Supplier> results = supplierService.findBySupplierNameContaining(name);
        ApiResponse<List<Supplier>> response = new ApiResponse<>("Search results", true, results);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<Supplier>>> getSuppliersByStatus(@PathVariable String status) {
        try {
            SupplierStatus supplierStatus = SupplierStatus.valueOf(status.toUpperCase());
            List<Supplier> suppliers = supplierService.findByStatus(supplierStatus);
            
            ApiResponse<List<Supplier>> response = new ApiResponse<>(
                "Suppliers with status " + status + " fetched successfully", 
                true, 
                suppliers
            );
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<List<Supplier>> response = new ApiResponse<>(
                "Invalid supplier status: " + status, 
                false, 
                null
            );
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<Supplier>>> getSuppliersByCategory(@PathVariable String category) {
        List<Supplier> suppliers = supplierService.findByCategory(category);
        
        ApiResponse<List<Supplier>> response = new ApiResponse<>(
            "Suppliers in category " + category + " fetched successfully", 
            true, 
            suppliers
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> addSupplier(@RequestBody Supplier supplier) {
        supplierService.addSupplier(supplier);
        ApiResponse<Supplier> response = new ApiResponse<>("Supplier added successfully", true, supplier);
        return ResponseEntity.status(201).body(response);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplier(@PathVariable Long id, @RequestBody Supplier updatedSupplier) {
        try {
            Supplier supplier = supplierService.updateSupplier(id, updatedSupplier);
            return ResponseEntity.ok(new ApiResponse<>("Supplier updated successfully", true, supplier));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(new ApiResponse<>("Supplier not found", false, null));
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplierStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> statusUpdate) {
        
        String statusStr = statusUpdate.get("status");
        if (statusStr == null) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("Status field is required", false, null)
            );
        }
        
        try {
            SupplierStatus status = SupplierStatus.valueOf(statusStr.toUpperCase());
            Supplier updatedSupplier = supplierService.updateSupplierStatus(id, status);
            return ResponseEntity.ok(
                new ApiResponse<>("Supplier status updated successfully", true, updatedSupplier)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                new ApiResponse<>("Invalid status value: " + statusStr, false, null)
            );
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(
                new ApiResponse<>(e.getMessage(), false, null)
            );
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteSupplier(@PathVariable Long id) {
        Optional<Supplier> optionalSupplier = supplierRepository.findById(id);
        if (optionalSupplier.isPresent()) {
            supplierService.deleteSupplier(id);
            return ResponseEntity.ok(new ApiResponse<>("Supplier deleted successfully", true, null));
        } else {
            return ResponseEntity.status(404).body(new ApiResponse<>("Supplier not found", false, null));
        }
    }
}
