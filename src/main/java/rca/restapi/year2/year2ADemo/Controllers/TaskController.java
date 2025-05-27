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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Models.Task;
import rca.restapi.year2.year2ADemo.Models.Task.TaskStatus;
import rca.restapi.year2.year2ADemo.Repositories.SupplierRepository;
import rca.restapi.year2.year2ADemo.Services.TaskService;
import rca.restapi.year2.year2ADemo.Utils.ApiResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task management API")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {
    
    @Autowired
    private TaskService taskService;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    @Operation(summary = "Get all tasks", description = "Returns a paginated list of all tasks")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "List of tasks retrieved",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Task.class))
        )
    })
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<Task>>> getAllTasks(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Results per page") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getAllTasks(pageable);
        
        ApiResponse<Page<Task>> response = new ApiResponse<>(
                "Retrieved tasks successfully", true, tasks);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get task by ID", description = "Returns a task by its ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Task found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Task.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Task not found",
            content = @Content
        )
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public ResponseEntity<ApiResponse<Task>> getTaskById(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id) {
        
        Task task = taskService.getTaskById(id);
        
        ApiResponse<Task> response = new ApiResponse<>(
                "Task retrieved successfully", true, task);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get tasks by project", description = "Returns tasks belonging to a specific project")
    @GetMapping("/project/{projectId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public ResponseEntity<ApiResponse<Page<Task>>> getTasksByProject(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Task> tasks = taskService.getTasksByProject(projectId, pageable);
        
        ApiResponse<Page<Task>> response = new ApiResponse<>(
                "Tasks for project retrieved successfully", true, tasks);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get my assigned tasks", description = "Returns tasks assigned to the logged-in supplier")
    @GetMapping("/my-tasks")
    @PreAuthorize("hasRole('SUPPLIER')")
    public ResponseEntity<ApiResponse<List<Task>>> getMyTasks(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Supplier supplier = supplierRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Task> tasks = taskService.getTasksBySupplier(supplier.getId());
        
        ApiResponse<List<Task>> response = new ApiResponse<>(
                "My assigned tasks retrieved successfully", true, tasks);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get tasks by status", description = "Returns tasks filtered by status")
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByStatus(
            @Parameter(description = "Task status") @PathVariable TaskStatus status) {
        
        List<Task> tasks = taskService.getTasksByStatus(status);
        
        ApiResponse<List<Task>> response = new ApiResponse<>(
                "Tasks filtered by status", true, tasks);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Get tasks by project and status", description = "Returns tasks filtered by project and status")
    @GetMapping("/project/{projectId}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public ResponseEntity<ApiResponse<List<Task>>> getTasksByProjectAndStatus(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId,
            @Parameter(description = "Task status") @PathVariable TaskStatus status) {
        
        List<Task> tasks = taskService.getTasksByProjectAndStatus(projectId, status);
        
        ApiResponse<List<Task>> response = new ApiResponse<>(
                "Tasks filtered by project and status", true, tasks);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Create a new task", description = "Creates a new task for a specific project")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", 
            description = "Task created successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Task.class))
        )
    })
    @PostMapping("/project/{projectId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Task>> createTask(
            @Parameter(description = "Project ID", required = true) @PathVariable Long projectId,
            @Valid @RequestBody Task task) {
        
        Task newTask = taskService.createTask(task, projectId);
        
        ApiResponse<Task> response = new ApiResponse<>(
                "Task created successfully", true, newTask);
        return ResponseEntity.status(201).body(response);
    }
    
    @Operation(summary = "Update task", description = "Updates an existing task")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Task updated successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = Task.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Task not found",
            content = @Content
        )
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Task>> updateTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id,
            @Valid @RequestBody Task task) {
        
        Task updatedTask = taskService.updateTask(id, task);
        
        ApiResponse<Task> response = new ApiResponse<>(
                "Task updated successfully", true, updatedTask);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Assign task to supplier", description = "Assigns a task to a specific supplier")
    @PatchMapping("/{id}/assign/{supplierId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Task>> assignTaskToSupplier(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id,
            @Parameter(description = "Supplier ID", required = true) @PathVariable Long supplierId) {
        
        Task updatedTask = taskService.assignTaskToSupplier(id, supplierId);
        
        ApiResponse<Task> response = new ApiResponse<>(
                "Task assigned successfully", true, updatedTask);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Update task status", description = "Updates the status of an existing task")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPPLIER')")
    public ResponseEntity<ApiResponse<Task>> updateTaskStatus(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id,
            @RequestBody Map<String, String> status,
            Authentication authentication) {
        
        TaskStatus taskStatus = TaskStatus.valueOf(status.get("status"));
        Task task = taskService.getTaskById(id);
        
        // If user is a supplier, they can only update their own assigned tasks
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPPLIER"))) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Supplier supplier = supplierRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(supplier.getId())) {
                throw new RuntimeException("You can only update tasks assigned to you");
            }
        }
        
        Task updatedTask = taskService.updateTaskStatus(id, taskStatus);
        
        ApiResponse<Task> response = new ApiResponse<>(
                "Task status updated successfully", true, updatedTask);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Delete task", description = "Deletes a task by ID")
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", 
            description = "Task deleted successfully",
            content = @Content
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", 
            description = "Task not found",
            content = @Content
        )
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @Parameter(description = "Task ID", required = true) @PathVariable Long id) {
        
        taskService.deleteTask(id);
        
        ApiResponse<Void> response = new ApiResponse<>(
                "Task deleted successfully", true, null);
        return ResponseEntity.ok(response);
    }
} 