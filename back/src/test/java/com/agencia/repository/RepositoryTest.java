package com.agencia.repository;

import com.agencia.domain.Destino;
import com.agencia.domain.Paquete;
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

import static org.assertj.core.api.Assertions.assertThat;

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
                .build();

        Paquete paquete = Paquete.builder()
                .descripcion("Paquete de prueba")
                .detalle("Detalle del paquete de prueba")
                .nombre("Paquete de prueba")
                .slug("paquete-de-prueba")
                .duracionDias(8)
                .disponible(true)
                .ultimaModificacion(LocalDateTime.now())
                .destino(destinoRepository.save(destino))
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
        Paquete paquete = paqueteRepository.findById(1L).orElse(null);
        assertThat(paquete).isNotNull();
        assertThat(paquete.getNombre()).isEqualTo("Paquete de prueba");
    }

}
