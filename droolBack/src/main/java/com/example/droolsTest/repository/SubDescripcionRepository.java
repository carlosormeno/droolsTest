package com.example.droolsTest.repository;

import com.example.droolsTest.entity.SubDescripcionContratacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubDescripcionRepository extends JpaRepository<SubDescripcionContratacion, Long> {

    Page<SubDescripcionContratacion> findByEstadoAndEstadoRegistroTrueOrderByNombre(
            String estado, Pageable pageable);

    boolean existsByCodigoAndEstadoRegistroTrueAndIdNot(String codigo, Long id);

    Optional<SubDescripcionContratacion> findByIdAndEstadoRegistroTrue(Long id);

    List<SubDescripcionContratacion> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado);

    boolean existsByCodigoAndEstadoRegistroTrue(String codigo);

    Optional<SubDescripcionContratacion> findByCodigoAndEstadoRegistroTrue(String codigo);
}
