package pe.cibertec.vidasalud.payments;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.Mockito.mock;

class PaymentControllerTest {
    @Test
    void rejectsPaymentWithZeroAmount() {
        PaymentController controller = new PaymentController(mock(PaymentRepository.class), mock(PaymentPatientClient.class));

        WebTestClient.bindToController(controller).build()
            .post().uri("/api/pagos")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"pacienteId\":\"f2428273-a3c7-4f41-b771-6ee7f2f8babe\",\"concepto\":\"Servicio demo\",\"monto\":0,\"medioPago\":\"EFECTIVO\",\"numeroOperacion\":\"OP-DEMO-01\"}")
            .exchange()
            .expectStatus().isBadRequest();
    }

    @Test
    void rejectsPaymentWithNegativeAmount() {
        PaymentController controller = new PaymentController(mock(PaymentRepository.class), mock(PaymentPatientClient.class));

        WebTestClient.bindToController(controller).build()
            .post().uri("/api/pagos")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{\"pacienteId\":\"f2428273-a3c7-4f41-b771-6ee7f2f8babe\",\"concepto\":\"Servicio demo\",\"monto\":-1.50,\"medioPago\":\"EFECTIVO\",\"numeroOperacion\":\"OP-DEMO-02\"}")
            .exchange()
            .expectStatus().isBadRequest();
    }
}
