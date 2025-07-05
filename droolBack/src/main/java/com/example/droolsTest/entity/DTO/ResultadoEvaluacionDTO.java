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
public class ResultadoEvaluacionDTO {
    private Boolean evaluacionExitosa;
    private String mensaje;
    private ParametricasVigentesDTO parametricasUtilizadas;
    private List<String> reglasAplicadas;
    private java.util.Map<String, Object> resultadosDetalle;
    private LocalDateTime fechaEvaluacion;
}
