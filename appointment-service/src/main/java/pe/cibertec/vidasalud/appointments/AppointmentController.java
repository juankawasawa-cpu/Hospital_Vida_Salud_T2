package pe.cibertec.vidasalud.appointments;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/citas")
public class AppointmentController {
    private final AppointmentRepository repository;
    private final PatientClient patients;
    private final AppointmentEvents events;

    AppointmentController(AppointmentRepository repository, PatientClient patients, AppointmentEvents events) {
        this.repository = repository;
        this.patients = patients;
        this.events = events;
    }

    @GetMapping
    public Flux<Appointment> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Appointment> get(@PathVariable UUID id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Appointment> create(@RequestBody Request request) {
        if (request == null || request.pacienteId() == null || !validText(request.especialidad(), 100)
            || request.fechaHora() == null || !validText(request.profesionalCodigo(), 50)
            || !validOptionalText(request.motivoAdministrativo(), 250)) {
            return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Paciente, especialidad, fecha/hora y profesional son obligatorios"
            ));
        }

        if (!request.fechaHora().isAfter(OffsetDateTime.now())) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cita debe ser futura"));
        }

        return patients.exists(request.pacienteId())
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
                }
                java.util.List<String> activeStates = java.util.List.of("PENDIENTE", "CONFIRMADA");
                return Mono.zip(
                        repository.existsByFechaHoraAndProfesionalCodigoAndEstadoIn(
                            request.fechaHora(), request.profesionalCodigo().trim(), activeStates),
                        repository.existsByPacienteIdAndFechaHoraAndEstadoIn(
                            request.pacienteId(), request.fechaHora(), activeStates)
                    )
                    .flatMap(conflicts -> {
                        if (conflicts.getT1() || conflicts.getT2()) {
                            return Mono.error(new ResponseStatusException(
                                HttpStatus.CONFLICT, "El paciente o el profesional ya tiene una cita en ese horario"));
                        }
                        OffsetDateTime now = OffsetDateTime.now();
                        Appointment appointment = new Appointment(
                            UUID.randomUUID(),
                            request.pacienteId(),
                            request.especialidad().trim(),
                            request.fechaHora(),
                            request.profesionalCodigo().trim(),
                            clean(request.motivoAdministrativo()),
                            "PENDIENTE",
                            now,
                            now
                        );
                        return repository.save(appointment);
                    });
            })
            .onErrorMap(DataIntegrityViolationException.class, error -> new ResponseStatusException(
                HttpStatus.CONFLICT, "El horario del profesional ya está ocupado", error));
    }

    @PatchMapping("/{id}/estado")
    public Mono<Appointment> changeState(@PathVariable UUID id, @RequestBody StateRequest request) {
        String next = request == null ? "" : AppointmentRules.normalizeStatus(request.estado());
        if (!AppointmentRules.validStatus(next)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de cita no válido"));
        }

        return repository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Cita no encontrada")))
            .flatMap(current -> {
                if (!AppointmentRules.allowed(current.estado(), next)) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Transición no permitida: " + current.estado() + " -> " + next
                    ));
                }

                OffsetDateTime now = OffsetDateTime.now();
                Appointment updated = new Appointment(
                    current.id(),
                    current.pacienteId(),
                    current.especialidad(),
                    current.fechaHora(),
                    current.profesionalCodigo(),
                    current.motivoAdministrativo(),
                    next,
                    current.fechaCreacion(),
                    now
                );

                return repository.save(updated)
                    .flatMap(saved -> {
                        String eventName = "CONFIRMADA".equals(next) ? "CitaConfirmada" : "CitaEstadoActualizado";
                        AppointmentEvents.AppointmentEvent event = new AppointmentEvents.AppointmentEvent(
                            saved.id(), saved.pacienteId(), eventName, current.estado(), saved.estado(), now,
                            "appointment-service"
                        );
                        return events.publish(event).thenReturn(saved);
                    });
            });
    }

    private static boolean validText(String value, int maxLength) {
        return value != null && !value.isBlank() && value.trim().length() <= maxLength;
    }

    private static boolean validOptionalText(String value, int maxLength) {
        return value == null || value.length() <= maxLength;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record Request(
        UUID pacienteId,
        String especialidad,
        OffsetDateTime fechaHora,
        String profesionalCodigo,
        String motivoAdministrativo
    ) {
    }

    public record StateRequest(String estado) {
    }
}
