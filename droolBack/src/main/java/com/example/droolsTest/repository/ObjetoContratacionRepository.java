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
public interface ObjetoContratacionRepository extends JpaRepository<ObjetoContratacion, Long> {

    List<ObjetoContratacion> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado);
    Optional<ObjetoContratacion> findByCodigoAndEstadoRegistroTrue(String codigo);
    List<ObjetoContratacion> findByPermiteSubDescripcionTrueAndEstadoAndEstadoRegistroTrueOrderByNombre(String estado);
    boolean existsByCodigoAndEstadoRegistroTrueAndIdNot(String codigo, Long id);

    @Query("SELECT oc FROM ObjetoContratacion oc LEFT JOIN FETCH oc.subDescripciones sd WHERE oc.id = :id AND oc.estadoRegistro = true AND (sd.estadoRegistro = true OR sd.estadoRegistro IS NULL)")
    Optional<ObjetoContratacion> findByIdWithSubDescripciones(@Param("id") Long id);

    @Query("SELECT DISTINCT oc FROM ObjetoContratacion oc JOIN oc.topes t WHERE t.anioVigencia = :anio AND oc.estado = 'ACTIVO' AND oc.estadoRegistro = true AND t.estado = 'ACTIVO' AND t.estadoRegistro = true")
    List<ObjetoContratacion> findObjetosConTopesVigentes(@Param("anio") Integer anio);
}
