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
public interface OperadoresMontoRepository extends JpaRepository<OperadoresMonto, Long> {

    List<OperadoresMonto> findByEstadoAndEstadoRegistroTrueOrderByNombre(String estado);

    Optional<OperadoresMonto> findByCodigoAndEstadoRegistroTrue(String codigo);

    Optional<OperadoresMonto> findBySimboloAndEstadoRegistroTrue(String simbolo);

    boolean existsByCodigoAndEstadoRegistroTrueAndIdNot(String codigo, Long id);

    boolean existsBySimboloAndEstadoRegistroTrueAndIdNot(String simbolo, Long id);

    List<OperadoresMonto> findByEstadoRegistroTrueOrderByNombre();
}
