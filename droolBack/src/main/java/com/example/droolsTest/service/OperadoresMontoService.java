package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.ResourceNotFoundException;
import com.example.droolsTest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OperadoresMontoService {

    private final OperadoresMontoRepository operadoresRepository;

    /**
     * Crear nuevo operador
     */
    public OperadoresMonto crearOperador(OperadoresMonto operador, String usuario) {
        log.info("Creando operador: {}", operador.getCodigo());

        if (operadoresRepository.existsByCodigoAndEstadoRegistroTrueAndIdNot(operador.getCodigo(), null)) {
            throw new IllegalArgumentException("Ya existe un operador con código: " + operador.getCodigo());
        }

        if (operadoresRepository.existsBySimboloAndEstadoRegistroTrueAndIdNot(operador.getSimbolo(), null)) {
            throw new IllegalArgumentException("Ya existe un operador con símbolo: " + operador.getSimbolo());
        }

        operador.setCreatedBy(usuario);
        operador.setUpdatedBy(usuario);
        operador.setEstadoRegistro(true);

        return operadoresRepository.save(operador);
    }

    public OperadoresMonto actualizarOperador(Long id, OperadoresMonto operadorActualizado, String usuario) {
        OperadoresMonto existente = obtenerPorId(id);

        if (operadoresRepository.existsByCodigoAndEstadoRegistroTrueAndIdNot(operadorActualizado.getCodigo(), id)) {
            throw new IllegalArgumentException("Ya existe otro operador con el código: " + operadorActualizado.getCodigo());
        }

        existente.setCodigo(operadorActualizado.getCodigo());
        existente.setNombre(operadorActualizado.getNombre());
        existente.setSimbolo(operadorActualizado.getSimbolo());
        existente.setDescripcion(operadorActualizado.getDescripcion());
        existente.setEstado(operadorActualizado.getEstado());
        existente.setUpdatedBy(usuario);

        return operadoresRepository.save(existente);
    }

    @Transactional(readOnly = true)
    public OperadoresMonto obtenerPorId(Long id) {
        return operadoresRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Operador no encontrado con ID: " + id));
    }

    /**
     * Obtener operadores activos
     */
    @Transactional(readOnly = true)
    public List<OperadoresMonto> obtenerOperadoresActivos() {
        return operadoresRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO");
    }

    /**
     * Obtener operadores en uso

    @Transactional(readOnly = true)
    public List<OperadoresMonto> obtenerOperadoresEnUso() {
        return operadoresRepository.findOperadoresEnUso();
    }*/

    public void eliminarOperador(Long id, String usuario) {
        OperadoresMonto existente = obtenerPorId(id);
        existente.setEstadoRegistro(false);
        existente.setUpdatedBy(usuario);
        operadoresRepository.save(existente);
    }

}
