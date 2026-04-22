package unimag.proyect.api.controllers.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import tools.jackson.databind.json.JsonMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Base para {@code @WebMvcTest}: reduce duplicación en pruebas HTTP (JSON, verbos).
 * Usa Jackson 3 ({@link JsonMapper}) alineado con Spring Boot 4.
 */
public abstract class ControllerMvcSliceTest {

    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired
    protected MockMvc mockMvc;

    protected ResultActions postJson(String url, Object body) throws Exception {
        return mockMvc.perform(
                post(url).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(body)));
    }

    protected ResultActions postRawJson(String url, String json) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions patchJson(String url, Object body) throws Exception {
        return mockMvc.perform(
                patch(url).contentType(MediaType.APPLICATION_JSON).content(JSON.writeValueAsString(body)));
    }

    protected ResultActions patchRawJson(String url, String json) throws Exception {
        return mockMvc.perform(patch(url).contentType(MediaType.APPLICATION_JSON).content(json));
    }

    protected ResultActions getJson(String url) throws Exception {
        return mockMvc.perform(get(url).accept(MediaType.APPLICATION_JSON));
    }
}
