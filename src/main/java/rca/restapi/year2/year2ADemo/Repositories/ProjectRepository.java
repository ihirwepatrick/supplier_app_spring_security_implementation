package rca.restapi.year2.year2ADemo.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rca.restapi.year2.year2ADemo.Models.Admin;
import rca.restapi.year2.year2ADemo.Models.Project;
import rca.restapi.year2.year2ADemo.Models.Project.ProjectStatus;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {
    
    // Find projects by admin who created them
    List<Project> findByCreatedBy(Admin admin);
    
    // Find projects by name containing a string
    Page<Project> findByNameContaining(String name, Pageable pageable);
    
    // Find projects by status
    List<Project> findByStatus(ProjectStatus status);
} 