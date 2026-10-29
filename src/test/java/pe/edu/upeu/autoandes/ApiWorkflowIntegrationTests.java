package pe.edu.upeu.autoandes;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class ApiWorkflowIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void orderTotalIsCalculatedFromOracleServicePrices() throws Exception {
        String token = loginAndGetToken("asesor");
        long vehiculoId = idFor("SELECT ID FROM VEHICULO WHERE PLACA = 'AAA-111'");
        long servicioId = idFor("SELECT ID FROM SERVICIO WHERE CODIGO = 'SRV-001'");

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": %d,
                                  "kilometraje": 45000,
                                  "detalles": [{"servicioId": %d, "cantidad": 2}]
                                }
                                """.formatted(vehiculoId, servicioId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(240.0))
                .andExpect(jsonPath("$.detalles", hasSize(1)))
                .andExpect(jsonPath("$.detalles[0].precioUnitario").value(120.0))
                .andExpect(jsonPath("$.detalles[0].subtotal").value(240.0));
    }

    @Test
    @Transactional
    void orderRejectsRepeatedAndInactiveServices() throws Exception {
        String token = loginAndGetToken("asesor");
        long vehiculoId = idFor("SELECT ID FROM VEHICULO WHERE PLACA = 'AAA-111'");
        long servicioActivoId = idFor("SELECT ID FROM SERVICIO WHERE CODIGO = 'SRV-001'");
        long servicioInactivoId = idFor("SELECT ID FROM SERVICIO WHERE CODIGO = 'SRV-008'");

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": %d,
                                  "kilometraje": 45000,
                                  "detalles": [
                                    {"servicioId": %d, "cantidad": 1},
                                    {"servicioId": %d, "cantidad": 1}
                                  ]
                                }
                                """.formatted(vehiculoId, servicioActivoId, servicioActivoId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": %d,
                                  "kilometraje": 45000,
                                  "detalles": [{"servicioId": %d, "cantidad": 1}]
                                }
                                """.formatted(vehiculoId, servicioInactivoId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Transactional
    void orderRejectsKilometrajeBelowTheLastRegisteredValue() throws Exception {
        String token = loginAndGetToken("asesor");
        long vehiculoId = idFor("SELECT ID FROM VEHICULO WHERE PLACA = 'AAA-111'");
        long servicioId = idFor("SELECT ID FROM SERVICIO WHERE CODIGO = 'SRV-001'");

        mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": %d,
                                  "kilometraje": 44999,
                                  "detalles": [{"servicioId": %d, "cantidad": 1}]
                                }
                                """.formatted(vehiculoId, servicioId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    @Transactional
    void orderCanBeCancelledOnTheSameDayWithAValidReason() throws Exception {
        String token = loginAndGetToken("asesor");
        long vehiculoId = idFor("SELECT ID FROM VEHICULO WHERE PLACA = 'AAA-111'");
        long servicioId = idFor("SELECT ID FROM SERVICIO WHERE CODIGO = 'SRV-001'");
        MvcResult created = mockMvc.perform(post("/api/v1/ordenes")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "vehiculoId": %d,
                                  "kilometraje": 45000,
                                  "detalles": [{"servicioId": %d, "cantidad": 1}]
                                }
                                """.formatted(vehiculoId, servicioId)))
                .andExpect(status().isOk())
                .andReturn();
        Number ordenId = JsonPath.read(created.getResponse().getContentAsString(), "$.id");

        mockMvc.perform(patch("/api/v1/ordenes/" + ordenId.longValue() + "/anular")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"motivo":"Solicitud de prueba válida"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ANULADA"))
                .andExpect(jsonPath("$.motivoAnulacion").value("Solicitud de prueba válida"));
    }

    @Test
    void duplicateServiceAndVehicleIdentifiersReturn409() throws Exception {
        String token = loginAndGetToken("admin");

        mockMvc.perform(post("/api/v1/servicios")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "codigo": "SRV-001",
                                  "nombre": "Servicio duplicado",
                                  "precio": 99.00,
                                  "duracionMinutos": 30
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        mockMvc.perform(post("/api/v1/vehiculos")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "placa": "AAA-111",
                                  "marca": "Toyota",
                                  "modelo": "Corolla",
                                  "anio": 2019,
                                  "clienteId": %d
                                }
                                """.formatted(idFor("SELECT ID FROM CLIENTE WHERE DOCUMENTO = '70123456'"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void orderSearchReturnsAnEmptyJsonArrayWhenThereAreNoResults() throws Exception {
        String token = loginAndGetToken("asesor");

        mockMvc.perform(get("/api/v1/ordenes")
                        .param("placa", "ZZZ-000")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void reportUsesAggregatedResultsAndExcludesCancelledOrders() throws Exception {
        String token = loginAndGetToken("admin");
        MvcResult result = mockMvc.perform(get("/api/v1/reportes/ingresos-por-servicio")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        List<Number> importes = JsonPath.read(
                result.getResponse().getContentAsString(),
                "$[?(@.codigoServicio == 'SRV-001')].importeTotal");
        org.junit.jupiter.api.Assertions.assertFalse(importes.isEmpty());
        org.junit.jupiter.api.Assertions.assertEquals(120.0, importes.getFirst().doubleValue());
    }

    private String loginAndGetToken(String username) throws Exception {
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"%s","password":"Auto2026*"}
                                """.formatted(username)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.token");
    }

    private long idFor(String sql) {
        Long id = jdbcTemplate.queryForObject(sql, Long.class);
        if (id == null) {
            throw new IllegalStateException("Expected seeded record was not found");
        }
        return id;
    }
}
