package com.example.droolsTest.repository;

import com.example.droolsTest.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TipoProcesoRepository extends JpaRepository<TipoProcesoSeleccion, Long> {

    List<TipoProcesoSeleccion> findByAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByNombre(
            Integer anio, String estado);

    Optional<TipoProcesoSeleccion> findByCodigoAndAnioVigenciaAndEstadoRegistroTrue(
            String codigo, Integer anio);

    List<Integer> findDistinctAnioVigenciaByEstadoRegistroTrueOrderByAnioVigenciaDesc();

    boolean existsByCodigoAndAnioVigenciaAndEstadoRegistroTrueAndIdNot(
            String codigo, Integer anio, Long id);

    @Query("SELECT DISTINCT tp FROM TipoProcesoSeleccion tp JOIN tp.topes t WHERE t.anioVigencia = :anio AND tp.estado = 'ACTIVO' AND tp.estadoRegistro = true AND t.estado = 'ACTIVO' AND t.estadoRegistro = true")
    List<TipoProcesoSeleccion> findTiposProcesoConTopesVigentes(@Param("anio") Integer anio);

    List<TipoProcesoSeleccion> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado);

}
