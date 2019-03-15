package org.cosns;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing 
public class CosnsApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosnsApplication.class, args);
	}

}
