package com.example.droolsTest.controller;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.EntityNotFoundException;
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
@RequestMapping("/api/uit")
@RequiredArgsConstructor
@Slf4j
public class UitController {

    private final UitService uitService;

    /**
     * Crear nueva UIT
     */
    @PostMapping
    public ResponseEntity<Uit> crearUit(@Valid @RequestBody Uit uit,
                                        @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/uit - Creando UIT para año: {}", uit.getAnioVigencia());
            Uit uitCreada = uitService.crearUit(uit, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(uitCreada);
        } catch (IllegalArgumentException e) {
            log.warn("Error al crear UIT: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            log.error("Error interno al crear UIT: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Uit> obtenerUitPorId(@PathVariable Long id) {
        try {
            log.debug("GET /api/uit/{}", id);
            Uit uit = uitService.obtenerPorId(id);
            return ResponseEntity.ok(uit);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Listar todas las UITs activas
     */
    @GetMapping
    public ResponseEntity<List<Uit>> listarUitsActivas() {
        log.debug("GET /api/uit - Listando UITs activas");
        List<Uit> uits = uitService.listarUitsActivas();
        return ResponseEntity.ok(uits);
    }

    /**
     * Obtener UIT vigente para una fecha específica
     */
    @GetMapping("/vigente")
    public ResponseEntity<Uit> obtenerUitVigente(@RequestParam String fecha) {
        try {
            log.debug("GET /api/uit/vigente?fecha={}", fecha);
            LocalDate fechaParsed = LocalDate.parse(fecha);
            Optional<Uit> uit = uitService.obtenerUitVigente(fechaParsed);
            return uit.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.warn("Error al parsear fecha: {}", fecha);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Obtener UITs por rango de años
     */
    @GetMapping("/rango")
    public ResponseEntity<List<Uit>> obtenerUitsPorRango(@RequestParam Integer anioInicio,
                                                         @RequestParam Integer anioFin) {
        log.debug("GET /api/uit/rango?anioInicio={}&anioFin={}", anioInicio, anioFin);
        List<Uit> uits = uitService.obtenerUitsPorRango(anioInicio, anioFin);
        return ResponseEntity.ok(uits);
    }

    /**
     * Actualizar UIT
     */
    @PutMapping("/{id}")
    public ResponseEntity<Uit> actualizarUit(@PathVariable Long id,
                                             @Valid @RequestBody Uit uit,
                                             @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/uit/{} - Actualizando UIT", id);
            Uit uitActualizada = uitService.actualizarUit(id, uit, usuario);
            return ResponseEntity.ok(uitActualizada);
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar UIT: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error interno al actualizar UIT: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar UIT (borrado lógico)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUit(@PathVariable Long id,
                                            @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("DELETE /api/uit/{} - Eliminando UIT", id);
            uitService.eliminarUit(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error al eliminar UIT: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error interno al eliminar UIT: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
