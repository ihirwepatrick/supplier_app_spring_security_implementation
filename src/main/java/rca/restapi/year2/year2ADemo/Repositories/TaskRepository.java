package rca.restapi.year2.year2ADemo.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rca.restapi.year2.year2ADemo.Models.Project;
import rca.restapi.year2.year2ADemo.Models.Supplier;
import rca.restapi.year2.year2ADemo.Models.Task;
import rca.restapi.year2.year2ADemo.Models.Task.TaskStatus;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    // Find tasks by project
    List<Task> findByProject(Project project);
    
    // Find tasks by assigned supplier
    List<Task> findByAssignedTo(Supplier supplier);
    
    // Find tasks by status
    List<Task> findByStatus(TaskStatus status);
    
    // Find tasks by project and status
    List<Task> findByProjectAndStatus(Project project, TaskStatus status);
    
    // Find tasks by project with pagination
    Page<Task> findByProject(Project project, Pageable pageable);
} 