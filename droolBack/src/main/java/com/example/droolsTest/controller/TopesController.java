package com.example.droolsTest.controller;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.OperationNotAllowedException;
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
@RequestMapping("/api/topes")
@RequiredArgsConstructor
@Slf4j
public class TopesController {

    private final TopesService topesService;

    @PostMapping
    public ResponseEntity<Topes> crearTope(@Valid @RequestBody Topes tope,
                                           @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/topes - Creando tope para año: {}", tope.getAnioVigencia());
            Topes creado = topesService.crearTope(tope, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (OperationNotAllowedException e) {
            log.warn("Error al crear tope: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<Topes>> obtenerTopesVigentes(@RequestParam(required = false) Integer anio) {
        log.debug("GET /api/topes?anio={}", anio);
        List<Topes> topes;

        if (anio != null) {
            topes = topesService.obtenerTopesVigentes(anio);
            log.debug("Obtenidos {} topes de proceso para año {}", topes.size(), anio);
        } else {
            topes = topesService.obtenerTodosTopesActivos();
            log.debug("Obtenidos {} tipos de proceso (todos los años)", topes.size());
        }

        return ResponseEntity.ok(topes);
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<Topes>> obtenerTopesVigentesPaginado(@RequestParam Integer anio, Pageable pageable) {
        log.debug("GET /api/topes/paginado?anio={}", anio);
        Page<Topes> topes = topesService.obtenerTopesVigentes(anio, pageable);
        return ResponseEntity.ok(topes);
    }

    @GetMapping("/evaluar")
    public ResponseEntity<List<Topes>> evaluarMonto(@RequestParam BigDecimal monto,
                                                    @RequestParam Integer anio) {
        log.debug("GET /api/topes/evaluar?monto={}&anio={}", monto, anio);
        List<Topes> topesAplicables = topesService.evaluarMontoContraTopes(monto, anio);
        return ResponseEntity.ok(topesAplicables);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topes> obtenerTopePorId(@PathVariable Long id) {
        try {
            log.debug("GET /api/topes/{}", id);
            Topes tope = topesService.obtenerPorId(id);
            return ResponseEntity.ok(tope);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<List<Object[]>> obtenerEstadisticas() {
        log.debug("GET /api/topes/estadisticas");
        List<Object[]> estadisticas = topesService.obtenerEstadisticasPorAnio();
        return ResponseEntity.ok(estadisticas);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topes> actualizarTope(@PathVariable Long id,
                                                @Valid @RequestBody Topes tope,
                                                @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/topes/{}", id);
            Topes actualizado = topesService.actualizarTope(id, tope, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (OperationNotAllowedException e) {
            log.warn("Error al actualizar tope: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTope(@PathVariable Long id,
                                             @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("DELETE /api/topes/{}", id);
            topesService.eliminarTope(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
