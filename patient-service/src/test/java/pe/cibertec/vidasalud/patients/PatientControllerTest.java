package pe.cibertec.vidasalud.patients;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;

class PatientControllerTest {
    @Test
    void returnsCreatedForValidPatient() {
        PatientRepository repository = mock(PatientRepository.class);
        PatientController controller = new PatientController(repository);
        when(repository.findByTipoDocumentoAndNumeroDocumento("DNI-DEMO", "00000001"))
            .thenReturn(Mono.empty());
        when(repository.save(any(Patient.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        WebTestClient.bindToController(controller).build()
            .post().uri("/api/pacientes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"tipoDocumento":"DNI-DEMO","numeroDocumento":"00000001","nombres":"Ana","apellidos":"Prueba"}
                """)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.tipoDocumento").isEqualTo("DNI-DEMO")
            .jsonPath("$.estado").isEqualTo("ACTIVO");
    }

    @Test
    void returnsConflictForDuplicatePatientDocument() {
        PatientRepository repository = mock(PatientRepository.class);
        Patient existing = new Patient(
            UUID.randomUUID(), "DNI-DEMO", "00000001", "Ana", "Prueba", null, null, null, null,
            OffsetDateTime.parse("2026-10-11T10:00:00-05:00"), "ACTIVO"
        );
        when(repository.findByTipoDocumentoAndNumeroDocumento("DNI-DEMO", "00000001"))
            .thenReturn(Mono.just(existing));

        WebTestClient.bindToController(new PatientController(repository)).build()
            .post().uri("/api/pacientes")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"tipoDocumento":"DNI-DEMO","numeroDocumento":"00000001","nombres":"Ana","apellidos":"Prueba"}
                """)
            .exchange()
            .expectStatus().isEqualTo(409);
    }
    @Test
    void returnsNotFoundWhenPatientDoesNotExist() {
        UUID patientId = UUID.randomUUID();
        PatientRepository repository = mock(PatientRepository.class);
        when(repository.findById(patientId)).thenReturn(Mono.empty());

        WebTestClient.bindToController(new PatientController(repository)).build()
            .get().uri("/api/pacientes/{id}", patientId)
            .exchange()
            .expectStatus().isNotFound();
    }

}
