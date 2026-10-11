package pe.cibertec.vidasalud.patients;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pacientes")
public class PatientController {
    private final PatientRepository repository;

    public PatientController(PatientRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Flux<Patient> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Patient> get(@PathVariable UUID id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Patient> create(@RequestBody PatientRequest request) {
        if (!PatientRules.validRequest(request)) {
            return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tipo y número de documento, nombres y apellidos son obligatorios; el estado debe ser ACTIVO o INACTIVO"
            ));
        }

        String documentType = normalize(request.tipoDocumento());
        String documentNumber = request.numeroDocumento().trim();
        String initialStatus = request.estado() == null ? "ACTIVO" : request.estado().trim().toUpperCase(Locale.ROOT);

        return repository.findByTipoDocumentoAndNumeroDocumento(documentType, documentNumber)
            .flatMap(existing -> Mono.<Patient>error(new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe un paciente con ese documento")))
            .switchIfEmpty(Mono.defer(() -> repository.save(new Patient(
                UUID.randomUUID(),
                documentType,
                documentNumber,
                request.nombres().trim(),
                request.apellidos().trim(),
                request.fechaNacimiento(),
                clean(request.telefono()),
                clean(request.correo()),
                clean(request.direccion()),
                OffsetDateTime.now(),
                initialStatus
            ))))
            .onErrorMap(DataIntegrityViolationException.class, error -> new ResponseStatusException(
                HttpStatus.CONFLICT, "Ya existe un paciente con ese documento", error));
    }

    @PutMapping("/{id}")
    public Mono<Patient> update(@PathVariable UUID id, @RequestBody PatientRequest request) {
        if (!PatientRules.validRequest(request)) {
            return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Tipo y número de documento, nombres y apellidos son obligatorios; el estado debe ser ACTIVO o INACTIVO"
            ));
        }

        String documentType = normalize(request.tipoDocumento());
        String documentNumber = request.numeroDocumento().trim();
        String status = request.estado() == null ? null : request.estado().trim().toUpperCase(Locale.ROOT);

        return repository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado")))
            .flatMap(existing -> repository.findByTipoDocumentoAndNumeroDocumento(documentType, documentNumber)
                .filter(other -> !other.id().equals(id))
                .flatMap(other -> Mono.<Patient>error(new ResponseStatusException(
                    HttpStatus.CONFLICT, "Documento asociado a otro paciente")))
                .switchIfEmpty(Mono.defer(() -> repository.save(new Patient(
                    existing.id(),
                    documentType,
                    documentNumber,
                    request.nombres().trim(),
                    request.apellidos().trim(),
                    request.fechaNacimiento(),
                    clean(request.telefono()),
                    clean(request.correo()),
                    clean(request.direccion()),
                    existing.fechaRegistro(),
                    status == null ? existing.estado() : status
                )))))
            .onErrorMap(DataIntegrityViolationException.class, error -> new ResponseStatusException(
                HttpStatus.CONFLICT, "El documento ya está registrado", error));
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record PatientRequest(
        String tipoDocumento,
        String numeroDocumento,
        String nombres,
        String apellidos,
        java.time.LocalDate fechaNacimiento,
        String telefono,
        String correo,
        String direccion,
        String estado
    ) {
    }
}
