package pe.cibertec.vidasalud.appointments;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Mono;

class AppointmentControllerTest {
    private static final UUID PATIENT_ID = UUID.fromString("f2428273-a3c7-4f41-b771-6ee7f2f8babe");
    private static final OffsetDateTime SLOT = OffsetDateTime.parse("2027-01-20T10:00:00-05:00");

    @Test
    void createsAppointmentForExistingPatient() {
        AppointmentRepository repository = mock(AppointmentRepository.class);
        PatientClient patients = mock(PatientClient.class);
        AppointmentEvents events = mock(AppointmentEvents.class);
        when(patients.exists(PATIENT_ID)).thenReturn(Mono.just(true));
        when(repository.existsByFechaHoraAndProfesionalCodigoAndEstadoIn(eq(SLOT), eq("AGENDA-DEMO-01"), anyCollection()))
            .thenReturn(Mono.just(false));
        when(repository.existsByPacienteIdAndFechaHoraAndEstadoIn(eq(PATIENT_ID), eq(SLOT), anyCollection()))
            .thenReturn(Mono.just(false));
        when(repository.save(any(Appointment.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        WebTestClient.bindToController(new AppointmentController(repository, patients, events)).build()
            .post().uri("/api/citas")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"pacienteId":"f2428273-a3c7-4f41-b771-6ee7f2f8babe","especialidad":"Medicina general","fechaHora":"2027-01-20T10:00:00-05:00","profesionalCodigo":"AGENDA-DEMO-01","motivoAdministrativo":"Control de prueba"}
                """)
            .exchange()
            .expectStatus().isCreated()
            .expectBody()
            .jsonPath("$.estado").isEqualTo("PENDIENTE")
            .jsonPath("$.pacienteId").isEqualTo(PATIENT_ID.toString());
    }

    @Test
    void returnsNotFoundWhenAppointmentDoesNotExist() {
        UUID appointmentId = UUID.randomUUID();
        AppointmentRepository repository = mock(AppointmentRepository.class);
        when(repository.findById(appointmentId)).thenReturn(Mono.empty());

        WebTestClient.bindToController(new AppointmentController(
            repository, mock(PatientClient.class), mock(AppointmentEvents.class)
        )).build()
            .get().uri("/api/citas/{id}", appointmentId)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void rejectsConfirmingCancelledAppointment() {
        UUID appointmentId = UUID.randomUUID();
        Appointment cancelled = new Appointment(
            appointmentId, PATIENT_ID, "Medicina general", SLOT, "AGENDA-DEMO-01", "Demo", "CANCELADA", SLOT, SLOT
        );
        AppointmentRepository repository = mock(AppointmentRepository.class);
        when(repository.findById(appointmentId)).thenReturn(Mono.just(cancelled));

        WebTestClient.bindToController(new AppointmentController(
            repository, mock(PatientClient.class), mock(AppointmentEvents.class)
        )).build()
            .patch().uri("/api/citas/{id}/estado", appointmentId)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""
                {"estado":"CONFIRMADA"}
                """)
            .exchange()
            .expectStatus().isEqualTo(409);
    }
}
