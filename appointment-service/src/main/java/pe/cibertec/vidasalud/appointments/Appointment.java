package pe.cibertec.vidasalud.appointments;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("citas")
public record Appointment(
    @Id UUID id,
    UUID pacienteId,
    String especialidad,
    OffsetDateTime fechaHora,
    String profesionalCodigo,
    String motivoAdministrativo,
    String estado,
    OffsetDateTime fechaCreacion,
    OffsetDateTime fechaActualizacion
) {
}
