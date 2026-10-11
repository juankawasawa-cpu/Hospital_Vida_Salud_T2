package pe.cibertec.vidasalud.patients;

import java.util.UUID;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PatientRepository extends ReactiveCrudRepository<Patient, UUID> {
    Mono<Patient> findByTipoDocumentoAndNumeroDocumento(String tipoDocumento, String numeroDocumento);
}
