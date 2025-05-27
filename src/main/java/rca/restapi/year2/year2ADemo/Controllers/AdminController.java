package rca.restapi.year2.year2ADemo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Services.AdminService;
import rca.restapi.year2.year2ADemo.Utils.ApiResponse;

import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Admin>>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sortBy,
            @RequestParam(defaultValue = "ASC") String direction) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
        Page<Admin> adminsPage = adminService.getAllAdmins(pageable);

        ApiResponse<Page<Admin>> response = new ApiResponse<>("Admins fetched successfully", true, adminsPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Admin>> getAdminById(@PathVariable Long id) {
        Optional<Admin> admin = adminService.getAdminById(id);

        if (admin.isPresent()) {
            ApiResponse<Admin> response = new ApiResponse<>("Admin found", true, admin.get());
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<Admin> response = new ApiResponse<>("Admin not found", false, null);
            return ResponseEntity.status(404).body(response);
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Admin>> createAdmin(@RequestBody Admin admin) {
        Admin savedAdmin = adminService.registerAdmin(admin);
        ApiResponse<Admin> response = new ApiResponse<>("Admin created successfully", true, savedAdmin);
        return ResponseEntity.status(201).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Admin>> updateAdmin(@PathVariable Long id, @RequestBody Admin adminDetails) {
        try {
            Admin updatedAdmin = adminService.updateAdmin(id, adminDetails);
            ApiResponse<Admin> response = new ApiResponse<>("Admin updated successfully", true, updatedAdmin);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            ApiResponse<Admin> response = new ApiResponse<>(e.getMessage(), false, null);
            return ResponseEntity.status(404).body(response);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteAdmin(@PathVariable Long id) {
        Optional<Admin> admin = adminService.getAdminById(id);
        if (admin.isPresent()) {
            adminService.deleteAdmin(id);
            ApiResponse<String> response = new ApiResponse<>("Admin deleted successfully", true, null);
            return ResponseEntity.ok(response);
        } else {
            ApiResponse<String> response = new ApiResponse<>("Admin not found", false, null);
            return ResponseEntity.status(404).body(response);
        }
    }
} 