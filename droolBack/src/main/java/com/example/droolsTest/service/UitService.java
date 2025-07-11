package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.BusinessValidationException;
import com.example.droolsTest.exception.DuplicateEntityException;
import com.example.droolsTest.exception.EntityNotFoundException;
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
public class UitService {

    private final UitRepository uitRepository;

    /**
     * Crear nueva UIT
     */
    public Uit crearUit(Uit uit, String usuario) {
        log.info("Creando UIT para año {} con monto {}", uit.getAnioVigencia(), uit.getMonto());

        // Validar que no exista UIT para el mismo año
        if (uitRepository.existsByAnioVigenciaAndEstadoRegistroTrue(uit.getAnioVigencia())) {
            throw new DuplicateEntityException("UIT", "año de vigencia", uit.getAnioVigencia());
        }

        // Validación de negocio
        if (uit.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessValidationException("El monto de la UIT debe ser mayor a cero");
        }

        // Establecer datos de auditoría
        uit.setCreatedBy(usuario);
        uit.setUpdatedBy(usuario);
        uit.setEstadoRegistro(true);

        Uit uitGuardada = uitRepository.save(uit);
        log.info("UIT creada con ID: {}", uitGuardada.getId());

        return uitGuardada;
    }

    /**
     * Actualizar UIT existente
     */
    public Uit actualizarUit(Long id, Uit uitActualizada, String usuario) {
        log.info("Actualizando UIT con ID: {}", id);

        Uit uitExistente = obtenerPorId(id);

        // Validar que no exista otra UIT para el mismo año (excluyendo la actual)
        Optional<Uit> uitDuplicada = uitRepository.findByAnioVigenciaAndEstadoAndEstadoRegistroTrue(
                uitActualizada.getAnioVigencia(), "ACTIVO");

        if (uitDuplicada.isPresent() && !uitDuplicada.get().getId().equals(id)) {
            throw new IllegalArgumentException("Ya existe otra UIT activa para el año " + uitActualizada.getAnioVigencia());
        }

        // Actualizar campos
        uitExistente.setMonto(uitActualizada.getMonto());
        uitExistente.setAnioVigencia(uitActualizada.getAnioVigencia());
        uitExistente.setEstado(uitActualizada.getEstado());
        uitExistente.setObservaciones(uitActualizada.getObservaciones());
        uitExistente.setUpdatedBy(usuario);

        Uit uitGuardada = uitRepository.save(uitExistente);
        log.info("UIT actualizada exitosamente");

        return uitGuardada;
    }

    /**
     * Obtener UIT vigente para una fecha específica (lógica temporal)
     */
    @Transactional(readOnly = true)
    public Optional<Uit> obtenerUitVigente(LocalDate fecha) {
        Integer anio = fecha.getYear();
        log.debug("Buscando UIT vigente para fecha: {} (año: {})", fecha, anio);

        return uitRepository.findUitVigentePorFecha(anio);
    }

    /**
     * Listar todas las UITs activas
     */
    @Transactional(readOnly = true)
    public List<Uit> listarUitsActivas() {
        return uitRepository.findByEstadoAndEstadoRegistroTrueOrderByAnioVigenciaDesc("ACTIVO");
    }

    /**
     * Obtener UIT por ID
     */
    @Transactional(readOnly = true)
    public Uit obtenerPorId(Long id) {
        return uitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("UIT", id));
    }

    /**
     * Eliminar UIT (borrado lógico)
     */
    public void eliminarUit(Long id, String usuario) {
        log.info("Eliminando UIT con ID: {}", id);

        if (!uitRepository.existsById(id)) {
            throw new IllegalArgumentException("UIT no encontrada con ID: " + id);
        }

        uitRepository.softDelete(id, usuario);
        log.info("UIT eliminada exitosamente");
    }

    /**
     * Obtener UITs por rango de años
     */
    @Transactional(readOnly = true)
    public List<Uit> obtenerUitsPorRango(Integer anioInicio, Integer anioFin) {
        return uitRepository.findByAnioVigenciaBetweenAndEstadoAndEstadoRegistroTrueOrderByAnioVigencia(
                anioInicio, anioFin, "ACTIVO");
    }

}
