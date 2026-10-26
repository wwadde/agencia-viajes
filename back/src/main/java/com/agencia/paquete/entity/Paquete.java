package com.agencia.paquete.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.agencia.destino.entity.Destino;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode detalle;

    @Column(name = "detalle_texto_plano", columnDefinition = "text")
    private String detalleTextoPlano;

    @Column(name = "duracion_dias")
    private Integer duracionDias;

    private boolean disponible;

    private boolean destacado;

    @Column(name = "ultima_modificacion", nullable = false)
    private LocalDateTime ultimaModificacion;

    @Builder.Default
    @OneToMany(mappedBy = "paquete", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImagenPaquete> imagenes = new ArrayList<>();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "paquete_destinos",
        joinColumns = @JoinColumn(name = "paquete_id"),
        inverseJoinColumns = @JoinColumn(name = "destino_id")
    )
    @OrderColumn(name = "orden")
    private List<Destino> destinos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "paquete")
    private List<Salida> salidas = new ArrayList<>();
}
