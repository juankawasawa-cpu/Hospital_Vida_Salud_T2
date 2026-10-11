package pe.cibertec.vidasalud.patients;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("pacientes")
public record Patient(
    @Id UUID id,
    String tipoDocumento,
    String numeroDocumento,
    String nombres,
    String apellidos,
    LocalDate fechaNacimiento,
    String telefono,
    String correo,
    String direccion,
    OffsetDateTime fechaRegistro,
    String estado
) {
}
