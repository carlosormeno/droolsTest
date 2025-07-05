package com.example.droolsTest.exception;

public class DuplicateEntityException extends RuntimeException{

    public DuplicateEntityException(String entityName, String field, Object value) {
        super(String.format("Ya existe %s con %s: %s", entityName, field, value));
    }

    public DuplicateEntityException(String message) {
        super(message);
    }

}
