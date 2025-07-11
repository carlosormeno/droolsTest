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
@RequestMapping("/api/objeto-contratacion")
@RequiredArgsConstructor
@Slf4j
public class ObjetoContratacionController {

    private final ObjetoContratacionService objetoService;

    @PostMapping
    public ResponseEntity<ObjetoContratacion> crearObjeto(@Valid @RequestBody ObjetoContratacion objeto,
                                                          @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/objeto-contratacion - Creando objeto: {}", objeto.getCodigo());
            ObjetoContratacion creado = objetoService.crearObjeto(objeto, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (OperationNotAllowedException e) {
            log.warn("Error al crear objeto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ObjetoContratacion>> listarObjetosActivos() {
        log.debug("GET /api/objeto-contratacion");
        List<ObjetoContratacion> objetos = objetoService.obtenerObjetosActivos();
        return ResponseEntity.ok(objetos);
    }

    @GetMapping("/paginado")
    public ResponseEntity<Page<ObjetoContratacion>> listarObjetosPaginado(Pageable pageable) {
        log.debug("GET /api/objeto-contratacion/paginado");
        Page<ObjetoContratacion> objetos = objetoService.obtenerObjetosActivos(pageable);
        return ResponseEntity.ok(objetos);
    }

    @GetMapping("/con-sub-descripcion")
    public ResponseEntity<Page<ObjetoContratacion>> obtenerObjetosConSubDescripcion(Pageable pageable) {
        log.debug("GET /api/objeto-contratacion/con-sub-descripcion");
        Page<ObjetoContratacion> objetos = objetoService.obtenerObjetosConSubDescripcion(pageable);
        return ResponseEntity.ok(objetos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObjetoContratacion> obtenerObjetoPorId(@PathVariable Long id) {
        try {
            log.debug("GET /api/objeto-contratacion/{}", id);
            ObjetoContratacion objeto = objetoService.obtenerPorId(id);
            return ResponseEntity.ok(objeto);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObjetoContratacion> actualizarObjeto(@PathVariable Long id,
                                                               @Valid @RequestBody ObjetoContratacion objeto,
                                                               @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/objeto-contratacion/{}", id);
            ObjetoContratacion actualizado = objetoService.actualizarObjeto(id, objeto, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (OperationNotAllowedException e) {
            log.warn("Error al actualizar objeto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarObjeto(@PathVariable Long id,
                                               @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("DELETE /api/objeto-contratacion/{}", id);
            objetoService.eliminarObjeto(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (OperationNotAllowedException e) {
            log.warn("Error al eliminar objeto: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

}
