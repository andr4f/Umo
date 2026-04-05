package unimag.proyect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import unimag.proyect.config.TestSecurityBeansConfig;

@ActiveProfiles("test")
@SpringBootTest
@Import({TestcontainersConfiguration.class, TestSecurityBeansConfig.class})
class ProyectApplicationTests {

    @Test
    void contextLoads() {
    }
}