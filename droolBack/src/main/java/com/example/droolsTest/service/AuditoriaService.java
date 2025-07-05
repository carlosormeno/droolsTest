package com.example.droolsTest.service;

import com.example.droolsTest.entity.*;
import com.example.droolsTest.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    /**
     * Establecer usuario actual para auditoría automática
     */
    public void establecerUsuarioActual(String username) {
        // Esto se puede usar con ThreadLocal o Spring Security Context
        log.debug("Usuario establecido para auditoría: {}", username);
        // Implementar según tu estrategia de manejo de usuarios
    }

    /**
     * Obtener usuario actual
     */
    public String obtenerUsuarioActual() {
        // Implementar según tu estrategia (SecurityContext, ThreadLocal, etc.)
        return "SISTEMA"; // Default
    }

}
