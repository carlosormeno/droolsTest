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
@RequestMapping("/api/operadores-monto")
@RequiredArgsConstructor
@Slf4j
public class OperadoresMontoController {

    private final OperadoresMontoService operadoresService;

    /**
     * Crear nuevo operador
     */
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

    /**
     * Listar operadores activos
     */
    @GetMapping
    public ResponseEntity<List<OperadoresMonto>> listarOperadoresActivos() {
        log.debug("GET /api/operadores-monto");
        List<OperadoresMonto> operadores = operadoresService.obtenerOperadoresActivos();
        return ResponseEntity.ok(operadores);
    }

    /**
     * Obtener operadores en uso
     */
    @GetMapping("/en-uso")
    public ResponseEntity<List<OperadoresMonto>> obtenerOperadoresEnUso() {
        log.debug("GET /api/operadores-monto/en-uso");
        List<OperadoresMonto> operadores = operadoresService.obtenerOperadoresEnUso();
        return ResponseEntity.ok(operadores);
    }

}
