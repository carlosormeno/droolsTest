package com.example.droolsTest.entity.DTO;

import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionParametricasDTO {
    private LocalDateTime fechaEvaluacion;
    private Long tipoProcesoId;
    private Long objetoContratacionId;
    private Long subDescripcionId;
    private Long operadorMontoId;
    private BigDecimal montoContrato;
    private Integer anioVigencia;

    // Parámetros adicionales para reglas
    private java.util.Map<String, Object> parametrosAdicionales;
}
