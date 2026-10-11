package pe.cibertec.vidasalud.patients;

import java.time.LocalDate;
import java.util.Locale;
import java.util.Set;

public final class PatientRules {
    private static final Set<String> STATUSES = Set.of("ACTIVO", "INACTIVO");

    private PatientRules() {
    }

    public static boolean validDocument(String type, String number) {
        return validText(type, 20) && validText(number, 30);
    }

    public static boolean validName(String value) {
        return validText(value, 120);
    }

    public static boolean validStatus(String value) {
        return value == null || STATUSES.contains(value.trim().toUpperCase(Locale.ROOT));
    }

    public static boolean validRequest(PatientController.PatientRequest request) {
        if (request == null || !validDocument(request.tipoDocumento(), request.numeroDocumento())
            || !validText(request.nombres(), 100) || !validText(request.apellidos(), 120)
            || !validStatus(request.estado())) {
            return false;
        }
        if (request.fechaNacimiento() != null && request.fechaNacimiento().isAfter(LocalDate.now())) {
            return false;
        }
        return validOptionalText(request.telefono(), 30)
            && validOptionalText(request.correo(), 150)
            && validOptionalText(request.direccion(), 250);
    }

    private static boolean validOptionalText(String value, int maxLength) {
        return value == null || value.length() <= maxLength;
    }

    private static boolean validText(String value, int maxLength) {
        return value != null && !value.isBlank() && value.trim().length() <= maxLength;
    }
}
