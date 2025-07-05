package com.example.droolsTest.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParametricaEntity {
    String value() default "";
    String description() default "";
    boolean includeInCatalog() default true;
}
