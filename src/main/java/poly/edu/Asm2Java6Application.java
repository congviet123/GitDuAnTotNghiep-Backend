package poly.edu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Asm2Java6Application {

	public static void main(String[] args) {
		SpringApplication.run(Asm2Java6Application.class, args);
	}

}