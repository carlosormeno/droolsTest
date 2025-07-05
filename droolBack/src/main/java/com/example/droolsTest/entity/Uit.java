package com.example.droolsTest.entity;

import com.example.droolsTest.annotation.ParametricaEntity;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "uit")
@ParametricaEntity(
        value = "uit",
        description = "Unidad Impositiva Tributaria por año fiscal"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Uit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "anio_vigencia", nullable = false)
    private Integer anioVigencia;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    // Auditoría
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "estado_registro", nullable = false)
    @Builder.Default
    private Boolean estadoRegistro = Boolean.TRUE;

    // Validaciones JPA
    @PrePersist
    @PreUpdate
    private void validateUit() {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto de la UIT debe ser mayor a cero");
        }
        if (anioVigencia != null && (anioVigencia < 2020 || anioVigencia > 2030)) {
            throw new IllegalArgumentException("El año de vigencia debe estar entre 2020 y 2030");
        }
    }
}
