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
@Table(name = "tipo_proceso_seleccion")
@ParametricaEntity(
        value = "tipo_proceso",
        description = "Tipos de proceso de selección según normativa OSCE"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class TipoProcesoSeleccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

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

    // Relación con Topes (lazy loading)
    @OneToMany(mappedBy = "tipoProcesoSeleccion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude // Evitar ciclos en toString
    private List<Topes> topes;
}
