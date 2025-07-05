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
@Table(name = "topes")
@ParametricaEntity(
        value = "topes",
        description = "Topes de montos por combinación de proceso y objeto"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class Topes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "id_tipo_proceso_seleccion", nullable = false, insertable = false, updatable = false)
    private Long idTipoProcesoSeleccion;

    @Column(name = "id_objeto_contratacion", nullable = false, insertable = false, updatable = false)
    private Long idObjetoContratacion;

    @Column(name = "id_sub_descripcion_contratacion", insertable = false, updatable = false)
    private Long idSubDescripcionContratacion;

    @Column(name = "id_operador_monto", nullable = false, insertable = false, updatable = false)
    private Long idOperadorMonto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    @Column(name = "referencia_uit", precision = 8, scale = 4)
    private BigDecimal referenciaUit;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "anio_vigencia", nullable = false)
    private Integer anioVigencia;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

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

    // Relaciones JPA
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_proceso_seleccion", referencedColumnName = "id")
    @ToString.Exclude
    private TipoProcesoSeleccion tipoProcesoSeleccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_objeto_contratacion", referencedColumnName = "id")
    @ToString.Exclude
    private ObjetoContratacion objetoContratacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sub_descripcion_contratacion", referencedColumnName = "id")
    @ToString.Exclude
    private SubDescripcionContratacion subDescripcionContratacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_operador_monto", referencedColumnName = "id")
    @ToString.Exclude
    private OperadoresMonto operadorMonto;

    // Validaciones
    @PrePersist
    @PreUpdate
    private void validateTopes() {
        if (monto != null && monto.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto no puede ser negativo");
        }
        if (anioVigencia != null && (anioVigencia < 2020 || anioVigencia > 2030)) {
            throw new IllegalArgumentException("El año de vigencia debe estar entre 2020 y 2030");
        }
    }
}
