package com.example.droolsTest.exception;

public class EntityNotFoundException extends RuntimeException{

    public EntityNotFoundException(String entityName, Long id) {
        super(String.format("%s no encontrado con ID: %d", entityName, id));
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

}
