package rca.restapi.year2.year2ADemo.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Security.JwtService;
import rca.restapi.year2.year2ADemo.Services.AdminService;
import rca.restapi.year2.year2ADemo.Utils.ApiResponse;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Admin>> registerAdmin(@RequestBody Admin admin) {
        Admin registeredAdmin = adminService.registerAdmin(admin);
        ApiResponse<Admin> response = new ApiResponse<>("Admin registered successfully", true, registeredAdmin);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> loginAdmin(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null) {
            ApiResponse<Map<String, String>> errorResponse = new ApiResponse<>(
                    "Email and password are required", false, null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            if (authentication.isAuthenticated()) {
                Map<String, String> tokenResponse = new HashMap<>();
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                String token = jwtService.generateToken(userDetails);
                tokenResponse.put("token", token);

                ApiResponse<Map<String, String>> response = new ApiResponse<>(
                        "Login successful", true, tokenResponse);
                return ResponseEntity.ok(response);
            } else {
                ApiResponse<Map<String, String>> errorResponse = new ApiResponse<>(
                        "Invalid credentials", false, null);
                return ResponseEntity.status(401).body(errorResponse);
            }
        } catch (UsernameNotFoundException e) {
            ApiResponse<Map<String, String>> errorResponse = new ApiResponse<>(
                    "User not found", false, null);
            return ResponseEntity.status(404).body(errorResponse);
        } catch (Exception e) {
            ApiResponse<Map<String, String>> errorResponse = new ApiResponse<>(
                    "Authentication failed: " + e.getMessage(), false, null);
            return ResponseEntity.status(401).body(errorResponse);
        }
    }
} 