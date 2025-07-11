package com.example.droolsTest.controller;

import com.example.droolsTest.entity.SubDescripcionContratacion;
import com.example.droolsTest.exception.OperationNotAllowedException;
import com.example.droolsTest.exception.ResourceNotFoundException;
import com.example.droolsTest.service.SubDescripcionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/sub-descripcion")
@RequiredArgsConstructor
@Slf4j
public class SubDescripcionController {

    private final SubDescripcionService subDescripcionService;

    /**
     * Crear nueva sub-descripción
     */
    @PostMapping
    public ResponseEntity<SubDescripcionContratacion> crearSubDescripcion(
            @Valid @RequestBody SubDescripcionContratacion subDescripcion,
            @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/sub-descripcion - Creando sub-descripción: {}", subDescripcion.getCodigo());
            SubDescripcionContratacion creada = subDescripcionService.crearSubDescripcion(subDescripcion, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creada);
        } catch (OperationNotAllowedException e) {
            log.warn("Error al crear sub-descripción: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Listar sub-descripciones activas sin paginación
     */
    @GetMapping
    public ResponseEntity<List<SubDescripcionContratacion>> listarSubDescripcionesActivas() {
        log.debug("GET /api/sub-descripcion");
        List<SubDescripcionContratacion> subDescripciones = subDescripcionService.obtenerSubDescripcionesActivas();
        return ResponseEntity.ok(subDescripciones);
    }

    /**
     * Listar sub-descripciones activas con paginación
     */
    @GetMapping("/paginado")
    public ResponseEntity<Page<SubDescripcionContratacion>> listarSubDescripcionesPaginado(Pageable pageable) {
        log.debug("GET /api/sub-descripcion/paginado");
        Page<SubDescripcionContratacion> subDescripciones = subDescripcionService.obtenerSubDescripcionesActivas(pageable);
        return ResponseEntity.ok(subDescripciones);
    }

    /**
     * Obtener sub-descripción por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<SubDescripcionContratacion> obtenerSubDescripcionPorId(@PathVariable Long id) {
        try {
            log.debug("GET /api/sub-descripcion/{}", id);
            SubDescripcionContratacion subDescripcion = subDescripcionService.obtenerPorId(id);
            return ResponseEntity.ok(subDescripcion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtener sub-descripción por código
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<SubDescripcionContratacion> obtenerSubDescripcionPorCodigo(@PathVariable String codigo) {
        try {
            log.debug("GET /api/sub-descripcion/codigo/{}", codigo);
            SubDescripcionContratacion subDescripcion = subDescripcionService.obtenerPorCodigo(codigo);
            return ResponseEntity.ok(subDescripcion);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Verificar si existe sub-descripción por código
     */
    @GetMapping("/existe/{codigo}")
    public ResponseEntity<Boolean> existeSubDescripcionPorCodigo(@PathVariable String codigo) {
        log.debug("GET /api/sub-descripcion/existe/{}", codigo);
        boolean existe = subDescripcionService.existePorCodigo(codigo);
        return ResponseEntity.ok(existe);
    }

    /**
     * Actualizar sub-descripción
     */
    @PutMapping("/{id}")
    public ResponseEntity<SubDescripcionContratacion> actualizarSubDescripcion(
            @PathVariable Long id,
            @Valid @RequestBody SubDescripcionContratacion subDescripcion,
            @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/sub-descripcion/{}", id);
            SubDescripcionContratacion actualizada = subDescripcionService.actualizarSubDescripcion(id, subDescripcion, usuario);
            return ResponseEntity.ok(actualizada);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (OperationNotAllowedException e) {
            log.warn("Error al actualizar sub-descripción: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Eliminar sub-descripción (eliminación lógica)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarSubDescripcion(@PathVariable Long id,
                                                       @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("DELETE /api/sub-descripcion/{}", id);
            subDescripcionService.eliminarSubDescripcion(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (OperationNotAllowedException e) {
            log.warn("Error al eliminar sub-descripción: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}