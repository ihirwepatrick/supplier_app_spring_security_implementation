package rca.restapi.year2.year2ADemo.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Services.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Auth API for admin and supplier login/registration")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(summary = "Register a new admin", description = "Creates a new admin account")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Admin registered successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Admin.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid input or email already exists",
            content = @Content
        )
    })
    @PostMapping("/admin/register")
    public ResponseEntity<rca.restapi.year2.year2ADemo.Utils.ApiResponse<Admin>> registerAdmin(@Valid @RequestBody Admin admin) {
        try {
            Admin registeredAdmin = authService.registerAdmin(admin);
            rca.restapi.year2.year2ADemo.Utils.ApiResponse<Admin> response = 
                new rca.restapi.year2.year2ADemo.Utils.ApiResponse<>("Admin registered successfully", true, registeredAdmin);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            rca.restapi.year2.year2ADemo.Utils.ApiResponse<Admin> response = 
                new rca.restapi.year2.year2ADemo.Utils.ApiResponse<>(e.getMessage(), false, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Register a new supplier", description = "Creates a new supplier account")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Supplier registered successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Supplier.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", 
            description = "Invalid input or email already exists",
            content = @Content
        )
    })
    @PostMapping("/supplier/register")
    public ResponseEntity<rca.restapi.year2.year2ADemo.Utils.ApiResponse<Supplier>> registerSupplier(@Valid @RequestBody Supplier supplier) {
        try {
            Supplier registeredSupplier = authService.registerSupplier(supplier);
            rca.restapi.year2.year2ADemo.Utils.ApiResponse<Supplier> response = 
                new rca.restapi.year2.year2ADemo.Utils.ApiResponse<>("Supplier registered successfully", true, registeredSupplier);
            return ResponseEntity.status(201).body(response);
        } catch (Exception e) {
            rca.restapi.year2.year2ADemo.Utils.ApiResponse<Supplier> response = 
                new rca.restapi.year2.year2ADemo.Utils.ApiResponse<>(e.getMessage(), false, null);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @Operation(summary = "Authenticate user", description = "Login with email and password to get JWT token")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Authentication successful",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Authentication failed",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<rca.restapi.year2.year2ADemo.Utils.ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String email = loginRequest.get("email");
            String password = loginRequest.get("password");
            
            if (email == null || password == null) {
                throw new IllegalArgumentException("Email and password are required");
            }
            
            Map<String, Object> authResponse = authService.authenticate(email, password);
            
            rca.restapi.year2.year2ADemo.Utils.ApiResponse<Map<String, Object>> response = 
                new rca.restapi.year2.year2ADemo.Utils.ApiResponse<>("Authentication successful", true, authResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            rca.restapi.year2.year2ADemo.Utils.ApiResponse<Map<String, Object>> response = 
                new rca.restapi.year2.year2ADemo.Utils.ApiResponse<>(e.getMessage(), false, null);
            return ResponseEntity.status(401).body(response);
        }
    }
} 