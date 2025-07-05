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
@RequestMapping("/api/objeto-contratacion")
@RequiredArgsConstructor
@Slf4j
public class ObjetoContratacionController {

    private final ObjetoContratacionService objetoService;

    /**
     * Crear nuevo objeto de contratación
     */
    @PostMapping
    public ResponseEntity<ObjetoContratacion> crearObjeto(@Valid @RequestBody ObjetoContratacion objeto,
                                                          @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/objeto-contratacion - Creando objeto: {}", objeto.getCodigo());
            ObjetoContratacion creado = objetoService.crearObjeto(objeto, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al crear objeto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Listar objetos activos
     */
    @GetMapping
    public ResponseEntity<List<ObjetoContratacion>> listarObjetosActivos() {
        log.debug("GET /api/objeto-contratacion");
        List<ObjetoContratacion> objetos = objetoService.obtenerObjetosActivos();
        return ResponseEntity.ok(objetos);
    }

    /**
     * Obtener objetos que permiten sub-descripción
     */
    @GetMapping("/con-sub-descripcion")
    public ResponseEntity<List<ObjetoContratacion>> obtenerObjetosConSubDescripcion() {
        log.debug("GET /api/objeto-contratacion/con-sub-descripcion");
        List<ObjetoContratacion> objetos = objetoService.obtenerObjetosConSubDescripcion();
        return ResponseEntity.ok(objetos);
    }

    /**
     * Obtener objeto con sub-descripciones cargadas
     */
    @GetMapping("/{id}/completo")
    public ResponseEntity<ObjetoContratacion> obtenerObjetoCompleto(@PathVariable Long id) {
        log.debug("GET /api/objeto-contratacion/{}/completo", id);
        Optional<ObjetoContratacion> objeto = objetoService.obtenerObjetoConSubDescripciones(id);
        return objeto.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualizar objeto
     */
    @PutMapping("/{id}")
    public ResponseEntity<ObjetoContratacion> actualizarObjeto(@PathVariable Long id,
                                                               @Valid @RequestBody ObjetoContratacion objeto,
                                                               @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/objeto-contratacion/{}", id);
            ObjetoContratacion actualizado = objetoService.actualizarObjeto(id, objeto, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar objeto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
