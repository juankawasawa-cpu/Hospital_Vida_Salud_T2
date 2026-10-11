package pe.cibertec.vidasalud.payments;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("pagos")
public record Payment(
    @Id UUID id,
    UUID pacienteId,
    String concepto,
    BigDecimal monto,
    OffsetDateTime fechaPago,
    String medioPago,
    String numeroOperacion,
    String estado
) {
}
