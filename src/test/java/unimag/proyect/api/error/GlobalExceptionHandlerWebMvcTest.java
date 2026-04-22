package unimag.proyect.api.error;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import unimag.proyect.exceptions.BusinessException;
import unimag.proyect.exceptions.DuplicateResourceException;
import unimag.proyect.exceptions.InactiveEntityException;
import unimag.proyect.exceptions.InvalidDateRangeException;
import unimag.proyect.exceptions.InvalidStateTransitionException;
import unimag.proyect.exceptions.ResourceNotFoundException;
import unimag.proyect.exceptions.ScheduleConflictException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba el {@link GlobalExceptionHandler} de forma aislada (sin levantar el contexto completo),
 * manteniendo mapeos HTTP verificables cuando se añadan nuevas excepciones de dominio.
 */
class GlobalExceptionHandlerWebMvcTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        var springValidator = new LocalValidatorFactoryBean();
        springValidator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(new ErrorProbeController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(springValidator)
                .build();
    }

    @Test
    void resourceNotFound_returns404() throws Exception {
        mockMvc.perform(get("/probe/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("missing"));
    }

    @Test
    void businessException_returns422() throws Exception {
        mockMvc.perform(get("/probe/business"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    void invalidStateTransition_returns422() throws Exception {
        mockMvc.perform(get("/probe/invalid-state"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void inactiveEntity_returns422() throws Exception {
        mockMvc.perform(get("/probe/inactive"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void scheduleConflict_returns409() throws Exception {
        mockMvc.perform(get("/probe/schedule-conflict"))
                .andExpect(status().isConflict());
    }

    @Test
    void duplicateResource_returns409() throws Exception {
        mockMvc.perform(get("/probe/duplicate"))
                .andExpect(status().isConflict());
    }

    @Test
    void invalidDateRange_returns400() throws Exception {
        mockMvc.perform(get("/probe/bad-range"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void methodArgumentNotValid_returns400WithViolations() throws Exception {
        mockMvc.perform(post("/probe/valid-body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.violations", hasSize(1)));
    }

    @RestController
    @RequestMapping("/probe")
    static class ErrorProbeController {

        public record ValidatedBody(@NotBlank(message = "x required") String x) {}

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("missing");
        }

        @GetMapping("/business")
        void business() {
            throw new BusinessException("rule");
        }

        @GetMapping("/invalid-state")
        void invalidState() {
            throw new InvalidStateTransitionException("bad transition");
        }

        @GetMapping("/inactive")
        void inactive() {
            throw new InactiveEntityException("inactive");
        }

        @GetMapping("/schedule-conflict")
        void conflict() {
            throw new ScheduleConflictException("overlap");
        }

        @GetMapping("/duplicate")
        void duplicate() {
            throw new DuplicateResourceException("dup");
        }

        @GetMapping("/bad-range")
        void badRange() {
            throw new InvalidDateRangeException("range");
        }

        @PostMapping("/valid-body")
        void validBody(@Valid @RequestBody ValidatedBody body) {
            // no-op
        }
    }
}
