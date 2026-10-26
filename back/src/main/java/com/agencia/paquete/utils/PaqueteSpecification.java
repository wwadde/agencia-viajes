package com.agencia.utils;

import com.agencia.domain.Destino;
import com.agencia.domain.Paquete;
import com.agencia.domain.Salida;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PaqueteSpecification {

    private PaqueteSpecification() {
    }

    public static Specification<Paquete> buscar(
            String pais,
            String ciudad,
            Integer duracionMaxima,
            BigDecimal presupuestoMaximo,
            LocalDate fechaSalida,
            Integer personas
    ) {
        return Specification.where(disponible())
                .and(filtrarPorDestino(pais, ciudad))
                .and(filtrarPorSalida(presupuestoMaximo, fechaSalida, personas))
                .and(filtrarPorDuracion(duracionMaxima));
    }

    private static Specification<Paquete> disponible() {
        return (root, query, cb) -> cb.isTrue(root.get("disponible"));
    }

    private static Specification<Paquete> filtrarPorDuracion(Integer duracionMaxima) {
        return (root, query, cb) -> {
            if (duracionMaxima == null) return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("duracionDias"), duracionMaxima);
        };
    }

    private static Specification<Paquete> filtrarPorDestino(String pais, String ciudad) {
        return (root, query, cb) -> {
            // Si no hay filtros de destino, no hacemos nada
            if (StringUtils.isBlank(pais) && StringUtils.isBlank(ciudad)) {
                return cb.conjunction();
            }

            query.distinct(true);

            Join<Paquete, Destino> destinoJoin = root.join("destinos", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.isNotBlank(pais)) {
                predicates.add(cb.like(cb.lower(destinoJoin.get("pais")), "%" + pais.toLowerCase() + "%"));
            }
            if (StringUtils.isNotBlank(ciudad)) {
                predicates.add(cb.like(cb.lower(destinoJoin.get("ciudad")), "%" + ciudad.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Specification<Paquete> filtrarPorSalida(BigDecimal precioMax, LocalDate fecha, Integer personas) {
        return (root, query, cb) -> {
            if (precioMax == null && fecha == null && personas == null) {
                return cb.conjunction();
            }

            query.distinct(true);

            Join<Paquete, Salida> salidaJoin = root.join("salidas", JoinType.INNER);

            List<Predicate> predicates = new ArrayList<>();

            if (fecha != null) {
                predicates.add(cb.greaterThanOrEqualTo(salidaJoin.get("fechaSalida"), fecha));
            }
            if (precioMax != null) {
                predicates.add(cb.lessThanOrEqualTo(salidaJoin.get("precio"), precioMax));
            }
            if (personas != null) {
                predicates.add(cb.greaterThanOrEqualTo(salidaJoin.get("cuposDisponibles"), personas));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}