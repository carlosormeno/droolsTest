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
public interface TopesRepository extends JpaRepository<Topes, Long> {

    List<Topes> findByAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMontoDesc(Integer anio, String estado);
    List<Topes> findByIdTipoProcesoSeleccionAndAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMonto(
            Long tipoProcesoId, Integer anio, String estado);
    List<Topes> findByIdObjetoContratacionAndAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMonto(
            Long objetoId, Integer anio, String estado);
    List<Topes> findByMontoBetweenAndAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMonto(
            BigDecimal montoMin, BigDecimal montoMax, Integer anio, String estado);

    // Lógica temporal
    @Query("SELECT t FROM Topes t WHERE t.anioVigencia <= :anio AND t.estado = 'ACTIVO' AND t.estadoRegistro = true ORDER BY t.anioVigencia DESC")
    List<Topes> findTopesVigentesPorFecha(@Param("anio") Integer anio);

    // JOIN FETCH para performance
    @Query("SELECT t FROM Topes t " +
            "JOIN FETCH t.tipoProcesoSeleccion tps " +
            "JOIN FETCH t.objetoContratacion oc " +
            "LEFT JOIN FETCH t.subDescripcionContratacion sdc " +
            "JOIN FETCH t.operadorMonto om " +
            "WHERE t.anioVigencia = :anio AND t.estado = 'ACTIVO' AND t.estadoRegistro = true " +
            "ORDER BY tps.nombre, oc.nombre, sdc.nombre")
    List<Topes> findTopesConRelacionesPorAnio(@Param("anio") Integer anio);

    // Búsqueda específica con lógica compleja
    @Query("SELECT t FROM Topes t " +
            "WHERE t.idTipoProcesoSeleccion = :tipoProcesoId " +
            "AND t.idObjetoContratacion = :objetoId " +
            "AND (:subDescripcionId IS NULL AND t.idSubDescripcionContratacion IS NULL OR t.idSubDescripcionContratacion = :subDescripcionId) " +
            "AND t.idOperadorMonto = :operadorId " +
            "AND t.anioVigencia = :anio " +
            "AND t.estado = 'ACTIVO' AND t.estadoRegistro = true")
    Optional<Topes> findTopeEspecifico(
            @Param("tipoProcesoId") Long tipoProcesoId,
            @Param("objetoId") Long objetoId,
            @Param("subDescripcionId") Long subDescripcionId,
            @Param("operadorId") Long operadorId,
            @Param("anio") Integer anio
    );

    // Evaluación de reglas (lógica matemática compleja)
    @Query("SELECT t FROM Topes t " +
            "JOIN FETCH t.tipoProcesoSeleccion tps " +
            "JOIN FETCH t.objetoContratacion oc " +
            "LEFT JOIN FETCH t.subDescripcionContratacion sdc " +
            "JOIN FETCH t.operadorMonto om " +
            "WHERE t.anioVigencia = :anio AND t.estado = 'ACTIVO' AND t.estadoRegistro = true " +
            "AND tps.estado = 'ACTIVO' AND tps.estadoRegistro = true " +
            "AND (" +
            "  (om.codigo = 'MAYOR_IGUAL' AND :monto >= t.monto) OR " +
            "  (om.codigo = 'MENOR_IGUAL' AND :monto <= t.monto) OR " +
            "  (om.codigo = 'MAYOR' AND :monto > t.monto) OR " +
            "  (om.codigo = 'MENOR' AND :monto < t.monto)" +
            ") " +
            "ORDER BY tps.nombre, oc.nombre")
    List<Topes> findTopesAplicablesParaMonto(@Param("monto") BigDecimal monto, @Param("anio") Integer anio);

    // Validación de duplicados (lógica condicional compleja)
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Topes t " +
            "WHERE t.idTipoProcesoSeleccion = :tipoProcesoId " +
            "AND t.idObjetoContratacion = :objetoId " +
            "AND (:subDescripcionId IS NULL AND t.idSubDescripcionContratacion IS NULL OR t.idSubDescripcionContratacion = :subDescripcionId) " +
            "AND t.anioVigencia = :anio " +
            "AND t.estadoRegistro = true " +
            "AND (:id IS NULL OR t.id != :id)")
    boolean existsCombinacionExcludingId(
            @Param("tipoProcesoId") Long tipoProcesoId,
            @Param("objetoId") Long objetoId,
            @Param("subDescripcionId") Long subDescripcionId,
            @Param("anio") Integer anio,
            @Param("id") Long id
    );

    // Estadísticas (GROUP BY)
    @Query("SELECT t.anioVigencia, COUNT(t) FROM Topes t WHERE t.estadoRegistro = true GROUP BY t.anioVigencia ORDER BY t.anioVigencia")
    List<Object[]> countTopesPorAnio();

}
