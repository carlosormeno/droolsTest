package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
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
public class ObjetoContratacionService {

    private final ObjetoContratacionRepository objetoRepository;

    /**
     * Crear nuevo objeto de contratación
     */
    public ObjetoContratacion crearObjeto(ObjetoContratacion objeto, String usuario) {
        log.info("Creando objeto de contratación: {}", objeto.getCodigo());

        if (objetoRepository.existsByCodigoAndEstadoRegistroTrueAndIdNot(objeto.getCodigo(), null)) {
            throw new IllegalArgumentException("Ya existe un objeto con código: " + objeto.getCodigo());
        }

        objeto.setCreatedBy(usuario);
        objeto.setUpdatedBy(usuario);

        return objetoRepository.save(objeto);
    }

    /**
     * Actualizar objeto de contratación
     */
    public ObjetoContratacion actualizarObjeto(Long id, ObjetoContratacion objetoActualizado, String usuario) {
        ObjetoContratacion existente = objetoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Objeto no encontrado con ID: " + id));

        if (objetoRepository.existsByCodigoAndEstadoRegistroTrueAndIdNot(objetoActualizado.getCodigo(), id)) {
            throw new IllegalArgumentException("Ya existe otro objeto con el mismo código");
        }

        existente.setCodigo(objetoActualizado.getCodigo());
        existente.setNombre(objetoActualizado.getNombre());
        existente.setDescripcion(objetoActualizado.getDescripcion());
        existente.setPermiteSubDescripcion(objetoActualizado.getPermiteSubDescripcion());
        existente.setEstado(objetoActualizado.getEstado());
        existente.setUpdatedBy(usuario);

        return objetoRepository.save(existente);
    }

    /**
     * Obtener objetos activos
     */
    @Transactional(readOnly = true)
    public List<ObjetoContratacion> obtenerObjetosActivos() {
        return objetoRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO");
    }

    /**
     * Obtener objeto con sub-descripciones
     */
    @Transactional(readOnly = true)
    public Optional<ObjetoContratacion> obtenerObjetoConSubDescripciones(Long id) {
        return objetoRepository.findByIdWithSubDescripciones(id);
    }

    /**
     * Obtener objetos que permiten sub-descripción
     */
    @Transactional(readOnly = true)
    public List<ObjetoContratacion> obtenerObjetosConSubDescripcion() {
        return objetoRepository.findByPermiteSubDescripcionTrueAndEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO");
    }

}
