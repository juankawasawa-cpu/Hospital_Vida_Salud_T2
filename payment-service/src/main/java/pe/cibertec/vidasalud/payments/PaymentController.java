package pe.cibertec.vidasalud.payments;

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/pagos")
public class PaymentController {
    private final PaymentRepository repository;
    private final PaymentPatientClient patients;

    public PaymentController(PaymentRepository repository, PaymentPatientClient patients) {
        this.repository = repository;
        this.patients = patients;
    }

    @GetMapping
    public Flux<Payment> all() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public Mono<Payment> get(@PathVariable UUID id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Pago no encontrado")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Payment> create(@RequestBody PaymentRequest request) {
        if (!PaymentRules.validRequest(request)) {
            return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Paciente, concepto, monto mayor que cero, medio de pago y número de operación son obligatorios"
            ));
        }

        String operationNumber = request.numeroOperacion().trim();
        return patients.exists(request.pacienteId())
            .flatMap(exists -> {
                if (!exists) {
                    return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Paciente no encontrado"));
                }
                return repository.findByNumeroOperacion(operationNumber)
                    .flatMap(existing -> Mono.<Payment>error(new ResponseStatusException(
                        HttpStatus.CONFLICT, "El número de operación ya fue registrado")))
                    .switchIfEmpty(Mono.defer(() -> repository.save(new Payment(
                        UUID.randomUUID(),
                        request.pacienteId(),
                        request.concepto().trim(),
                        request.monto(),
                        OffsetDateTime.now(),
                        request.medioPago().trim().toUpperCase(Locale.ROOT),
                        operationNumber,
                        "REGISTRADO"
                    ))));
            })
            .onErrorMap(DataIntegrityViolationException.class, error -> new ResponseStatusException(
                HttpStatus.CONFLICT, "El número de operación ya fue registrado", error));
    }

    public record PaymentRequest(
        UUID pacienteId,
        String concepto,
        java.math.BigDecimal monto,
        String medioPago,
        String numeroOperacion
    ) {
    }
}
