package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.entity.DTO.*;
import com.example.droolsTest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParametricasService {

    private final UitService uitService;
    private final TipoProcesoService tipoProcesoService;
    private final ObjetoContratacionService objetoService;
    private final OperadoresMontoService operadoresService;
    private final TopesService topesService;

    /**
     * Obtener todas las paramétricas vigentes para una fecha específica
     */
    @Transactional(readOnly = true)
    public ParametricasVigentesDTO obtenerParametricasVigentes(LocalDate fecha) {
        log.info("Obteniendo paramétricas vigentes para fecha: {}", fecha);

        Integer anio = fecha.getYear();

        // Obtener UIT vigente
        Optional<Uit> uitVigente = uitService.obtenerUitVigente(fecha);

        // Obtener tipos de proceso activos para el año
        List<TipoProcesoSeleccion> tiposProceso = tipoProcesoService.obtenerTiposProcesoActivos(anio);

        // Obtener objetos de contratación activos
        List<ObjetoContratacion> objetosContratacion = objetoService.obtenerObjetosActivos();

        // Obtener operadores activos
        List<OperadoresMonto> operadoresMonto = operadoresService.obtenerOperadoresActivos();

        // Obtener topes vigentes para el año
        List<Topes> topesVigentes = topesService.obtenerTopesVigentes(anio);

        log.info("Paramétricas obtenidas - UIT: {}, Tipos Proceso: {}, Objetos: {}, Operadores: {}, Topes: {}",
                uitVigente.isPresent() ? "Sí" : "No", tiposProceso.size(), objetosContratacion.size(),
                operadoresMonto.size(), topesVigentes.size());

        return ParametricasVigentesDTO.builder()
                .fechaEvaluacion(fecha.atStartOfDay())
                .anioVigencia(anio)
                .uitVigente(uitVigente.orElse(null))
                .tiposProceso(tiposProceso)
                .objetosContratacion(objetosContratacion)
                .operadoresMonto(operadoresMonto)
                .topesVigentes(topesVigentes)
                .build();
    }

    /**
     * Evaluar un monto contra las paramétricas vigentes
     */
    @Transactional(readOnly = true)
    public ResultadoEvaluacionDTO evaluarMonto(EvaluacionParametricasDTO evaluacion) {
        log.info("Evaluando monto {} para fecha {}", evaluacion.getMontoContrato(), evaluacion.getFechaEvaluacion());

        try {
            Integer anio = evaluacion.getAnioVigencia() != null ?
                    evaluacion.getAnioVigencia() :
                    evaluacion.getFechaEvaluacion().getYear();

            // Obtener paramétricas vigentes
            ParametricasVigentesDTO parametricas = obtenerParametricasVigentes(LocalDate.of(anio, 1, 1));

            // Evaluar topes aplicables
            List<Topes> topesAplicables = topesService.evaluarMontoContraTopes(evaluacion.getMontoContrato(), anio);

            // Construir resultado
            boolean evaluacionExitosa = !topesAplicables.isEmpty();
            String mensaje = evaluacionExitosa ?
                    "Se encontraron " + topesAplicables.size() + " topes aplicables" :
                    "No se encontraron topes aplicables para el monto especificado";

            return ResultadoEvaluacionDTO.builder()
                    .evaluacionExitosa(evaluacionExitosa)
                    .mensaje(mensaje)
                    .parametricasUtilizadas(parametricas)
                    .reglasAplicadas(topesAplicables.stream()
                            .map(t -> t.getTipoProcesoSeleccion().getNombre() + " - " +
                                    t.getObjetoContratacion().getNombre())
                            .toList())
                    .fechaEvaluacion(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            log.error("Error al evaluar monto: {}", e.getMessage(), e);

            return ResultadoEvaluacionDTO.builder()
                    .evaluacionExitosa(false)
                    .mensaje("Error en la evaluación: " + e.getMessage())
                    .fechaEvaluacion(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Obtener resumen de paramétricas por año
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerResumenPorAnio() {
        return topesService.obtenerEstadisticasPorAnio();
    }

}
