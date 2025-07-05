package com.example.droolsTest.repository;

import com.example.droolsTest.entity.BusinessRule;
import com.example.droolsTest.entity.BusinessRule.RuleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRuleRepository extends JpaRepository<BusinessRule, Long> {

    // Buscar por estado
    List<BusinessRule> findByStatus(RuleStatus status);

    // Buscar por categoría
    List<BusinessRule> findByCategory(String category);

    // Buscar por categoría y estado
    List<BusinessRule> findByCategoryAndStatus(String category, RuleStatus status);

    // Buscar por nombre (case insensitive)
    Optional<BusinessRule> findByNameIgnoreCase(String name);

    // Buscar por creador
    List<BusinessRule> findByCreatedBy(String createdBy);

    // Buscar reglas activas ordenadas por prioridad
    List<BusinessRule> findByStatusOrderByPriorityAsc(RuleStatus status);

    // Buscar por plantilla
    List<BusinessRule> findByIsTemplate(Boolean isTemplate);

    // Buscar por fecha de creación
    List<BusinessRule> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Buscar por tags (contiene)
    List<BusinessRule> findByTagsContainingIgnoreCase(String tag);

    // Query personalizada: buscar por texto en nombre, descripción o contenido
    @Query("SELECT br FROM BusinessRule br WHERE " +
            "LOWER(br.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(br.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(br.ruleContent) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<BusinessRule> findBySearchTerm(@Param("searchTerm") String searchTerm);

    // Query para estadísticas por estado
    @Query("SELECT br.status, COUNT(br) FROM BusinessRule br GROUP BY br.status")
    List<Object[]> getStatisticsByStatus();

    // Query para estadísticas por categoría
    @Query("SELECT br.category, COUNT(br) FROM BusinessRule br GROUP BY br.category")
    List<Object[]> getStatisticsByCategory();

    // Query para obtener reglas modificadas recientemente
    @Query("SELECT br FROM BusinessRule br WHERE br.updatedAt >= :since ORDER BY br.updatedAt DESC")
    List<BusinessRule> findRecentlyModified(@Param("since") LocalDateTime since);

    // Query para obtener reglas con errores
    @Query("SELECT br FROM BusinessRule br WHERE br.status = 'ERROR' OR br.validationErrors IS NOT NULL")
    List<BusinessRule> findRulesWithErrors();

    // Query para obtener reglas por rango de prioridad
    @Query("SELECT br FROM BusinessRule br WHERE br.priority BETWEEN :minPriority AND :maxPriority ORDER BY br.priority ASC")
    List<BusinessRule> findByPriorityRange(@Param("minPriority") Integer minPriority,
                                           @Param("maxPriority") Integer maxPriority);

    // Query para contar reglas por estado
    Long countByStatus(RuleStatus status);

    // Query para contar reglas por categoría
    Long countByCategory(String category);

    // Query para obtener todas las categorías únicas
    @Query("SELECT DISTINCT br.category FROM BusinessRule br WHERE br.category IS NOT NULL ORDER BY br.category")
    List<String> findAllCategories();

    // Query para obtener todos los creadores únicos
    @Query("SELECT DISTINCT br.createdBy FROM BusinessRule br WHERE br.createdBy IS NOT NULL ORDER BY br.createdBy")
    List<String> findAllCreators();

    // Query para obtener reglas duplicadas por nombre
    @Query("SELECT br FROM BusinessRule br WHERE br.name IN " +
            "(SELECT br2.name FROM BusinessRule br2 GROUP BY br2.name HAVING COUNT(br2.name) > 1)")
    List<BusinessRule> findDuplicateRules();

    // Query para obtener la versión más alta de una regla
    @Query("SELECT MAX(br.version) FROM BusinessRule br WHERE br.name = :name")
    Optional<Integer> findMaxVersionByName(@Param("name") String name);

    // Query para obtener reglas activas con prioridad alta
    @Query("SELECT br FROM BusinessRule br WHERE br.status = 'ACTIVE' AND br.priority <= :maxPriority ORDER BY br.priority ASC")
    List<BusinessRule> findHighPriorityActiveRules(@Param("maxPriority") Integer maxPriority);
}