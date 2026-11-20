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

/** Prueba de integración: autenticación JWT, permisos por rol y compra, con H2 en memoria. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SeguridadIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    private JsonNode login(String email, String password) throws Exception {
        String body = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body);
    }

    private String token(String email) throws Exception {
        return "Bearer " + login(email, "1234").get("accessToken").asText();
    }

    private String nombrePrimeraCategoria() throws Exception {
        String body = mvc.perform(get("/api/v1/categorias"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return om.readTree(body).get(0).get("nombre").asText();
    }

    private String productoJson(String codigo, String categoria) {
        return "{\"codigo\":\"" + codigo + "\",\"nombre\":\"Producto de prueba\",\"descripcion\":\"desc\","
                + "\"precio\":10000,\"precioOferta\":null,\"stock\":5,\"stockCritico\":2,\"imagen\":\"/img/x.jpg\","
                + "\"categoria\":\"" + categoria + "\"}";
    }

    private JsonNode primerProducto() throws Exception {
        String body = mvc.perform(get("/api/v1/productos")).andReturn().getResponse().getContentAsString();
        return om.readTree(body).get(0);
    }

    @Test
    void catalogoEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
        mvc.perform(get("/api/v1/categorias")).andExpect(status().isOk());
    }

    @Test
    void sinTokenNoSePuedeCrearProducto() throws Exception {
        mvc.perform(post("/api/v1/productos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(productoJson("SIN-TOKEN", nombrePrimeraCategoria())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void soloAdminPuedeCrearProductos() throws Exception {
        String cat = nombrePrimeraCategoria();
        mvc.perform(post("/api/v1/productos").header("Authorization", token("cliente@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("CLI-001", cat)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/productos").header("Authorization", token("vendedor@duoc.cl"))
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("VEN-001", cat)))
                .andExpect(status().isForbidden());
        mvc.perform(post("/api/v1/productos").header("Authorization", token("admin@duoc.cl"))
                        .contentType(MediaType.APPLICATION_JSON).content(productoJson("ADM-001", cat)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("ADM-001"));
    }

    @Test
    void soloAdminPuedeListarUsuarios() throws Exception {
        mvc.perform(get("/api/v1/usuarios").header("Authorization", token("cliente@gmail.com")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", token("vendedor@duoc.cl")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/usuarios").header("Authorization", token("admin@duoc.cl")))
                .andExpect(status().isOk());
    }

    @Test
    void ordenesLasVeSoloElPersonal() throws Exception {
        mvc.perform(get("/api/v1/ordenes").header("Authorization", token("cliente@gmail.com")))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/ordenes").header("Authorization", token("vendedor@duoc.cl")))
                .andExpect(status().isOk());
    }

    @Test
    void vendedorSoloConsultaProductosYOrdenes() throws Exception {
        String vendedor = token("vendedor@duoc.cl");
        mvc.perform(get("/api/v1/productos").header("Authorization", vendedor)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/ordenes").header("Authorization", vendedor)).andExpect(status().isOk());
        mvc.perform(get("/api/v1/productos/criticos").header("Authorization", vendedor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/dashboard").header("Authorization", vendedor)).andExpect(status().isForbidden());
        mvc.perform(get("/api/v1/admin/dashboard").header("Authorization", token("admin@duoc.cl"))).andExpect(status().isOk());
    }

    @Test
    void lasRutasAntiguasSinVersionYaNoExisten() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().is4xxClientError());
    }

    @Test
    void loginConClaveIncorrectaDa401() throws Exception {
        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@duoc.cl\",\"password\":\"mala\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meDevuelveElUsuarioConSuTipo() throws Exception {
        mvc.perform(get("/api/v1/auth/me").header("Authorization", token("vendedor@duoc.cl")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoUsuario").value("Vendedor"))
                .andExpect(jsonPath("$.email").value("vendedor@duoc.cl"));
    }

    @Test
    void registroCreaSiempreUnClienteYRechazaRunInvalido() throws Exception {
        String valido = "{\"run\":\"155555556\",\"nombre\":\"Ana\",\"apellidos\":\"Pérez\","
                + "\"email\":\"ana@gmail.com\",\"password\":\"abcd\"}";
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(valido))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoUsuario").value("Cliente"));

        String invalido = "{\"run\":\"155555551\",\"nombre\":\"Ana\",\"apellidos\":\"Pérez\","
                + "\"email\":\"ana2@gmail.com\",\"password\":\"abcd\"}";
        mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(invalido))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refreshTokenEntregaNuevoAccessToken() throws Exception {
        String refresh = login("cliente@gmail.com", "1234").get("refreshToken").asText();
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refresh + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void accessTokenNoSirveComoRefreshToken() throws Exception {
        String access = login("cliente@gmail.com", "1234").get("accessToken").asText();
        mvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + access + "\"}"))
                .andExpect(status().isUnauthorized());
    }

    private String ordenJson(long productoId, int cantidad, boolean simularRechazo) {
        return "{\"cliente\":{\"nombre\":\"Camila\",\"apellidos\":\"Rojas\",\"email\":\"cliente@gmail.com\"},"
                + "\"direccion\":{\"calle\":\"Los Crisantemos 1450\",\"departamento\":\"\",\"region\":\"RM\",\"comuna\":\"La Florida\",\"indicaciones\":\"\"},"
                + "\"entrega\":\"domicilio\",\"simularRechazo\":" + simularRechazo + ","
                + "\"items\":[{\"id\":" + productoId + ",\"cantidad\":" + cantidad + "}]}";
    }

    @Test
    void invitadoPuedeComprarYElServidorCalculaElTotal() throws Exception {
        JsonNode p = primerProducto();
        long id = p.get("id").asLong();
        int precio = p.get("precioOferta").isNull() ? p.get("precio").asInt() : p.get("precioOferta").asInt();
        int subtotal = precio * 2;
        int envio = subtotal >= 50000 ? 0 : 3990;

        mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(id, 2, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("Pagada"))
                .andExpect(jsonPath("$.subtotal").value(subtotal))
                .andExpect(jsonPath("$.total").value(subtotal + envio))
                .andExpect(jsonPath("$.usuarioEmail").doesNotExist());
    }

    @Test
    void ordenConSesionQuedaAsociadaAlUsuario() throws Exception {
        long id = primerProducto().get("id").asLong();
        mvc.perform(post("/api/v1/ordenes").header("Authorization", token("cliente@gmail.com"))
                        .contentType(MediaType.APPLICATION_JSON).content(ordenJson(id, 1, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioEmail").value("cliente@gmail.com"));
    }

    @Test
    void ordenSinStockSuficienteQuedaRechazadaYNoDescuentaStock() throws Exception {
        JsonNode p = primerProducto();
        long id = p.get("id").asLong();
        int stock = p.get("stock").asInt();

        mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(id, stock + 1, false)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("Rechazada"));

        mvc.perform(get("/api/v1/productos/" + id))
                .andExpect(jsonPath("$.stock").value(stock));
    }

    @Test
    void pagoRechazadoSimuladoNoDescuentaStock() throws Exception {
        JsonNode p = primerProducto();
        long id = p.get("id").asLong();
        int stock = p.get("stock").asInt();

        mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(id, 1, true)))
                .andExpect(jsonPath("$.estado").value("Rechazada"));
        mvc.perform(get("/api/v1/productos/" + id)).andExpect(jsonPath("$.stock").value(stock));
    }

    @Test
    void soloAdminCambiaElEstadoDeUnaOrden() throws Exception {
        long id = primerProducto().get("id").asLong();
        String orden = mvc.perform(post("/api/v1/ordenes").contentType(MediaType.APPLICATION_JSON).content(ordenJson(id, 1, false)))
                .andReturn().getResponse().getContentAsString();
        long ordenId = om.readTree(orden).get("id").asLong();

        mvc.perform(put("/api/v1/ordenes/" + ordenId + "/estado").header("Authorization", token("vendedor@duoc.cl"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Enviada\"}"))
                .andExpect(status().isForbidden());
        mvc.perform(put("/api/v1/ordenes/" + ordenId + "/estado").header("Authorization", token("admin@duoc.cl"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"Enviada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("Enviada"));
    }
}
