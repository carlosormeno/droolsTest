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
public interface UitRepository extends JpaRepository<Uit, Long> {

    // Métodos derivados - más legibles y mantenibles
    Optional<Uit> findByAnioVigenciaAndEstadoAndEstadoRegistroTrue(Integer anio, String estado);

    List<Uit> findByEstadoAndEstadoRegistroTrueOrderByAnioVigenciaDesc(String estado);

    boolean existsByAnioVigenciaAndEstadoRegistroTrue(Integer anio);

    List<Uit> findByAnioVigenciaBetweenAndEstadoAndEstadoRegistroTrueOrderByAnioVigencia(
            Integer anioInicio, Integer anioFin, String estado);

    // Solo @Query cuando sea realmente necesario - para lógica compleja
    @Query("SELECT u FROM Uit u WHERE u.anioVigencia <= :anio AND u.estado = 'ACTIVO' AND u.estadoRegistro = true ORDER BY u.anioVigencia DESC LIMIT 1")
    Optional<Uit> findUitVigentePorFecha(@Param("anio") Integer anio);

    // Borrado lógico - necesita @Query porque es UPDATE
    @Modifying
    @Transactional
    @Query("UPDATE Uit u SET u.estadoRegistro = false, u.updatedAt = CURRENT_TIMESTAMP, u.updatedBy = :usuario WHERE u.id = :id")
    int softDelete(@Param("id") Long id, @Param("usuario") String usuario);
}
