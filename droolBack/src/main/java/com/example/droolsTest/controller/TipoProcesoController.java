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
    public ResponseEntity<List<TipoProcesoSeleccion>> obtenerTiposProceso(@RequestParam Integer anio) {
        log.debug("GET /api/tipo-proceso?anio={}", anio);
        List<TipoProcesoSeleccion> tipos = tipoProcesoService.obtenerTiposProcesoActivos(anio);
        return ResponseEntity.ok(tipos);
    }

    /**
     * Obtener años disponibles
     */
    @GetMapping("/anios")
    public ResponseEntity<List<Integer>> obtenerAniosDisponibles() {
        log.debug("GET /api/tipo-proceso/anios");
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
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar tipo proceso: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
