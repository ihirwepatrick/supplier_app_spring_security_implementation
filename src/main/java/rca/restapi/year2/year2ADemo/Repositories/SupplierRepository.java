package rca.restapi.year2.year2ADemo.Repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import rca.restapi.year2.year2ADemo.Models.Supplier;

import java.util.List;
@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    // JPA method for address search
    List<Supplier> findByAddressContaining(String address);
    Page<Supplier> findBySupplierNameContaining(String name, Pageable pageable);

    // Native query to search by supplier name
    @Query(value = "SELECT * FROM supplier WHERE supplier_name LIKE %:name%", nativeQuery = true)
    List<Supplier> searchByName(@Param("name") String name);

}
