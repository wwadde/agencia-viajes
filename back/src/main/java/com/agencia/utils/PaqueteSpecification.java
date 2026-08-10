package com.agencia.utils;

import com.agencia.domain.Paquete;
import com.agencia.domain.Salida;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class PaqueteSpecification {

    private PaqueteSpecification() {
    }

    public static Specification<Paquete> buscar(
            String destino,
            String pais,
            Integer duracionMaxima,
            BigDecimal presupuestoMaximo,
            LocalDate fechaSalida,
            Integer personas
    ) {
        Specification<Paquete> specification = disponible();

        if (destino != null && !destino.isBlank()) {
            specification = specification.and(destinoContiene(destino));
        }

        if (pais != null && !pais.isBlank()) {
            specification = specification.and(paisContiene(pais));
        }

        if (duracionMaxima != null) {
            specification = specification.and(duracionMaxima(duracionMaxima));
        }

        if (presupuestoMaximo != null) {
            specification = specification.and(precioMaximo(presupuestoMaximo));
        }

        if (fechaSalida != null) {
            specification = specification.and(salidaDesde(fechaSalida));
        }

        if (personas != null) {
            specification = specification.and(cuposMinimos(personas));
        }

        return specification;
    }

    private static Specification<Paquete> disponible() {
        return (root, query, cb) ->
                cb.isTrue(root.get("disponible"));
    }

    private static Specification<Paquete> destinoContiene(String destino) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("destino").get("nombre")),
                        "%" + destino.toLowerCase() + "%"
                );
    }

    private static Specification<Paquete> paisContiene(String pais) {
        return (root, query, cb) ->
                cb.like(
                        cb.lower(root.get("destino").get("pais")),
                        "%" + pais.toLowerCase() + "%"
                );
    }

    private static Specification<Paquete> duracionMaxima(Integer dias) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(
                        root.get("duracionDias"),
                        dias
                );
    }

    private static Specification<Paquete> salidaDesde(LocalDate fecha) {
        return (root, query, cb) -> {
            Join<Paquete, Salida> salida = root.join("salidas");

            return cb.greaterThanOrEqualTo(
                    salida.get("fechaSalida"),
                    fecha
            );
        };
    }

    private static Specification<Paquete> precioMaximo(BigDecimal precio) {
        return (root, query, cb) -> {
            Join<Paquete, Salida> salida = root.join("salidas");

            return cb.lessThanOrEqualTo(
                    salida.get("precio"),
                    precio
            );
        };
    }

    private static Specification<Paquete> cuposMinimos(Integer personas) {
        return (root, query, cb) -> {
            Join<Paquete, Salida> salida = root.join("salidas");

            return cb.greaterThanOrEqualTo(
                    salida.get("cuposDisponibles"),
                    personas
            );
        };
    }
}