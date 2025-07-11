package com.example.droolsTest.repository;

import com.example.droolsTest.entity.ObjetoContratacion;
import com.example.droolsTest.entity.Topes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface TopesRepository extends JpaRepository<Topes, Long> {

    Page<Topes> findByAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMontoDesc(
            Integer anio, String estado, Pageable pageable);

    Page<Topes> findByTipoProcesoSeleccionIdAndAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMonto(
            Long tipoProcesoId, Integer anio, String estado, Pageable pageable);

    Page<Topes> findByObjetoContratacionIdAndAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMonto(
            Long objetoId, Integer anio, String estado, Pageable pageable);

    Page<Topes> findByMontoBetweenAndAnioVigenciaAndEstadoAndEstadoRegistroTrueOrderByMonto(
            BigDecimal montoMin, BigDecimal montoMax, Integer anio, String estado, Pageable pageable);

    Optional<Topes> findByIdAndEstadoRegistroTrue(Long id);

    List<Topes> findByAnioVigenciaAndEstadoAndEstadoRegistroTrue(Integer anio, String estado);

    boolean existsByObjetoContratacionAndEstadoRegistroTrue(ObjetoContratacion objetoContratacion);

    // Solo @Query para lógicas complejas que no se pueden expresar con métodos derivados

    // Validar combinación única - necesita lógica compleja
    @Query("SELECT COUNT(t) > 0 FROM Topes t " +
            "WHERE t.tipoProcesoSeleccion.id = :tipoProcesoId " +
            "AND t.objetoContratacion.id = :objetoId " +
            "AND t.operadorMonto.id = :operadorId " +
            "AND t.anioVigencia = :anio " +
            "AND t.estadoRegistro = true " +
            "AND (:id IS NULL OR t.id != :id)")
    boolean existeCombinacionUnica(
            @Param("tipoProcesoId") Long tipoProcesoId,
            @Param("objetoId") Long objetoId,
            @Param("operadorId") Long operadorId,
            @Param("anio") Integer anio,
            @Param("id") Long id);

    // Evaluación de montos - lógica compleja con operadores
    @Query("SELECT t FROM Topes t " +
            "JOIN FETCH t.tipoProcesoSeleccion tps " +
            "JOIN FETCH t.objetoContratacion oc " +
            "JOIN FETCH t.operadorMonto om " +
            "WHERE t.anioVigencia = :anio " +
            "AND t.estado = 'ACTIVO' " +
            "AND t.estadoRegistro = true " +
            "AND tps.estado = 'ACTIVO' " +
            "AND tps.estadoRegistro = true " +
            "AND (" +
            "  (om.codigo = 'MAYOR_IGUAL' AND :monto >= t.monto) OR " +
            "  (om.codigo = 'MENOR_IGUAL' AND :monto <= t.monto) OR " +
            "  (om.codigo = 'MAYOR' AND :monto > t.monto) OR " +
            "  (om.codigo = 'MENOR' AND :monto < t.monto) OR " +
            "  (om.codigo = 'IGUAL' AND t.monto = :monto) " +
            ") " +
            "ORDER BY tps.nombre, oc.nombre, t.monto")
    List<Topes> findTopesAplicablesParaMonto(@Param("monto") BigDecimal monto, @Param("anio") Integer anio);

    // Estadísticas - agregaciones que requieren @Query
    @Query("SELECT t.anioVigencia, COUNT(t), MIN(t.monto), MAX(t.monto), AVG(t.monto) " +
            "FROM Topes t " +
            "WHERE t.estadoRegistro = true " +
            "GROUP BY t.anioVigencia " +
            "ORDER BY t.anioVigencia")
    List<Object[]> obtenerEstadisticasPorAnio();

    List<Topes> findByEstadoAndEstadoRegistroTrue(String estado);
}
