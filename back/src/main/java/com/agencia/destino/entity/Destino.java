package com.agencia.destino.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import com.agencia.paquete.entity.Paquete;

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
