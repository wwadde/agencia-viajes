package com.agencia.contenido.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "contenidos")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Contenido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clave;

    @Column(length = 5000)
    private String valor;
}
