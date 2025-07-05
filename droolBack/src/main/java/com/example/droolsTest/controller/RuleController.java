package com.example.droolsTest.controller;

import com.example.droolsTest.entity.BusinessRule;
import com.example.droolsTest.entity.BusinessRule.RuleStatus;
import com.example.droolsTest.model.Customer;
import com.example.droolsTest.service.RuleService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/rules")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:80"})
public class RuleController {

    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);

    @Autowired
    private RuleService ruleService;

    // === CRUD Operations ===

    @GetMapping
    public ResponseEntity<List<BusinessRule>> getAllRules(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            Sort sort = sortDir.equalsIgnoreCase("desc") ?
                    Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);
            Page<BusinessRule> rulePage = ruleService.getAllRules(pageable);

            return ResponseEntity.ok()
                    .header("X-Total-Count", String.valueOf(rulePage.getTotalElements()))
                    .header("X-Total-Pages", String.valueOf(rulePage.getTotalPages()))
                    .body(rulePage.getContent());
        } catch (Exception e) {
            logger.error("Error obteniendo reglas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<BusinessRule> getRuleById(@PathVariable Long id) {
        try {
            Optional<BusinessRule> rule = ruleService.getRuleById(id);
            return rule.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error obteniendo regla por ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<BusinessRule> createRule(@Valid @RequestBody BusinessRule rule) {
        try {
            rule.setCreatedBy("api-user"); // TODO: Obtener del contexto de seguridad
            BusinessRule savedRule = ruleService.saveRule(rule);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
        } catch (Exception e) {
            logger.error("Error creando regla", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<BusinessRule> updateRule(@PathVariable Long id,
                                                   @Valid @RequestBody BusinessRule rule) {
        try {
            if (!ruleService.getRuleById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            rule.setId(id);
            rule.setUpdatedBy("api-user"); // TODO: Obtener del contexto de seguridad
            BusinessRule updatedRule = ruleService.saveRule(rule);
            return ResponseEntity.ok(updatedRule);
        } catch (Exception e) {
            logger.error("Error actualizando regla ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        try {
            if (!ruleService.getRuleById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }

            ruleService.deleteRule(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error eliminando regla ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === Query Endpoints ===

    @GetMapping("/active")
    public ResponseEntity<List<BusinessRule>> getActiveRules() {
        try {
            List<BusinessRule> activeRules = ruleService.getActiveRules();
            return ResponseEntity.ok(activeRules);
        } catch (Exception e) {
            logger.error("Error obteniendo reglas activas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<BusinessRule>> getRulesByCategory(@PathVariable String category) {
        try {
            List<BusinessRule> rules = ruleService.getRulesByCategory(category);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error obteniendo reglas por categoría: {}", category, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<BusinessRule>> getRulesByStatus(@PathVariable RuleStatus status) {
        try {
            List<BusinessRule> rules = ruleService.getRulesByStatus(status);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error obteniendo reglas por estado: {}", status, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<BusinessRule>> searchRules(@RequestParam String q) {
        try {
            List<BusinessRule> rules = ruleService.searchRules(q);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error buscando reglas con término: {}", q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<List<BusinessRule>> getRecentRules(@RequestParam(defaultValue = "7") int days) {
        try {
            List<BusinessRule> rules = ruleService.getRecentRules(days);
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error obteniendo reglas recientes", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/errors")
    public ResponseEntity<List<BusinessRule>> getRulesWithErrors() {
        try {
            List<BusinessRule> rules = ruleService.getRulesWithErrors();
            return ResponseEntity.ok(rules);
        } catch (Exception e) {
            logger.error("Error obteniendo reglas con errores", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        try {
            List<String> categories = ruleService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Error obteniendo categorías", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === Rule Management ===

    @PostMapping("/{id}/activate")
    public ResponseEntity<BusinessRule> activateRule(@PathVariable Long id) {
        try {
            BusinessRule rule = ruleService.activateRule(id);
            return ResponseEntity.ok(rule);
        } catch (RuntimeException e) {
            logger.error("Error activando regla ID: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error activando regla ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<BusinessRule> deactivateRule(@PathVariable Long id) {
        try {
            BusinessRule rule = ruleService.deactivateRule(id);
            return ResponseEntity.ok(rule);
        } catch (RuntimeException e) {
            logger.error("Error desactivando regla ID: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error desactivando regla ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<RuleService.ValidationResult> validateRule(@PathVariable Long id) {
        try {
            Optional<BusinessRule> ruleOpt = ruleService.getRuleById(id);
            if (!ruleOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            RuleService.ValidationResult result = ruleService.validateRule(ruleOpt.get().getRuleContent());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error validando regla ID: {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<RuleService.ValidationResult> validateRuleContent(@RequestBody Map<String, String> payload) {
        try {
            String ruleContent = payload.get("ruleContent");
            if (ruleContent == null || ruleContent.trim().isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            RuleService.ValidationResult result = ruleService.validateRule(ruleContent);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error validando contenido de regla", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === Rules Execution ===

    @PostMapping("/execute")
    public ResponseEntity<Customer> executeRules(@RequestBody Customer customer) {
        try {
            Customer result = ruleService.executeRules(customer);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error ejecutando reglas para cliente: {}", customer.getName(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === Statistics ===

    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = ruleService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("Error obteniendo estadísticas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // === Templates ===

    @GetMapping("/templates")
    public ResponseEntity<List<Map<String, String>>> getRuleTemplates() {
        try {
            List<Map<String, String>> templates = List.of(
                    Map.of(
                            "name", "Descuento por Edad",
                            "description", "Descuento basado en la edad del cliente",
                            "content", "rule \"Descuento por Edad\"\nwhen\n    $customer : Customer(age < 30)\nthen\n    $customer.setDiscount(\"10% descuento jóvenes\");\nend"
                    ),
                    Map.of(
                            "name", "Cliente VIP",
                            "description", "Clasificación de cliente VIP por compras",
                            "content", "rule \"Cliente VIP\"\nwhen\n    $customer : Customer(totalPurchases > 10000)\nthen\n    $customer.setCategory(\"VIP\");\n    $customer.setDiscount(\"25% descuento VIP\");\nend"
                    ),
                    Map.of(
                            "name", "Cliente Nuevo",
                            "description", "Bienvenida para clientes nuevos",
                            "content", "rule \"Cliente Nuevo\"\nwhen\n    $customer : Customer(isNewCustomer() == true)\nthen\n    $customer.setRecommendation(\"¡Bienvenido! Descubre nuestros productos\");\nend"
                    )
            );

            return ResponseEntity.ok(templates);
        } catch (Exception e) {
            logger.error("Error obteniendo plantillas", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}