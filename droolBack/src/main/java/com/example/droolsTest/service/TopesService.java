package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.OperationNotAllowedException;
import com.example.droolsTest.exception.ResourceNotFoundException;
import com.example.droolsTest.repository.TopesRepository;
import com.example.droolsTest.repository.UitRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TopesService {

    private final TopesRepository topesRepository;
    private final UitRepository uitRepository;
    private final ObjetoContratacionService objetoService;
    private final TipoProcesoService tipoProcesoService;
    private final OperadoresMontoService operadorService;

    public Topes crearTope(Topes tope, String usuario) {
        log.info("Creando tope para año {} con monto {}", tope.getAnioVigencia(), tope.getMonto());
        log.info("tope: {}", tope);

        poblarEntidadesDesdeIds(tope);

        validarRelaciones(tope);

        if (existeCombinacionUnica(tope, null)) {
            throw new OperationNotAllowedException("Ya existe un tope para esta combinación de parámetros");
        }

        if (tope.getReferenciaUit() == null) {
            calcularReferenciaUit(tope);
        }

        tope.setCreatedBy(usuario);
        tope.setUpdatedBy(usuario);
        tope.setEstadoRegistro(true);

        return topesRepository.save(tope);
    }

    private void poblarEntidadesDesdeIds(Topes tope) {
        // Cargar TipoProcesoSeleccion
        if (tope.getIdTipoProcesoSeleccion() != null) {
            TipoProcesoSeleccion tipoProceso = tipoProcesoService.obtenerPorId(tope.getIdTipoProcesoSeleccion());
            tope.setTipoProcesoSeleccion(tipoProceso);
        }

        // Cargar ObjetoContratacion
        if (tope.getIdObjetoContratacion() != null) {
            ObjetoContratacion objeto = objetoService.obtenerPorId(tope.getIdObjetoContratacion());
            tope.setObjetoContratacion(objeto);
        }

        // Cargar OperadoresMonto
        if (tope.getIdOperadorMonto() != null) {
            OperadoresMonto operador = operadorService.obtenerPorId(tope.getIdOperadorMonto());
            tope.setOperadorMonto(operador);
        }
    }

    public Topes actualizarTope(Long id, Topes topeActualizado, String usuario) {
        //log.info("Creando tope para año {} con monto {}", topeActualizado.getAnioVigencia(), tope.getMonto());
        log.info("tope: {}", topeActualizado);
        Topes existente = obtenerPorId(id);

        poblarEntidadesDesdeIds(topeActualizado);
        validarRelaciones(topeActualizado);

        if (existeCombinacionUnica(topeActualizado, id)) {
            throw new OperationNotAllowedException("Ya existe otro tope para esta combinación");
        }

        existente.setTipoProcesoSeleccion(topeActualizado.getTipoProcesoSeleccion());
        existente.setObjetoContratacion(topeActualizado.getObjetoContratacion());
        existente.setOperadorMonto(topeActualizado.getOperadorMonto());
        existente.setMonto(topeActualizado.getMonto());
        existente.setReferenciaUit(topeActualizado.getReferenciaUit());
        existente.setObservaciones(topeActualizado.getObservaciones());
        existente.setAnioVigencia(topeActualizado.getAnioVigencia());
        existente.setEstado(topeActualizado.getEstado());
        existente.setUpdatedBy(usuario);

        if (existente.getReferenciaUit() == null) {
            calcularReferenciaUit(existente);
        }

        return topesRepository.save(existente);
    }

    @Transactional(readOnly = true)
    public Topes obtenerPorId(Long id) {
        return topesRepository.findByIdAndEstadoRegistroTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tope no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Topes> obtenerTopesVigentes(Integer anio) {
        return topesRepository.findByAnioVigenciaAndEstadoAndEstadoRegistroTrue(anio, "ACTIVO");
    }

    @Transactional(readOnly = true)
    public Page<Topes> obtenerTopesVigentes(Integer anio, Pageable pageable) {
        return topesRepository.findByAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMontoDesc(anio, "ACTIVO", pageable);
    }

    @Transactional(readOnly = true)
    public List<Topes> evaluarMontoContraTopes(BigDecimal monto, Integer anio) {
        return topesRepository.findTopesAplicablesParaMonto(monto, anio);
    }

    public void eliminarTope(Long id, String usuario) {
        Topes existente = obtenerPorId(id);
        existente.setEstadoRegistro(false);
        existente.setUpdatedBy(usuario);
        topesRepository.save(existente);
    }

    @Transactional(readOnly = true)
    public List<Object[]> obtenerEstadisticasPorAnio() {
        return topesRepository.obtenerEstadisticasPorAnio();
    }

    private void validarRelaciones(Topes tope) {
        objetoService.obtenerPorId(tope.getObjetoContratacion().getId());
        tipoProcesoService.obtenerPorId(tope.getTipoProcesoSeleccion().getId());
        operadorService.obtenerPorId(tope.getOperadorMonto().getId());
    }

    private boolean existeCombinacionUnica(Topes tope, Long excludeId) {
        return topesRepository.existeCombinacionUnica(
                tope.getTipoProcesoSeleccion().getId(),
                tope.getObjetoContratacion().getId(),
                tope.getOperadorMonto().getId(),
                tope.getAnioVigencia(),
                excludeId
        );
    }

    private void calcularReferenciaUit(Topes tope) {
        Optional<Uit> uitVigente = uitRepository.findByAnioVigenciaAndEstadoAndEstadoRegistroTrue(
                tope.getAnioVigencia(), "ACTIVO");

        if (uitVigente.isPresent()) {
            BigDecimal montoUit = uitVigente.get().getMonto();
            if (montoUit.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal referenciaUit = tope.getMonto().divide(montoUit, 4, RoundingMode.HALF_UP);
                tope.setReferenciaUit(referenciaUit);
            }
        }
    }

    // AGREGAR en TopesService.java:
    public List<Topes> obtenerTodos() {
        return topesRepository.findAll();
    }

    public List<Topes> obtenerTodosTopesActivos() {
        return topesRepository.findByEstadoAndEstadoRegistroTrue("ACTIVO");
    }
}
