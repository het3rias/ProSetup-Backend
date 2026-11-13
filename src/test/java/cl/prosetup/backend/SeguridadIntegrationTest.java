package cl.prosetup.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Prueba de integración: autenticación JWT y permisos por rol, con H2 en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    private JsonNode login(String correo, String password) throws Exception {
        String body = mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"" + correo + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body);
    }

    private String token(String correo) throws Exception {
        return "Bearer " + login(correo, "1234").get("accessToken").asText();
    }

    private long primeraCategoriaId() throws Exception {
        String body = mvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get(0).get("id").asLong();
    }

    private String productoJson(String codigo, long categoriaId) {
        return "{\"codigo\":\"" + codigo + "\",\"nombre\":\"Producto de prueba\",\"descripcion\":\"desc\","
                + "\"precio\":10000,\"stock\":5,\"stockCritico\":2,\"imagen\":\"x.jpg\","
                + "\"oferta\":false,\"descuento\":0,\"categoriaId\":" + categoriaId + "}";
    }

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/categorias")).andExpect(status().isOk());
    }

    @Test
    void sinTokenNoSePuedeCrearProducto() throws Exception {
        mvc.perform(post("/api/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("SIN-TOKEN", primeraCategoriaId())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteNoPuedeCrearProductoPeroAdminSi() throws Exception {
        long cat = primeraCategoriaId();
        mvc.perform(post("/api/productos")
                        .header("Authorization", token("cliente@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("CLI-001", cat)))
                .andExpect(status().isForbidden());

        mvc.perform(post("/api/productos")
                        .header("Authorization", token("admin@duoc.cl"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("ADM-001", cat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("ADM-001"));
    }

    @Test
    void soloAdminPuedeListarUsuarios() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization", token("cliente@gmail.com")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header("Authorization", token("vendedor@duoc.cl")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios").header("Authorization", token("admin@duoc.cl")))
                .andExpect(status().isOk());
    }

    @Test
    void loginConClaveIncorrectaDa401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@duoc.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void registroCreaSiempreUnClienteYRechazaRunInvalido() throws Exception {
        String valido = "{\"run\":\"15.555.555-6\",\"nombre\":\"Ana\",\"apellidos\":\"Pérez\","
                + "\"correo\":\"ana@gmail.com\",\"password\":\"abcd\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rol").value("CLIENTE"));

        String invalido = "{\"run\":\"15.555.555-1\",\"nombre\":\"Ana\",\"apellidos\":\"Pérez\","
                + "\"correo\":\"ana2@gmail.com\",\"password\":\"abcd\"}";
        mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshTokenEntregaNuevoAccessToken() throws Exception {
        String refresh = login("cliente@gmail.com", "1234").get("refreshToken").asText();
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void accessTokenNoSirveComoRefreshToken() throws Exception {
        String access = login("cliente@gmail.com", "1234").get("accessToken").asText();
        mvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + access + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void compraDescuentaStockYCalculaTotal() throws Exception {
        String productos = mvc.perform(get("/api/productos")).andReturn().getResponse().getContentAsString();
        JsonNode p = om.readTree(productos).get(0);
        long id = p.get("id").asLong();
        int precioFinal = p.get("precioFinal").asInt();

        mvc.perform(post("/api/ordenes")
                        .header("Authorization", token("cliente@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productoId\":" + id + ",\"cantidad\":2}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.total").value(precioFinal * 2));
    }
}
