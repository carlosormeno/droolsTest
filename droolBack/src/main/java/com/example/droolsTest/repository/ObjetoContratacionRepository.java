package com.example.droolsTest.repository;

import com.example.droolsTest.entity.ObjetoContratacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjetoContratacionRepository extends JpaRepository<ObjetoContratacion, Long> {

    Page<ObjetoContratacion> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado, Pageable pageable);

    Page<ObjetoContratacion> findByEstadoRegistroTrue(Pageable pageable);

    Page<ObjetoContratacion> findByPermiteSubDescripcionTrueAndEstadoAndEstadoRegistroTrueOrderByNombre(
            String estado, Pageable pageable);

    Optional<ObjetoContratacion> findByIdAndEstadoRegistroTrue(Long id);

    Optional<ObjetoContratacion> findByCodigoAndEstadoRegistroTrue(String codigo);

    boolean existsByCodigoAndEstadoRegistroTrue(String codigo);

    boolean existsByCodigoAndEstadoRegistroTrueAndIdNot(String codigo, Long id);

    List<ObjetoContratacion> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado);

    // Para verificar si tiene sub-descripción específica
    List<ObjetoContratacion> findBySubDescripcionContratacionIdAndEstadoRegistroTrue(Long subDescripcionId);

    // Para verificar si permite sub-descripciones
    List<ObjetoContratacion> findByPermiteSubDescripcionTrueAndEstadoRegistroTrue();
}
