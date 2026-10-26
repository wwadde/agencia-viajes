package com.agencia.auth.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordConstraint {
    String message() default "Contraseña inválida. Debe contener al menos una letra mayúscula, una letra minúscula, un dígito y un caracter especial.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}