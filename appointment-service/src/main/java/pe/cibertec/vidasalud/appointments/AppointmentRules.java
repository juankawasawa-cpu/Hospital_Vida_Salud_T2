package pe.cibertec.vidasalud.appointments;

import java.util.Locale;
import java.util.Set;

public final class AppointmentRules {
    private static final Set<String> STATUSES = Set.of("PENDIENTE", "CONFIRMADA", "ATENDIDA", "CANCELADA", "AUSENTE");

    private AppointmentRules() {
    }

    public static String normalizeStatus(String status) {
        return status == null ? "" : status.trim().toUpperCase(Locale.ROOT);
    }

    public static boolean validStatus(String status) {
        return STATUSES.contains(normalizeStatus(status));
    }

    public static boolean allowed(String current, String next) {
        String from = normalizeStatus(current);
        String to = normalizeStatus(next);
        return switch (from) {
            case "PENDIENTE" -> to.equals("CONFIRMADA") || to.equals("CANCELADA") || to.equals("AUSENTE");
            case "CONFIRMADA" -> to.equals("ATENDIDA") || to.equals("CANCELADA") || to.equals("AUSENTE");
            default -> false;
        };
    }
}
