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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
    public ResponseEntity<rca.restapi.year2.year2ADemo.Utils.ApiResponse<Admin>> registerAdmin(@RequestBody Map<String, Object> adminData) {
        try {
            Admin admin = new Admin();
            
            // Set basic fields
            if (adminData.containsKey("fullName")) admin.setFullName(adminData.get("fullName").toString());
            if (adminData.containsKey("email")) admin.setEmail(adminData.get("email").toString());
            if (adminData.containsKey("password")) admin.setPassword(adminData.get("password").toString());
            if (adminData.containsKey("phoneNumber")) admin.setPhoneNumber(adminData.get("phoneNumber").toString());
            
            // Handle roles which may come as array from Swagger or as single string
            if (adminData.containsKey("roles")) {
                Object rolesObj = adminData.get("roles");
                HashSet<String> roles = new HashSet<>();
                
                if (rolesObj instanceof List) {
                    // If it's a list/array, add each element
                    List<?> rolesList = (List<?>) rolesObj;
                    for (Object role : rolesList) {
                        if (role != null) {
                            roles.add(role.toString());
                        }
                    }
                } else if (rolesObj instanceof String) {
                    // If it's a string, add it directly
                    roles.add((String) rolesObj);
                }
                
                // If we got any roles, set them
                if (!roles.isEmpty()) {
                    admin.setRoles(roles);
                } else {
                    // Default role
                    admin.setRoles(Collections.singleton("ROLE_ADMIN"));
                }
            }
            
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
    public ResponseEntity<rca.restapi.year2.year2ADemo.Utils.ApiResponse<Supplier>> registerSupplier(@RequestBody Map<String, Object> supplierData) {
        try {
            Supplier supplier = new Supplier();
            
            // Set basic fields
            if (supplierData.containsKey("supplierName")) supplier.setSupplierName(supplierData.get("supplierName").toString());
            if (supplierData.containsKey("address")) supplier.setAddress(supplierData.get("address").toString());
            if (supplierData.containsKey("phoneNumber")) supplier.setPhoneNumber(supplierData.get("phoneNumber").toString());
            if (supplierData.containsKey("email")) supplier.setEmail(supplierData.get("email").toString());
            if (supplierData.containsKey("password")) supplier.setPassword(supplierData.get("password").toString());
            if (supplierData.containsKey("contactPerson")) supplier.setContactPerson(supplierData.get("contactPerson").toString());
            if (supplierData.containsKey("category")) supplier.setCategory(supplierData.get("category").toString());
            if (supplierData.containsKey("description")) supplier.setDescription(supplierData.get("description").toString());
            
            // Handle roles which may come as array from Swagger or as single string
            if (supplierData.containsKey("roles")) {
                Object rolesObj = supplierData.get("roles");
                HashSet<String> roles = new HashSet<>();
                
                if (rolesObj instanceof List) {
                    // If it's a list/array, add each element
                    List<?> rolesList = (List<?>) rolesObj;
                    for (Object role : rolesList) {
                        if (role != null) {
                            roles.add(role.toString());
                        }
                    }
                } else if (rolesObj instanceof String) {
                    // If it's a string, add it directly
                    roles.add((String) rolesObj);
                }
                
                // If we got any roles, set them
                if (!roles.isEmpty()) {
                    supplier.setRoles(roles);
                } else {
                    // Default role
                    supplier.setRoles(Collections.singleton("ROLE_SUPPLIER"));
                }
            }
            
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

    @Operation(
        summary = "Authenticate user", 
        description = "Login with email and password to get JWT token. " +
                "After getting the token, click on the 'Authorize' button at the top right of the page " +
                "and enter the token in the format: 'Bearer yourTokenHere'. " +
                "This will authenticate all subsequent API calls."
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Authentication successful. Token is returned in the response.",
            content = @Content(mediaType = "application/json")
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", 
            description = "Authentication failed - invalid credentials",
            content = @Content
        )
    })
    @PostMapping("/login")
    public ResponseEntity<rca.restapi.year2.year2ADemo.Utils.ApiResponse<Map<String, Object>>> login(@RequestBody Map<String, Object> loginRequest) {
        try {
            String email;
            String password;
            
            // Handle email field which might be a string or array from Swagger UI
            Object emailObj = loginRequest.get("email");
            if (emailObj instanceof String) {
                email = (String) emailObj;
            } else if (emailObj instanceof List) {
                // If it's an array/list, take the first value
                List<?> emailList = (List<?>) emailObj;
                if (emailList.isEmpty()) {
                    throw new IllegalArgumentException("Email is required");
                }
                email = emailList.get(0).toString();
            } else {
                throw new IllegalArgumentException("Invalid email format");
            }
            
            // Handle password field which might be a string or array
            Object passwordObj = loginRequest.get("password");
            if (passwordObj instanceof String) {
                password = (String) passwordObj;
            } else if (passwordObj instanceof List) {
                // If it's an array/list, take the first value
                List<?> passwordList = (List<?>) passwordObj;
                if (passwordList.isEmpty()) {
                    throw new IllegalArgumentException("Password is required");
                }
                password = passwordList.get(0).toString();
            } else {
                throw new IllegalArgumentException("Invalid password format");
            }
            
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