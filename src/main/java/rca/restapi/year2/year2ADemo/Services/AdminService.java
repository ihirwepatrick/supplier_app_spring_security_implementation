package rca.restapi.year2.year2ADemo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Repositories.AdminRepository;

import java.util.Collections;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public Page<Admin> getAllAdmins(Pageable pageable) {
        return adminRepository.findAll(pageable);
    }
    
    public Optional<Admin> getAdminById(Long id) {
        return adminRepository.findById(id);
    }
    
    public Optional<Admin> getAdminByEmail(String email) {
        return adminRepository.findByEmail(email);
    }
    
    public Admin registerAdmin(Admin admin) {
        // Encrypt the password before saving
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        
        // Set default role if none provided
        if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
            admin.setRoles(Collections.singleton("ROLE_ADMIN"));
        }
        
        return adminRepository.save(admin);
    }
    
    public Admin updateAdmin(Long id, Admin adminDetails) {
        Admin admin = adminRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Admin not found with id: " + id));
        
        admin.setFullName(adminDetails.getFullName());
        admin.setPhoneNumber(adminDetails.getPhoneNumber());
        
        // Only update email if it has changed and is not already taken
        if (!admin.getEmail().equals(adminDetails.getEmail()) && 
            !adminRepository.existsByEmail(adminDetails.getEmail())) {
            admin.setEmail(adminDetails.getEmail());
        }
        
        // Update password if provided
        if (adminDetails.getPassword() != null && !adminDetails.getPassword().isEmpty()) {
            admin.setPassword(passwordEncoder.encode(adminDetails.getPassword()));
        }
        
        // Update roles if provided
        if (adminDetails.getRoles() != null && !adminDetails.getRoles().isEmpty()) {
            admin.setRoles(adminDetails.getRoles());
        }
        
        return adminRepository.save(admin);
    }
    
    public void deleteAdmin(Long id) {
        adminRepository.deleteById(id);
    }
} 