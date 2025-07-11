package com.example.droolsTest.controller;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.ResourceNotFoundException;
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
@RequestMapping("/api/tipo-proceso")
@RequiredArgsConstructor
@Slf4j
public class TipoProcesoController {

    private final TipoProcesoService tipoProcesoService;

    /**
     * Crear nuevo tipo de proceso
     */
    @PostMapping
    public ResponseEntity<TipoProcesoSeleccion> crearTipoProceso(@Valid @RequestBody TipoProcesoSeleccion tipoProceso,
                                                                 @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/tipo-proceso - Creando tipo proceso: {}", tipoProceso.getCodigo());
            TipoProcesoSeleccion creado = tipoProcesoService.crearTipoProceso(tipoProceso, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al crear tipo proceso: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener tipos de proceso por año
     */
    @GetMapping
    public ResponseEntity<List<TipoProcesoSeleccion>> obtenerTiposProceso(@RequestParam(required = false) Integer anio) {
        log.debug("GET /api/tipo-proceso?anio={}", anio);
        List<TipoProcesoSeleccion> tipos;

        if (anio != null) {
            // Si se proporciona año, filtrar por año
            tipos = tipoProcesoService.obtenerTiposProcesoActivos(anio);
            log.debug("Obtenidos {} tipos de proceso para año {}", tipos.size(), anio);
        } else {
            // Si no se proporciona año, obtener todos los activos
            tipos = tipoProcesoService.obtenerTodosTiposProcesoActivos();
            log.debug("Obtenidos {} tipos de proceso (todos los años)", tipos.size());
        }
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/activos")
    public ResponseEntity<List<TipoProcesoSeleccion>> listarTiposProceso() {
        log.debug("GET /api/tipo-proceso");
        List<TipoProcesoSeleccion> tipos = tipoProcesoService.obtenerTodosTiposProcesoActivos();
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/por-anio/{anio}")
    public ResponseEntity<List<TipoProcesoSeleccion>> obtenerTiposPorAnio(@PathVariable Integer anio) {
        log.debug("GET /api/tipo-proceso/por-anio/{}", anio);
        List<TipoProcesoSeleccion> tipos = tipoProcesoService.obtenerTiposProcesoActivos(anio);
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/anios-disponibles")
    public ResponseEntity<List<Integer>> obtenerAniosDisponibles() {
        log.debug("GET /api/tipo-proceso/anios-disponibles");
        List<Integer> anios = tipoProcesoService.obtenerAniosDisponibles();
        return ResponseEntity.ok(anios);
    }

    /**
     * Obtener tipos de proceso con topes vigentes
     */
    @GetMapping("/con-topes")
    public ResponseEntity<List<TipoProcesoSeleccion>> obtenerTiposProcesoConTopes(@RequestParam Integer anio) {
        log.debug("GET /api/tipo-proceso/con-topes?anio={}", anio);
        List<TipoProcesoSeleccion> tipos = tipoProcesoService.obtenerTiposProcesoConTopes(anio);
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoProcesoSeleccion> obtenerTipoProcesoPorId(@PathVariable Long id) {
        try {
            log.debug("GET /api/tipo-proceso/{}", id);
            TipoProcesoSeleccion tipo = tipoProcesoService.obtenerPorId(id);
            return ResponseEntity.ok(tipo);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualizar tipo de proceso
     */
    @PutMapping("/{id}")
    public ResponseEntity<TipoProcesoSeleccion> actualizarTipoProceso(@PathVariable Long id,
                                                                      @Valid @RequestBody TipoProcesoSeleccion tipoProceso,
                                                                      @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/tipo-proceso/{}", id);
            TipoProcesoSeleccion actualizado = tipoProcesoService.actualizarTipoProceso(id, tipoProceso, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar tipo proceso: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar tipo de proceso (borrado lógico)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTipoProceso(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("DELETE /api/tipo-proceso/{}", id);
            tipoProcesoService.eliminarTipoProceso(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error al eliminar tipo proceso: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
