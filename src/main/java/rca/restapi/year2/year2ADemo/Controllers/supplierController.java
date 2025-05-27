package rca.restapi.year2.year2ADemo.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Exception.ResourceNotFoundException;
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
@Tag(name = "Suppliers", description = "Suppliers management API")
@SecurityRequirement(name = "bearerAuth")
public class supplierController {

    @Autowired
    private SupplierService supplierService;
    
    @Autowired
    private SupplierRepository supplierRepository;

    @Operation(summary = "Get all suppliers", description = "Returns a paginated list of all suppliers")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "List of suppliers retrieved",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<Supplier>>> getAllSuppliers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Results per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Supplier> suppliers = supplierService.getAllSuppliers(pageable);
        
        ApiResponse<Page<Supplier>> response = new ApiResponse<>(
                "Retrieved suppliers successfully", true, suppliers);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get supplier by ID", description = "Returns a supplier by their ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Supplier found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Supplier not found",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(
            @Parameter(description = "Supplier ID", required = true) @PathVariable Long id) {
        
        try {
            Supplier supplier = supplierService.getSupplierById(id);
            ApiResponse<Supplier> response = new ApiResponse<>(
                    "Supplier retrieved successfully", true, supplier);
            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            ApiResponse<Supplier> response = new ApiResponse<>(
                    e.getMessage(), false, null);
            return ResponseEntity.status(404).body(response);
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
    
    @Operation(summary = "Search suppliers by name", description = "Finds suppliers by partial name match")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Search results",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        )
    })
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<Supplier>>> searchSuppliers(
            @Parameter(description = "Name to search for") @RequestParam String name) {
        
        List<Supplier> suppliers = supplierService.findBySupplierNameContaining(name);
        
        ApiResponse<List<Supplier>> response = new ApiResponse<>(
                "Search results", true, suppliers);
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

    @Operation(summary = "Add a new supplier", description = "Creates a new supplier record")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Supplier created successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        )
    })
    @PostMapping
    public ResponseEntity<ApiResponse<Supplier>> addSupplier(
            @Parameter(description = "Supplier object", required = true) 
            @Valid @RequestBody Supplier supplier) {
        
        Supplier newSupplier = supplierService.addSupplier(supplier);
        
        ApiResponse<Supplier> response = new ApiResponse<>(
                "Supplier added successfully", true, newSupplier);
        return ResponseEntity.status(201).body(response);
    }
    
    @Operation(summary = "Update supplier", description = "Updates an existing supplier by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Supplier updated successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Supplier not found",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplier(
            @Parameter(description = "Supplier ID", required = true) @PathVariable Long id,
            @Parameter(description = "Updated supplier details", required = true) 
            @Valid @RequestBody Supplier supplier) {
        
        try {
            Supplier updatedSupplier = supplierService.updateSupplier(id, supplier);
            
            ApiResponse<Supplier> response = new ApiResponse<>(
                    "Supplier updated successfully", true, updatedSupplier);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Supplier> response = new ApiResponse<>(
                    e.getMessage(), false, null);
            return ResponseEntity.status(404).body(response);
        }
    }
    
    @Operation(summary = "Update supplier status", description = "Changes the status of a supplier (ACTIVE, INACTIVE, BLACKLISTED)")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Status updated successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Supplier not found",
            content = @Content
        )
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Supplier>> updateSupplierStatus(
            @Parameter(description = "Supplier ID", required = true) @PathVariable Long id,
            @Parameter(description = "New status", required = true) @RequestBody Map<String, String> status) {
        
        try {
            String statusValue = status.get("status");
            SupplierStatus supplierStatus = SupplierStatus.valueOf(statusValue);
            
            Supplier updatedSupplier = supplierService.updateSupplierStatus(id, supplierStatus);
            
            ApiResponse<Supplier> response = new ApiResponse<>(
                    "Supplier status updated successfully", true, updatedSupplier);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ApiResponse<Supplier> response = new ApiResponse<>(
                    "Invalid status value. Valid values are: ACTIVE, INACTIVE, BLACKLISTED", false, null);
            return ResponseEntity.badRequest().body(response);
        } catch (RuntimeException e) {
            ApiResponse<Supplier> response = new ApiResponse<>(
                    e.getMessage(), false, null);
            return ResponseEntity.status(404).body(response);
        }
    }
    
    @Operation(summary = "Delete supplier", description = "Deletes a supplier by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Supplier deleted successfully",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Supplier not found",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(
            @Parameter(description = "Supplier ID", required = true) @PathVariable Long id) {
        
        try {
            supplierService.deleteSupplier(id);
            
            ApiResponse<Void> response = new ApiResponse<>(
                    "Supplier deleted successfully", true, null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Void> response = new ApiResponse<>(
                    "Supplier not found", false, null);
            return ResponseEntity.status(404).body(response);
        }
    }
}
