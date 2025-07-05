package com.example.droolsTest.service;

import com.example.droolsTest.config.DroolsConfig;
import com.example.droolsTest.entity.BusinessRule;
import com.example.droolsTest.entity.BusinessRule.RuleStatus;
import com.example.droolsTest.model.Customer;
import com.example.droolsTest.repository.BusinessRuleRepository;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class RuleService {

    private static final Logger logger = LoggerFactory.getLogger(RuleService.class);

    @Autowired
    private BusinessRuleRepository ruleRepository;

    @Autowired
    private KieServices kieServices;

    @Autowired
    private DroolsConfig droolsConfig;

    private KieContainer kieContainer;

    // === CRUD Operations ===

    public List<BusinessRule> getAllRules() {
        return ruleRepository.findAll();
    }

    public Page<BusinessRule> getAllRules(Pageable pageable) {
        return ruleRepository.findAll(pageable);
    }

    public List<BusinessRule> getActiveRules() {
        return ruleRepository.findByStatusOrderByPriorityAsc(RuleStatus.ACTIVE);
    }

    public Optional<BusinessRule> getRuleById(Long id) {
        return ruleRepository.findById(id);
    }

    public BusinessRule saveRule(BusinessRule rule) {
        // Validar regla antes de guardar
        ValidationResult validation = validateRule(rule.getRuleContent());
        if (!validation.isValid()) {
            rule.setStatus(RuleStatus.ERROR);
            rule.setValidationErrors(validation.getErrors());
        } else {
            rule.setValidationErrors(null);
            if (rule.getStatus() == RuleStatus.ERROR) {
                rule.setStatus(RuleStatus.DRAFT);
            }
        }

        // Incrementar versión si es una actualización
        if (rule.getId() != null) {
            Optional<BusinessRule> existingRule = ruleRepository.findById(rule.getId());
            if (existingRule.isPresent()) {
                rule.incrementVersion();
            }
        }

        BusinessRule savedRule = ruleRepository.save(rule);
        logger.info("Regla guardada: {} [ID: {}]", savedRule.getName(), savedRule.getId());

        // Recargar contenedor si la regla está activa
        if (savedRule.getStatus() == RuleStatus.ACTIVE) {
            reloadRulesContainer();
        }

        return savedRule;
    }

    public void deleteRule(Long id) {
        Optional<BusinessRule> rule = ruleRepository.findById(id);
        if (rule.isPresent()) {
            ruleRepository.deleteById(id);
            logger.info("Regla eliminada: {} [ID: {}]", rule.get().getName(), id);

            // Recargar contenedor si la regla estaba activa
            if (rule.get().getStatus() == RuleStatus.ACTIVE) {
                reloadRulesContainer();
            }
        }
    }

    // === Query Methods ===

    public List<BusinessRule> getRulesByCategory(String category) {
        return ruleRepository.findByCategory(category);
    }

    public List<BusinessRule> getRulesByStatus(RuleStatus status) {
        return ruleRepository.findByStatus(status);
    }

    public List<BusinessRule> searchRules(String searchTerm) {
        return ruleRepository.findBySearchTerm(searchTerm);
    }

    public List<BusinessRule> getRecentRules(int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return ruleRepository.findRecentlyModified(since);
    }

    public List<BusinessRule> getRulesWithErrors() {
        return ruleRepository.findRulesWithErrors();
    }

    public List<String> getAllCategories() {
        return ruleRepository.findAllCategories();
    }

    // === Validation ===

    public ValidationResult validateRule(String ruleContent) {
        ValidationResult result = new ValidationResult();

        try {
            KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
            String drlContent = createDrlContent(ruleContent);
            kieFileSystem.write("src/main/resources/temp-rule.drl", drlContent);

            KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
            kieBuilder.buildAll();

            Results results = kieBuilder.getResults();

            if (results.hasMessages(Message.Level.ERROR)) {
                result.setValid(false);
                StringBuilder errors = new StringBuilder();
                for (Message message : results.getMessages(Message.Level.ERROR)) {
                    errors.append(message.getText()).append("\n");
                }
                result.setErrors(errors.toString());
            } else {
                result.setValid(true);
                result.setErrors(null);
            }

        } catch (Exception e) {
            result.setValid(false);
            result.setErrors("Error de validación: " + e.getMessage());
            logger.error("Error validating rule", e);
        }

        return result;
    }

    // === Rule Activation/Deactivation ===

    public BusinessRule activateRule(Long id) {
        Optional<BusinessRule> ruleOpt = ruleRepository.findById(id);
        if (ruleOpt.isPresent()) {
            BusinessRule rule = ruleOpt.get();

            // Validar regla antes de activar
            ValidationResult validation = validateRule(rule.getRuleContent());
            if (!validation.isValid()) {
                rule.setStatus(RuleStatus.ERROR);
                rule.setValidationErrors(validation.getErrors());
                return ruleRepository.save(rule);
            }

            rule.setStatus(RuleStatus.ACTIVE);
            rule.setValidationErrors(null);
            BusinessRule savedRule = ruleRepository.save(rule);

            // Recargar el contenedor de reglas
            reloadRulesContainer();

            logger.info("Regla activada: {} [ID: {}]", savedRule.getName(), savedRule.getId());
            return savedRule;
        }
        throw new RuntimeException("Regla no encontrada con ID: " + id);
    }

    public BusinessRule deactivateRule(Long id) {
        Optional<BusinessRule> ruleOpt = ruleRepository.findById(id);
        if (ruleOpt.isPresent()) {
            BusinessRule rule = ruleOpt.get();
            rule.setStatus(RuleStatus.INACTIVE);
            BusinessRule savedRule = ruleRepository.save(rule);

            // Recargar el contenedor de reglas
            reloadRulesContainer();

            logger.info("Regla desactivada: {} [ID: {}]", savedRule.getName(), savedRule.getId());
            return savedRule;
        }
        throw new RuntimeException("Regla no encontrada con ID: " + id);
    }

    // === Rules Execution ===

    public Customer executeRules(Customer customer) {
        if (kieContainer == null) {
            reloadRulesContainer();
        }

        if (kieContainer == null) {
            logger.warn("No rules container available");
            return customer;
        }

        KieSession kieSession = kieContainer.newKieSession();

        try {
            // Limpiar resultados anteriores
            customer.setDiscount(null);
            customer.setRecommendation(null);
            customer.setAlert(null);
            customer.setEligibleForPromotion(null);
            customer.setNextAction(null);

            kieSession.insert(customer);
            int rulesFired = kieSession.fireAllRules();

            logger.info("Reglas ejecutadas: {} reglas disparadas para cliente: {}",
                    rulesFired, customer.getName());

            return customer;
        } finally {
            kieSession.dispose();
        }
    }

    // === Container Management ===

    private void reloadRulesContainer() {
        try {
            List<BusinessRule> activeRules = getActiveRules();

            if (activeRules.isEmpty()) {
                logger.info("No hay reglas activas para cargar");
                return;
            }

            List<String> ruleContents = activeRules.stream()
                    .map(BusinessRule::getRuleContent)
                    .collect(Collectors.toList());

            this.kieContainer = droolsConfig.reloadRules(kieServices, ruleContents);

            logger.info("Contenedor de reglas recargado con {} reglas activas", activeRules.size());

        } catch (Exception e) {
            logger.error("Error recargando contenedor de reglas", e);
        }
    }

    private String createDrlContent(String ruleContent) {
        StringBuilder drl = new StringBuilder();
        drl.append("package com.empresa.drools.rules;\n\n");
        drl.append("import com.empresa.drools.model.Customer;\n");
        drl.append("import java.util.List;\n");
        drl.append("import java.time.LocalDate;\n\n");
        drl.append(ruleContent);
        return drl.toString();
    }

    // === Statistics ===

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        // Estadísticas por estado
        Map<RuleStatus, Long> statusStats = new HashMap<>();
        for (RuleStatus status : RuleStatus.values()) {
            statusStats.put(status, ruleRepository.countByStatus(status));
        }
        stats.put("byStatus", statusStats);

        // Estadísticas por categoría
        List<String> categories = getAllCategories();
        Map<String, Long> categoryStats = new HashMap<>();
        for (String category : categories) {
            categoryStats.put(category, ruleRepository.countByCategory(category));
        }
        stats.put("byCategory", categoryStats);

        // Estadísticas generales
        stats.put("totalRules", ruleRepository.count());
        stats.put("activeRules", ruleRepository.countByStatus(RuleStatus.ACTIVE));
        stats.put("rulesWithErrors", ruleRepository.findRulesWithErrors().size());

        return stats;
    }

    // === Validation Result Class ===

    public static class ValidationResult {
        private boolean valid;
        private String errors;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getErrors() {
            return errors;
        }

        public void setErrors(String errors) {
            this.errors = errors;
        }
    }
}
