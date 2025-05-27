package rca.restapi.year2.year2ADemo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Services.AuthService;
import rca.restapi.year2.year2ADemo.Utils.ApiResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<Admin>> registerAdmin(@RequestBody Admin admin) {
        try {
            Admin registeredAdmin = authService.registerAdmin(admin);
            ApiResponse<Admin> response = new ApiResponse<>("Admin registered successfully", true, registeredAdmin);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            ApiResponse<Admin> respongit se = new ApiResponse<>(e.getMessage(), false, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/supplier/register")
    public ResponseEntity<ApiResponse<Supplier>> registerSupplier(@RequestBody Supplier supplier) {
        try {
            Supplier registeredSupplier = authService.registerSupplier(supplier);
            ApiResponse<Supplier> response = new ApiResponse<>("Supplier registered successfully", true, registeredSupplier);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            ApiResponse<Supplier> response = new ApiResponse<>(e.getMessage(), false, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    "Email and password are required", false, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Map<String, Object> authResponse = authService.authenticate(email, password);
            ApiResponse<Map<String, Object>> response = new ApiResponse<>(
                    "Login successful", true, authResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Map<String, Object>> errorResponse = new ApiResponse<>(
                    "Authentication failed: " + e.getMessage(), false, null);
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
} 