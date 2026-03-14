package br.com.fiap.v2i.processing;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableRabbit
public class V2iProcessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(V2iProcessingApplication.class, args);
	}

}
