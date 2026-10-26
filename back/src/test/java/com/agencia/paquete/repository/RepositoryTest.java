package com.agencia.paquete.repository;

import com.agencia.destino.entity.Destino;
import com.agencia.destino.repository.DestinoRepository;
import com.agencia.paquete.entity.Paquete;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static tools.jackson.databind.node.JsonNodeFactory.instance;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("pgvector/pgvector:pg16");

    @Autowired
    PaqueteRepository paqueteRepository;
    @Autowired
    DestinoRepository destinoRepository;

    @BeforeEach
    void setUp() {

        Destino destino = Destino.builder()
                .nombre("Destino de prueba")
                .ciudad("Ciudad de prueba")
                .pais("País de prueba")
                .latitud(10.0)
                .longitud(-75.0)
                .build();

        Paquete paquete = Paquete.builder()
                .descripcion("Paquete de prueba")
                .detalle(instance.textNode("Detalle del paquete de prueba"))
                .nombre("Paquete de prueba")
                .slug("paquete-de-prueba-" + System.nanoTime())
                .duracionDias(8)
                .disponible(true)
                .ultimaModificacion(LocalDateTime.now())
                .destinos(new java.util.ArrayList<>(List.of(destinoRepository.save(destino))))
                .build();
        paqueteRepository.save(paquete);

    }

    @Test
    void connectionEstablished(){
        assertThat(postgres.isCreated()).isTrue();
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void retornaPaquete(){
        Paquete paquete = paqueteRepository.findByNombre("Paquete de prueba").orElse(null);
        assertThat(paquete).isNotNull();
        assertThat(paquete.getNombre()).isEqualTo("Paquete de prueba");
    }

}
