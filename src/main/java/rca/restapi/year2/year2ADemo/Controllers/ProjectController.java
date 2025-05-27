package rca.restapi.year2.year2ADemo.Controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Models.Project;
import rca.restapi.year2.year2ADemo.Models.Project.ProjectStatus;
import rca.restapi.year2.year2ADemo.Repositories.AdminRepository;
import rca.restapi.year2.year2ADemo.Services.ProjectService;
import rca.restapi.year2.year2ADemo.Utils.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/projects")
@Tag(name = "Projects", description = "Project management API")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {
    
    @Autowired
    private ProjectService projectService;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Operation(summary = "Get all projects", description = "Returns a paginated list of all projects")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "List of projects retrieved",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Project.class))
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Project>>> getAllProjects(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Results per page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "id") String sortBy) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<Project> projects = projectService.getAllProjects(pageable);
        
        ApiResponse<Page<Project>> response = new ApiResponse<>(
                "Retrieved projects successfully", true, projects);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get project by ID", description = "Returns a project by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Project found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Project.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Project not found",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public ResponseEntity<ApiResponse<Project>> getProjectById(
            @Parameter(description = "Project ID", required = true) @PathVariable Long id) {
        
        Project project = projectService.getProjectById(id);
        
        ApiResponse<Project> response = new ApiResponse<>(
                "Project retrieved successfully", true, project);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Search projects", description = "Search projects by name")
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Project>>> searchProjects(
            @Parameter(description = "Project name") @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projects = projectService.searchProjects(name, pageable);
        
        ApiResponse<Page<Project>> response = new ApiResponse<>(
                "Search results", true, projects);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get projects by status", description = "Returns projects filtered by status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Project>>> getProjectsByStatus(
            @Parameter(description = "Project status") @PathVariable ProjectStatus status) {
        
        List<Project> projects = projectService.getProjectsByStatus(status);
        
        ApiResponse<List<Project>> response = new ApiResponse<>(
                "Projects filtered by status", true, projects);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get my projects", description = "Returns projects created by the logged-in admin")
    @GetMapping("/my-projects")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Project>>> getMyProjects(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Admin admin = adminRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Project> projects = projectService.getProjectsByAdmin(admin.getId());
        
        ApiResponse<List<Project>> response = new ApiResponse<>(
                "My projects retrieved successfully", true, projects);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Create a new project", description = "Creates a new project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Project created successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Project.class))
        )
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Project>> createProject(
            @Valid @RequestBody Project project,
            Authentication authentication) {
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Admin admin = adminRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Project newProject = projectService.createProject(project, admin.getId());
        
        ApiResponse<Project> response = new ApiResponse<>(
                "Project created successfully", true, newProject);
        return ResponseEntity.status(201).body(response);
    }
    
    @Operation(summary = "Update project", description = "Updates an existing project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Project updated successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Project.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Project not found",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Project>> updateProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long id,
            @Valid @RequestBody Project project) {
        
        Project updatedProject = projectService.updateProject(id, project);
        
        ApiResponse<Project> response = new ApiResponse<>(
                "Project updated successfully", true, updatedProject);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update project status", description = "Updates the status of an existing project")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Project>> updateProjectStatus(
            @Parameter(description = "Project ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> status) {
        
        ProjectStatus projectStatus = ProjectStatus.valueOf(status.get("status"));
        Project updatedProject = projectService.updateProjectStatus(id, projectStatus);
        
        ApiResponse<Project> response = new ApiResponse<>(
                "Project status updated successfully", true, updatedProject);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete project", description = "Deletes a project by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Project deleted successfully",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Project not found",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long id) {
        
        projectService.deleteProject(id);
        
        ApiResponse<Void> response = new ApiResponse<>(
                "Project deleted successfully", true, null);
        return ResponseEntity.ok(response);
    }
} 