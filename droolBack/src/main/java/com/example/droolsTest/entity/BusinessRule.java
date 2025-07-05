package com.example.droolsTest.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

@Entity
@Table(name = "business_rules")
public class BusinessRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la regla es requerido")
    @Size(max = 255, message = "El nombre no puede exceder 255 caracteres")
    @Column(nullable = false, unique = true)
    private String name;

    @Size(max = 1000, message = "La descripción no puede exceder 1000 caracteres")
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotBlank(message = "El contenido de la regla es requerido")
    @Column(name = "rule_content", columnDefinition = "TEXT", nullable = false)
    private String ruleContent;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private RuleStatus status = RuleStatus.DRAFT;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "priority")
    private Integer priority = 100;

    @Column(name = "validation_errors", columnDefinition = "TEXT")
    private String validationErrors;

    @Column(name = "tags", length = 500)
    private String tags;

    @Column(name = "is_template")
    private Boolean isTemplate = false;

    public enum RuleStatus {
        DRAFT("Borrador"),
        ACTIVE("Activa"),
        INACTIVE("Inactiva"),
        TESTING("En Prueba"),
        ERROR("Error");

        private final String displayName;

        RuleStatus(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (version == null) {
            version = 1;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructores
    public BusinessRule() {}

    public BusinessRule(String name, String description, String ruleContent) {
        this.name = name;
        this.description = description;
        this.ruleContent = ruleContent;
        this.status = RuleStatus.DRAFT;
    }

    // Métodos de conveniencia
    public boolean isActive() {
        return RuleStatus.ACTIVE.equals(this.status);
    }

    public boolean hasErrors() {
        return RuleStatus.ERROR.equals(this.status) ||
                (validationErrors != null && !validationErrors.trim().isEmpty());
    }

    public void incrementVersion() {
        this.version = (this.version == null) ? 1 : this.version + 1;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleContent() {
        return ruleContent;
    }

    public void setRuleContent(String ruleContent) {
        this.ruleContent = ruleContent;
    }

    public RuleStatus getStatus() {
        return status;
    }

    public void setStatus(RuleStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(String validationErrors) {
        this.validationErrors = validationErrors;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Boolean getIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(Boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    @Override
    public String toString() {
        return "BusinessRule{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", category='" + category + '\'' +
                ", priority=" + priority +
                ", version=" + version +
                ", createdAt=" + createdAt +
                '}';
    }
}