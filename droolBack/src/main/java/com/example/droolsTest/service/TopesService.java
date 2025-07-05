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
public class TopesService {

    private final TopesRepository topesRepository;
    private final UitService uitService;

    /**
     * Crear nuevo tope
     */
    public Topes crearTope(Topes tope, String usuario) {
        log.info("Creando tope para año {} con monto {}", tope.getAnioVigencia(), tope.getMonto());

        // Validar que no exista la misma combinación
        if (topesRepository.existsCombinacionExcludingId(
                tope.getIdTipoProcesoSeleccion(),
                tope.getIdObjetoContratacion(),
                tope.getIdSubDescripcionContratacion(),
                tope.getAnioVigencia(),
                null)) {
            throw new IllegalArgumentException("Ya existe un tope para esta combinación de parámetros");
        }

        // Calcular referencia UIT si no se proporciona
        if (tope.getReferenciaUit() == null) {
            calcularReferenciaUit(tope);
        }

        tope.setCreatedBy(usuario);
        tope.setUpdatedBy(usuario);

        Topes guardado = topesRepository.save(tope);
        log.info("Tope creado con ID: {}", guardado.getId());

        return guardado;
    }

    /**
     * Actualizar tope existente
     */
    public Topes actualizarTope(Long id, Topes topeActualizado, String usuario) {
        Topes existente = topesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tope no encontrado con ID: " + id));

        // Validar duplicado excluyendo el actual
        if (topesRepository.existsCombinacionExcludingId(
                topeActualizado.getIdTipoProcesoSeleccion(),
                topeActualizado.getIdObjetoContratacion(),
                topeActualizado.getIdSubDescripcionContratacion(),
                topeActualizado.getAnioVigencia(),
                id)) {
            throw new IllegalArgumentException("Ya existe otro tope para esta combinación");
        }

        // Actualizar campos
        existente.setIdTipoProcesoSeleccion(topeActualizado.getIdTipoProcesoSeleccion());
        existente.setIdObjetoContratacion(topeActualizado.getIdObjetoContratacion());
        existente.setIdSubDescripcionContratacion(topeActualizado.getIdSubDescripcionContratacion());
        existente.setIdOperadorMonto(topeActualizado.getIdOperadorMonto());
        existente.setMonto(topeActualizado.getMonto());
        existente.setReferenciaUit(topeActualizado.getReferenciaUit());
        existente.setObservaciones(topeActualizado.getObservaciones());
        existente.setAnioVigencia(topeActualizado.getAnioVigencia());
        existente.setEstado(topeActualizado.getEstado());
        existente.setUpdatedBy(usuario);

        // Recalcular referencia UIT si cambió el monto
        if (existente.getReferenciaUit() == null) {
            calcularReferenciaUit(existente);
        }

        return topesRepository.save(existente);
    }

    /**
     * Obtener topes vigentes para un año con todas las relaciones
     */
    @Transactional(readOnly = true)
    public List<Topes> obtenerTopesVigentes(Integer anio) {
        return topesRepository.findTopesConRelacionesPorAnio(anio);
    }

    /**
     * Evaluar qué topes aplican para un monto específico
     */
    @Transactional(readOnly = true)
    public List<Topes> evaluarMontoContraTopes(BigDecimal monto, Integer anio) {
        log.debug("Evaluando monto {} contra topes del año {}", monto, anio);
        return topesRepository.findTopesAplicablesParaMonto(monto, anio);
    }

    /**
     * Buscar tope específico por parámetros
     */
    @Transactional(readOnly = true)
    public Optional<Topes> buscarTopeEspecifico(Long tipoProcesoId, Long objetoId,
                                                Long subDescripcionId, Long operadorId, Integer anio) {
        return topesRepository.findTopeEspecifico(tipoProcesoId, objetoId, subDescripcionId, operadorId, anio);
    }

    /**
     * Obtener estadísticas de topes por año
     */
    @Transactional(readOnly = true)
    public List<Object[]> obtenerEstadisticasPorAnio() {
        return topesRepository.countTopesPorAnio();
    }

    /**
     * Calcular referencia UIT basada en el monto y año
     */
    private void calcularReferenciaUit(Topes tope) {
        Optional<Uit> uitVigente = uitService.obtenerUitVigente(LocalDate.of(tope.getAnioVigencia(), 1, 1));

        if (uitVigente.isPresent()) {
            BigDecimal montoUit = uitVigente.get().getMonto();
            BigDecimal referenciaUit = tope.getMonto().divide(montoUit, 4, BigDecimal.ROUND_HALF_UP);
            tope.setReferenciaUit(referenciaUit);
            log.debug("Calculada referencia UIT: {} (monto: {} / UIT: {})", referenciaUit, tope.getMonto(), montoUit);
        }
    }

}
