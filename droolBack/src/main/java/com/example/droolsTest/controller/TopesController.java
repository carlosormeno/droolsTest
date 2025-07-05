package com.example.droolsTest.controller;

import com.example.droolsTest.entity.*;
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

@RestController
@RequestMapping("/api/topes")
@RequiredArgsConstructor
@Slf4j
public class TopesController {

    private final TopesService topesService;

    /**
     * Crear nuevo tope
     */
    @PostMapping
    public ResponseEntity<Topes> crearTope(@Valid @RequestBody Topes tope,
                                           @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/topes - Creando tope para año: {}", tope.getAnioVigencia());
            Topes creado = topesService.crearTope(tope, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al crear tope: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener topes vigentes por año
     */
    @GetMapping
    public ResponseEntity<List<Topes>> obtenerTopesVigentes(@RequestParam Integer anio) {
        log.debug("GET /api/topes?anio={}", anio);
        List<Topes> topes = topesService.obtenerTopesVigentes(anio);
        return ResponseEntity.ok(topes);
    }

    /**
     * Evaluar monto contra topes
     */
    @GetMapping("/evaluar")
    public ResponseEntity<List<Topes>> evaluarMonto(@RequestParam BigDecimal monto,
                                                    @RequestParam Integer anio) {
        log.debug("GET /api/topes/evaluar?monto={}&anio={}", monto, anio);
        List<Topes> topesAplicables = topesService.evaluarMontoContraTopes(monto, anio);
        return ResponseEntity.ok(topesAplicables);
    }

    /**
     * Buscar tope específico
     */
    @GetMapping("/buscar")
    public ResponseEntity<Topes> buscarTopeEspecifico(@RequestParam Long tipoProcesoId,
                                                      @RequestParam Long objetoId,
                                                      @RequestParam(required = false) Long subDescripcionId,
                                                      @RequestParam Long operadorId,
                                                      @RequestParam Integer anio) {
        log.debug("GET /api/topes/buscar - Buscando tope específico");
        Optional<Topes> tope = topesService.buscarTopeEspecifico(tipoProcesoId, objetoId, subDescripcionId, operadorId, anio);
        return tope.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener estadísticas por año
     */
    @GetMapping("/estadisticas")
    public ResponseEntity<List<Object[]>> obtenerEstadisticas() {
        log.debug("GET /api/topes/estadisticas");
        List<Object[]> estadisticas = topesService.obtenerEstadisticasPorAnio();
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Actualizar tope
     */
    @PutMapping("/{id}")
    public ResponseEntity<Topes> actualizarTope(@PathVariable Long id,
                                                @Valid @RequestBody Topes tope,
                                                @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/topes/{}", id);
            Topes actualizado = topesService.actualizarTope(id, tope, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar tope: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
