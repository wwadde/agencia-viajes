package com.agencia;

import com.agencia.config.PaymentProperties;
import com.agencia.config.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableConfigurationProperties({StorageProperties.class, PaymentProperties.class})
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
@EnableAsync
public class AgenciaApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
		SpringApplication.run(AgenciaApplication.class, args);
	}


}
