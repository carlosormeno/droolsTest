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
public interface SubDescripcionRepository extends JpaRepository<SubDescripcionContratacion, Long> {

    // ✅ Métodos automáticos
    List<SubDescripcionContratacion> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado);
    List<SubDescripcionContratacion> findByIdObjetoContratacionAndEstadoAndEstadoRegistroTrueOrderByNombre(
            Long idObjeto, String estado);
    boolean existsByCodigoAndIdObjetoContratacionAndEstadoRegistroTrueAndIdNot(
            String codigo, Long idObjeto, Long id);

    @Query("SELECT sd FROM SubDescripcionContratacion sd JOIN FETCH sd.objetoContratacion WHERE sd.id = :id AND sd.estadoRegistro = true")
    Optional<SubDescripcionContratacion> findByIdWithObjeto(@Param("id") Long id);

    @Query("SELECT DISTINCT sd FROM SubDescripcionContratacion sd JOIN sd.topes t WHERE t.anioVigencia = :anio AND sd.estado = 'ACTIVO' AND sd.estadoRegistro = true AND t.estado = 'ACTIVO' AND t.estadoRegistro = true")
    List<SubDescripcionContratacion> findSubDescripcionesConTopesVigentes(@Param("anio") Integer anio);
}
