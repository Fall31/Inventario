package com.company.inventory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class InventorySp3ApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	@Timeout(60)
	void mainArrancaLaAplicacionEnModoSinServidorWeb() {
		InventorySp3Application.main(new String[]{
				"--spring.main.web-application-type=none",
				"--spring.jmx.enabled=false"
		});
	}

}
