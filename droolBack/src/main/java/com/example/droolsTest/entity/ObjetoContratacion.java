package com.example.droolsTest.entity;

import com.example.droolsTest.annotation.ParametricaEntity;
import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "objeto_contratacion")
@ParametricaEntity(
        value = "objeto_contratacion",
        description = "Objetos de contratación (Bienes, Servicios, Obras)"
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
public class ObjetoContratacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 50)
    private String nombre;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "permite_sub_descripcion", nullable = false)
    @Builder.Default
    private Boolean permiteSubDescripcion = Boolean.FALSE;

    @Column(name = "estado", nullable = false, length = 20)
    @Builder.Default
    private String estado = "ACTIVO";

    @Column(name = "id_sub_descripcion_contratacion", insertable = false)
    private Long idSubDescripcionContratacion;

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

    // Relación con SubDescripcionContratacion (muchos a uno) - CORREGIDO SEGÚN SQL
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sub_descripcion_contratacion", nullable = true,insertable = false, updatable = false)
    @ToString.Exclude
    @JsonBackReference("subdescripcion-objetos")
    private SubDescripcionContratacion subDescripcionContratacion;

    // Relación con Topes
    @OneToMany(mappedBy = "objetoContratacion",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH},
            orphanRemoval = false,
            fetch = FetchType.LAZY)
    @Builder.Default
    @ToString.Exclude
    private List<Topes> topes = new ArrayList<>();
}