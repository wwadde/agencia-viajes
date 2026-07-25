package com.agencia.chatbot;

import com.agencia.domain.Usuario;
import jakarta.persistence.*;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "conversaciones")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Conversacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    private String titulo;

    @OneToMany(mappedBy = "conversacion")
    private List<MensajeChat> mensajes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;


}
