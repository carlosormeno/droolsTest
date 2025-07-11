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
@RequestMapping("/api/operadores-monto")
@RequiredArgsConstructor
@Slf4j
public class OperadoresMontoController {

    private final OperadoresMontoService operadoresService;

    @PostMapping
    public ResponseEntity<OperadoresMonto> crearOperador(@Valid @RequestBody OperadoresMonto operador,
                                                         @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("POST /api/operadores-monto - Creando operador: {}", operador.getCodigo());
            OperadoresMonto creado = operadoresService.crearOperador(operador, usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(creado);
        } catch (IllegalArgumentException e) {
            log.warn("Error al crear operador: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<OperadoresMonto>> listarOperadoresActivos() {
        log.debug("GET /api/operadores-monto");
        List<OperadoresMonto> operadores = operadoresService.obtenerOperadoresActivos();
        return ResponseEntity.ok(operadores);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperadoresMonto> obtenerOperadorPorId(@PathVariable Long id) {
        try {
            log.debug("GET /api/operadores-monto/{}", id);
            OperadoresMonto operador = operadoresService.obtenerPorId(id);
            return ResponseEntity.ok(operador);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<OperadoresMonto> actualizarOperador(@PathVariable Long id,
                                                              @Valid @RequestBody OperadoresMonto operador,
                                                              @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("PUT /api/operadores-monto/{}", id);
            OperadoresMonto actualizado = operadoresService.actualizarOperador(id, operador, usuario);
            return ResponseEntity.ok(actualizado);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Error al actualizar operador: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarOperador(@PathVariable Long id,
                                                 @RequestParam(defaultValue = "SISTEMA") String usuario) {
        try {
            log.info("DELETE /api/operadores-monto/{}", id);
            operadoresService.eliminarOperador(id, usuario);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
