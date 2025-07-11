package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.exception.OperationNotAllowedException;
import com.example.droolsTest.exception.ResourceNotFoundException;
import com.example.droolsTest.repository.ObjetoContratacionRepository;
import com.example.droolsTest.repository.TopesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ObjetoContratacionService {

    private final ObjetoContratacionRepository objetoRepository;
    private final TopesRepository topesRepository;

    public ObjetoContratacion crearObjeto(ObjetoContratacion objeto, String usuario) {
        log.info("Creando objeto de contratación: {}", objeto.getCodigo());

        if (objetoRepository.existsByCodigoAndEstadoRegistroTrue(objeto.getCodigo())) {
            throw new OperationNotAllowedException("Ya existe un objeto con código: " + objeto.getCodigo());
        }

        objeto.setCreatedBy(usuario);
        objeto.setUpdatedBy(usuario);
        objeto.setEstadoRegistro(true);

        return objetoRepository.save(objeto);
    }

    public ObjetoContratacion actualizarObjeto(Long id, ObjetoContratacion objetoActualizado, String usuario) {
        ObjetoContratacion existente = obtenerPorId(id);

        if (objetoRepository.existsByCodigoAndEstadoRegistroTrueAndIdNot(objetoActualizado.getCodigo(), id)) {
            throw new OperationNotAllowedException("Ya existe otro objeto con el código: " + objetoActualizado.getCodigo());
        }

        existente.setCodigo(objetoActualizado.getCodigo());
        existente.setNombre(objetoActualizado.getNombre());
        existente.setDescripcion(objetoActualizado.getDescripcion());
        existente.setPermiteSubDescripcion(objetoActualizado.getPermiteSubDescripcion());
        existente.setEstado(objetoActualizado.getEstado());
        existente.setSubDescripcionContratacion(objetoActualizado.getSubDescripcionContratacion());
        existente.setUpdatedBy(usuario);

        return objetoRepository.save(existente);
    }

    @Transactional(readOnly = true)
    public ObjetoContratacion obtenerPorId(Long id) {
        return objetoRepository.findByIdAndEstadoRegistroTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Objeto no encontrado con ID: " + id));
    }

    @Transactional(readOnly = true)
    public Page<ObjetoContratacion> listarObjetos(Pageable pageable) {
        return objetoRepository.findByEstadoRegistroTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Page<ObjetoContratacion> obtenerObjetosActivos(Pageable pageable) {
        return objetoRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO", pageable);
    }

    @Transactional(readOnly = true)
    public List<ObjetoContratacion> obtenerObjetosActivos() {
        return objetoRepository.findByEstadoAndEstadoRegistroTrueOrderByNombre("ACTIVO");
    }

    @Transactional(readOnly = true)
    public Page<ObjetoContratacion> obtenerObjetosConSubDescripcion(Pageable pageable) {
        return objetoRepository.findByPermiteSubDescripcionTrueAndEstadoAndEstadoRegistroTrueOrderByNombre(
                "ACTIVO", pageable);
    }

    public void eliminarObjeto(Long id, String usuario) {
        ObjetoContratacion existente = obtenerPorId(id);

        if (topesRepository.existsByObjetoContratacionAndEstadoRegistroTrue(existente)) {
            throw new OperationNotAllowedException(
                    "No se puede eliminar el objeto porque tiene topes asociados");
        }

        existente.setEstadoRegistro(false);
        existente.setUpdatedBy(usuario);
        objetoRepository.save(existente);
    }
}
