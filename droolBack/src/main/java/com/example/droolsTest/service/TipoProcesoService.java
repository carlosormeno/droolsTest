package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.entity.DTO.*;
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
public class TipoProcesoService {

    private final TipoProcesoRepository tipoProcesoRepository;

    /**
     * Crear nuevo tipo de proceso
     */
    public TipoProcesoSeleccion crearTipoProceso(TipoProcesoSeleccion tipoProceso, String usuario) {
        log.info("Creando tipo de proceso: {} para año {}", tipoProceso.getCodigo(), tipoProceso.getAnioVigencia());

        // Validar duplicado código-año
        if (tipoProcesoRepository.existsByCodigoAndAnioVigenciaAndEstadoRegistroTrueAndIdNot(
                tipoProceso.getCodigo(), tipoProceso.getAnioVigencia(), null)) {
            throw new IllegalArgumentException("Ya existe un tipo de proceso con código " +
                    tipoProceso.getCodigo() + " para el año " + tipoProceso.getAnioVigencia());
        }

        tipoProceso.setCreatedBy(usuario);
        tipoProceso.setUpdatedBy(usuario);
        tipoProceso.setEstadoRegistro(true);

        TipoProcesoSeleccion guardado = tipoProcesoRepository.save(tipoProceso);
        log.info("Tipo de proceso creado con ID: {}", guardado.getId());

        return guardado;
    }

    /**
     * Actualizar tipo de proceso
     */
    public TipoProcesoSeleccion actualizarTipoProceso(Long id, TipoProcesoSeleccion tipoProcesoActualizado, String usuario) {
        TipoProcesoSeleccion existente = obtenerPorId(id);

        // Validar duplicado excluyendo el actual
        if (tipoProcesoRepository.existsByCodigoAndAnioVigenciaAndEstadoRegistroTrueAndIdNot(
                tipoProcesoActualizado.getCodigo(), tipoProcesoActualizado.getAnioVigencia(), id)) {
            throw new IllegalArgumentException("Ya existe otro tipo de proceso con el mismo código y año");
        }

        // Actualizar campos
        existente.setCodigo(tipoProcesoActualizado.getCodigo());
        existente.setNombre(tipoProcesoActualizado.getNombre());
        existente.setDescripcion(tipoProcesoActualizado.getDescripcion());
        existente.setAnioVigencia(tipoProcesoActualizado.getAnioVigencia());
        existente.setEstado(tipoProcesoActualizado.getEstado());
        existente.setObservaciones(tipoProcesoActualizado.getObservaciones());
        existente.setUpdatedBy(usuario);

        return tipoProcesoRepository.save(existente);
    }

    @Transactional(readOnly = true)
    public TipoProcesoSeleccion obtenerPorId(Long id) {
        return tipoProcesoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de proceso no encontrado con ID: " + id));
    }
    /**
     * Obtener tipos de proceso activos por año
     */
    @Transactional(readOnly = true)
    public List<TipoProcesoSeleccion> obtenerTiposProcesoActivos(Integer anio) {
        return tipoProcesoRepository.findByAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByNombre(anio, "ACTIVO");
    }

    @Transactional(readOnly = true)
    public List<TipoProcesoSeleccion> obtenerTodosTiposProcesoActivos() {
        return tipoProcesoRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO");
    }

    /**
     * Obtener todos los años disponibles
     */
    @Transactional(readOnly = true)
    public List<Integer> obtenerAniosDisponibles() {
        return tipoProcesoRepository.findDistinctAnioVigenciaByEstadoRegistroTrueOrderByAnioVigenciaDesc();
    }

    /**
     * Obtener tipos de proceso con topes vigentes
     */
    @Transactional(readOnly = true)
    public List<TipoProcesoSeleccion> obtenerTiposProcesoConTopes(Integer anio) {
        return tipoProcesoRepository.findTiposProcesoConTopesVigentes(anio);
    }

    /**
     * Eliminar tipo de proceso (borrado lógico)
     */
    public void eliminarTipoProceso(Long id, String usuario) {
        log.info("Eliminando lógicamente tipo de proceso con ID: {}", id);

        TipoProcesoSeleccion tipoProceso = tipoProcesoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de proceso no encontrado con ID: " + id));

        // Realizar borrado lógico
        tipoProceso.setEstadoRegistro(false);
        tipoProceso.setUpdatedBy(usuario);
        tipoProceso.setUpdatedAt(LocalDateTime.now());

        tipoProcesoRepository.save(tipoProceso);
        log.info("Tipo de proceso eliminado lógicamente con ID: {}", id);
    }

}
