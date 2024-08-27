package pe.edu.vallegrande.vg_ms_communion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsVgCommunionApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsVgCommunionApplication.class, args);
	}

}
