package com.example.droolsTest.controller;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.entity.DTO.*;
import com.example.droolsTest.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping("/api/parametricas")
@RequiredArgsConstructor
@Slf4j
public class ParametricasController {

    private final ParametricasService parametricasService;
    private final CatalogoParametricasService catalogoParametricasService;

    /**
     * Obtener todas las paramétricas vigentes para una fecha
     */
    @GetMapping("/vigentes")
    public ResponseEntity<ParametricasVigentesDTO> obtenerParametricasVigentes(@RequestParam String fecha) {
        try {
            log.info("GET /api/parametricas/vigentes?fecha={}", fecha);
            LocalDate fechaParsed = LocalDate.parse(fecha);
            ParametricasVigentesDTO parametricas = parametricasService.obtenerParametricasVigentes(fechaParsed);
            return ResponseEntity.ok(parametricas);
        } catch (Exception e) {
            log.error("Error al obtener paramétricas vigentes: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Evaluar monto completo con resultado estructurado
     */
    @PostMapping("/evaluar")
    public ResponseEntity<ResultadoEvaluacionDTO> evaluarMonto(@Valid @RequestBody EvaluacionParametricasDTO evaluacion) {
        try {
            log.info("POST /api/parametricas/evaluar - Evaluando monto: {}", evaluacion.getMontoContrato());
            ResultadoEvaluacionDTO resultado = parametricasService.evaluarMonto(evaluacion);
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            log.error("Error al evaluar monto: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener resumen de paramétricas por año
     */
    @GetMapping("/resumen")
    public ResponseEntity<List<Object[]>> obtenerResumenPorAnio() {
        log.debug("GET /api/parametricas/resumen");
        List<Object[]> resumen = parametricasService.obtenerResumenPorAnio();
        return ResponseEntity.ok(resumen);
    }

    /**
     * Health check del sistema de paramétricas
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        try {
            // Verificar que el sistema está funcionando obteniendo paramétricas del año actual
            LocalDate hoy = LocalDate.now();
            ParametricasVigentesDTO parametricas = parametricasService.obtenerParametricasVigentes(hoy);

            String status = String.format(
                    "Sistema de Paramétricas OK - UIT: %s, Tipos Proceso: %d, Objetos: %d, Operadores: %d, Topes: %d",
                    parametricas.getUitVigente() != null ? "Disponible" : "No disponible",
                    parametricas.getTiposProceso().size(),
                    parametricas.getObjetosContratacion().size(),
                    parametricas.getOperadoresMonto().size(),
                    parametricas.getTopesVigentes().size()
            );

            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Error en health check: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Sistema de Paramétricas ERROR: " + e.getMessage());
        }
    }

    // Agregar al ParametricasController existente
    @GetMapping("/catalogo-completo")
    public ResponseEntity<CatalogoParametricasDTO> obtenerCatalogoCompleto() {
        try {
            log.debug("GET /api/parametricas/catalogo-completo");
            CatalogoParametricasDTO catalogo = catalogoParametricasService.obtenerCatalogoCompleto();

            log.info("Catálogo generado con {} paramétricas", catalogo.getTotalParametricas());
            return ResponseEntity.ok(catalogo);

        } catch (Exception e) {
            log.error("Error generando catálogo completo: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/parametrica/{nombre}/detalle")
    public ResponseEntity<ParametricaMetadata> obtenerDetalleParametrica(@PathVariable String nombre) {
        try {
            log.debug("GET /api/parametricas/parametrica/{}/detalle", nombre);
            ParametricaMetadata detalle = catalogoParametricasService.obtenerDetalleParametrica(nombre);
            return ResponseEntity.ok(detalle);

        } catch (IllegalArgumentException e) {
            log.warn("Paramétrica no encontrada: {}", nombre);
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("Error obteniendo detalle de paramétrica {}: {}", nombre, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/nombres")
    public ResponseEntity<Set<String>> obtenerNombresParametricas() {
        try {
            Set<String> nombres = catalogoParametricasService.obtenerNombresParametricas();
            return ResponseEntity.ok(nombres);
        } catch (Exception e) {
            log.error("Error obteniendo nombres de paramétricas: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
