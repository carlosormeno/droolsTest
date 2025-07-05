package com.example.droolsTest.entity.DTO;

import com.example.droolsTest.entity.*;
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
public class ParametricasVigentesDTO {
    private LocalDateTime fechaEvaluacion;
    private Integer anioVigencia;
    private Uit uitVigente;
    private List<TipoProcesoSeleccion> tiposProceso;
    private List<ObjetoContratacion> objetosContratacion;
    private List<OperadoresMonto> operadoresMonto;
    private List<Topes> topesVigentes;
}
