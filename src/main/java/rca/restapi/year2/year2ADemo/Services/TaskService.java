package rca.restapi.year2.year2ADemo.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import rca.restapi.year2.year2ADemo.Exception.ResourceNotFoundException;
import rca.restapi.year2.year2ADemo.Models.Project;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Models.Task;
import rca.restapi.year2.year2ADemo.Models.Task.TaskStatus;
import rca.restapi.year2.year2ADemo.Repositories.ProjectRepository;
import rca.restapi.year2.year2ADemo.Repositories.SupplierRepository;
import rca.restapi.year2.year2ADemo.Repositories.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private SupplierRepository supplierRepository;
    
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    public Page<Task> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    
    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", "id", id));
    }
    
    public List<Task> getTasksByProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        return taskRepository.findByProject(project);
    }
    
    public Page<Task> getTasksByProject(Long projectId, Pageable pageable) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        return taskRepository.findByProject(project, pageable);
    }
    
    public List<Task> getTasksBySupplier(Long supplierId) {
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));
        
        return taskRepository.findByAssignedTo(supplier);
    }
    
    public List<Task> getTasksByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }
    
    public List<Task> getTasksByProjectAndStatus(Long projectId, TaskStatus status) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        return taskRepository.findByProjectAndStatus(project, status);
    }
    
    public Task createTask(Task task, Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
        
        task.setProject(project);
        return taskRepository.save(task);
    }
    
    public Task updateTask(Long id, Task taskDetails) {
        Task task = getTaskById(id);
        
        task.setName(taskDetails.getName());
        task.setDescription(taskDetails.getDescription());
        task.setDueDate(taskDetails.getDueDate());
        task.setBudget(taskDetails.getBudget());
        task.setStatus(taskDetails.getStatus());
        
        return taskRepository.save(task);
    }
    
    public Task assignTaskToSupplier(Long taskId, Long supplierId) {
        Task task = getTaskById(taskId);
        Supplier supplier = supplierRepository.findById(supplierId)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", supplierId));
        
        task.setAssignedTo(supplier);
        return taskRepository.save(task);
    }
    
    public Task updateTaskStatus(Long id, TaskStatus status) {
        Task task = getTaskById(id);
        task.setStatus(status);
        return taskRepository.save(task);
    }
    
    public void deleteTask(Long id) {
        Task task = getTaskById(id);
        taskRepository.delete(task);
    }
} 