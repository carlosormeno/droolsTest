package com.example.droolsTest.service;

import com.example.droolsTest.entity.SubDescripcionContratacion;
import com.example.droolsTest.exception.OperationNotAllowedException;
import com.example.droolsTest.exception.ResourceNotFoundException;
import com.example.droolsTest.repository.SubDescripcionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SubDescripcionService {

    private final SubDescripcionRepository subDescripcionRepository;

    /**
     * Crear nueva sub-descripción
     */
    public SubDescripcionContratacion crearSubDescripcion(SubDescripcionContratacion subDescripcion, String usuario) {
        log.info("Creando sub-descripción: {}", subDescripcion.getCodigo());

        if (subDescripcionRepository.existsByCodigoAndEstadoRegistroTrue(subDescripcion.getCodigo())) {
            throw new OperationNotAllowedException("Ya existe una sub-descripción con código: " + subDescripcion.getCodigo());
        }

        subDescripcion.setCreatedBy(usuario);
        subDescripcion.setUpdatedBy(usuario);
        subDescripcion.setEstadoRegistro(true);

        return subDescripcionRepository.save(subDescripcion);
    }

    /**
     * Actualizar sub-descripción existente
     */
    public SubDescripcionContratacion actualizarSubDescripcion(Long id, SubDescripcionContratacion subDescripcionActualizada, String usuario) {
        SubDescripcionContratacion existente = obtenerPorId(id);

        if (subDescripcionRepository.existsByCodigoAndEstadoRegistroTrueAndIdNot(subDescripcionActualizada.getCodigo(), id)) {
            throw new OperationNotAllowedException("Ya existe otra sub-descripción con el código: " + subDescripcionActualizada.getCodigo());
        }

        existente.setCodigo(subDescripcionActualizada.getCodigo());
        existente.setNombre(subDescripcionActualizada.getNombre());
        existente.setDescripcion(subDescripcionActualizada.getDescripcion());
        existente.setEstado(subDescripcionActualizada.getEstado());
        existente.setUpdatedBy(usuario);

        return subDescripcionRepository.save(existente);
    }

    /**
     * Obtener sub-descripción por ID
     */
    @Transactional(readOnly = true)
    public SubDescripcionContratacion obtenerPorId(Long id) {
        return subDescripcionRepository.findByIdAndEstadoRegistroTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-descripción no encontrada con ID: " + id));
    }

    /**
     * Listar sub-descripciones activas sin paginación
     */
    @Transactional(readOnly = true)
    public List<SubDescripcionContratacion> obtenerSubDescripcionesActivas() {
        return subDescripcionRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO");
    }

    /**
     * Listar sub-descripciones activas con paginación
     */
    @Transactional(readOnly = true)
    public Page<SubDescripcionContratacion> obtenerSubDescripcionesActivas(Pageable pageable) {
        return subDescripcionRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO", pageable);
    }

    /**
     * Obtener sub-descripción por código
     */
    @Transactional(readOnly = true)
    public SubDescripcionContratacion obtenerPorCodigo(String codigo) {
        return subDescripcionRepository.findByCodigoAndEstadoRegistroTrue(codigo)
                .orElseThrow(() -> new ResourceNotFoundException("Sub-descripción no encontrada con código: " + codigo));
    }

    /**
     * Eliminación lógica de sub-descripción
     */
    public void eliminarSubDescripcion(Long id, String usuario) {
        SubDescripcionContratacion existente = obtenerPorId(id);

        // Verificar si tiene objetos de contratación asociados
        if (!existente.getObjetosContratacion().isEmpty()) {
            throw new OperationNotAllowedException(
                    "No se puede eliminar la sub-descripción porque tiene objetos de contratación asociados");
        }

        existente.setEstadoRegistro(false);
        existente.setUpdatedBy(usuario);
        subDescripcionRepository.save(existente);
        log.info("Sub-descripción eliminada lógicamente con ID: {}", id);
    }

    /**
     * Verificar si existe por código
     */
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        return subDescripcionRepository.existsByCodigoAndEstadoRegistroTrue(codigo);
    }
}
