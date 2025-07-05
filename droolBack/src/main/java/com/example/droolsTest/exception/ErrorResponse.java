package com.example.droolsTest.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String code;                    // Código de error (ej: "VALIDATION_ERROR")
    private String message;                 // Mensaje descriptivo
    private LocalDateTime timestamp;        // Fecha y hora del error
    private Integer status;                 // Código HTTP (400, 404, 500, etc.)
    private Map<String, String> fieldErrors; // Errores específicos por campo (opcional)
    private String path;                    // Path de la API que falló (opcional)

    /**
     * Constructor para errores simples
     */
    public ErrorResponse(String code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

}
