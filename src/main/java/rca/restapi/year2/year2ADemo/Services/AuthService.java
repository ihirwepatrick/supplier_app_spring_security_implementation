package rca.restapi.year2.year2ADemo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rca.restapi.year2.year2ADemo.Exception.DuplicateResourceException;
import rca.restapi.year2.year2ADemo.Exception.ResourceNotFoundException;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Repositories.AdminRepository;
import rca.restapi.year2.year2ADemo.Repositories.SupplierRepository;
import rca.restapi.year2.year2ADemo.Security.JwtService;

import java.util.*;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // First try to find an admin with the email
        Optional<Admin> adminOptional = adminRepository.findByEmail(email);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            admin.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
            return new User(admin.getEmail(), admin.getPassword(), authorities);
        }

        // If not found, try to find a supplier with the email
        Optional<Supplier> supplierOptional = supplierRepository.findByEmail(email);
        if (supplierOptional.isPresent()) {
            Supplier supplier = supplierOptional.get();
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            supplier.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
            return new User(supplier.getEmail(), supplier.getPassword(), authorities);
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    public Map<String, Object> authenticate(String email, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            UserDetails userDetails = loadUserByUsername(email);
            String token = jwtService.generateToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            
            // Determine user type and include it in the response
            Optional<Admin> adminOptional = adminRepository.findByEmail(email);
            if (adminOptional.isPresent()) {
                response.put("userType", "admin");
                response.put("userId", adminOptional.get().getId());
            } else {
                Optional<Supplier> supplierOptional = supplierRepository.findByEmail(email);
                if (supplierOptional.isPresent()) {
                    response.put("userType", "supplier");
                    response.put("userId", supplierOptional.get().getId());
                }
            }

            return response;
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid email or password");
        }
    }

    public Admin registerAdmin(Admin admin) {
        if (adminRepository.existsByEmail(admin.getEmail()) || supplierRepository.existsByEmail(admin.getEmail())) {
            throw new DuplicateResourceException("User", "email", admin.getEmail());
        }
        
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        
        if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
            admin.setRoles(Collections.singleton("ROLE_ADMIN"));
        }
        
        return adminRepository.save(admin);
    }

    public Supplier registerSupplier(Supplier supplier) {
        if (adminRepository.existsByEmail(supplier.getEmail()) || supplierRepository.existsByEmail(supplier.getEmail())) {
            throw new DuplicateResourceException("User", "email", supplier.getEmail());
        }
        
        supplier.setPassword(passwordEncoder.encode(supplier.getPassword()));
        
        if (supplier.getRoles() == null || supplier.getRoles().isEmpty()) {
            supplier.setRoles(Collections.singleton("ROLE_SUPPLIER"));
        }
        
        return supplierRepository.save(supplier);
    }
} 