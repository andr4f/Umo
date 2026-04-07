package unimag.proyect;

import org.springframework.boot.SpringApplication;

public class TestProyectApplication {

	public static void main(String[] args) {
		SpringApplication.from(ProyectApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
