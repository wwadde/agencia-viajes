package com.agencia.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "paquetes")
public class Paquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    @Column(nullable = false, unique = true, length = 240)
    private String slug;

    @Column(length = 2000)
    private String descripcion;

    @Column(length = 5000)
    private String detalle;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    private boolean disponible;

    private boolean destacado;

    @Column(name = "ultima_modificacion", nullable = false)
    private LocalDateTime ultimaModificacion;

    @Builder.Default
    @OneToMany(mappedBy = "paquete")
    private List<ImagenPaquete> imagenes = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destino_id")
    private Destino destino;

    @Builder.Default
    @OneToMany(mappedBy = "paquete")
    private List<Salida> salidas = new ArrayList<>();
}
