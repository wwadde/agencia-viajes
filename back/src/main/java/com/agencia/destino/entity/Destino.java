package com.agencia.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "destinos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Destino {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;

    private String pais;

    private String ciudad;

    private Double latitud;

    private Double longitud;

    @Builder.Default
    @ManyToMany(mappedBy = "destinos")
    private List<Paquete> paquetes = new ArrayList<>();

}
