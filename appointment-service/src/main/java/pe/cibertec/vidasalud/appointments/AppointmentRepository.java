package pe.cibertec.vidasalud.appointments;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface AppointmentRepository extends ReactiveCrudRepository<Appointment, UUID> {
    Mono<Boolean> existsByFechaHoraAndProfesionalCodigoAndEstadoIn(
        OffsetDateTime fechaHora,
        String profesionalCodigo,
        Collection<String> estados
    );

    Mono<Boolean> existsByPacienteIdAndFechaHoraAndEstadoIn(
        UUID pacienteId,
        OffsetDateTime fechaHora,
        Collection<String> estados
    );
}
