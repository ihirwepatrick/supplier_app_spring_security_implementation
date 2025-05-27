package rca.restapi.year2.year2ADemo.Models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "suppliers")
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Supplier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Supplier name is required")
    @Size(min = 3, max = 100, message = "Supplier name must be between 3 and 100 characters")
    @Column(length = 100)
    private String supplierName;

    @NotBlank(message = "Address is required")
    @Size(min = 5, max = 200, message = "Address must be between 5 and 200 characters")
    @Column(length = 200)
    private String address;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone number must be a valid format")
    @Column(length = 15)
    private String phoneNumber;

    @Column(unique = true, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", 
             message = "Password must contain at least one letter and one number")
    @Column(length = 100)
    private String password;
    
    @Size(max = 100, message = "Contact person name must not exceed 100 characters")
    @Column(length = 100)
    private String contactPerson;
    
    @Size(max = 50, message = "Category must not exceed 50 characters")
    @Column(length = 50)
    private String category;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private SupplierStatus status = SupplierStatus.ACTIVE;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "supplier_roles", joinColumns = @JoinColumn(name = "supplier_id"))
    @Column(name = "role", length = 20)
    private Set<String> roles = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Default role for suppliers
        if (this.roles == null || this.roles.isEmpty()) {
            this.roles = new HashSet<>();
            this.roles.add("ROLE_SUPPLIER");
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    
    public enum SupplierStatus {
        ACTIVE, INACTIVE, BLACKLISTED
    }
}
