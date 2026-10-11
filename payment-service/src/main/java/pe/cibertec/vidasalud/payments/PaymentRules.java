package pe.cibertec.vidasalud.payments;

import java.math.BigDecimal;

public final class PaymentRules {
    private PaymentRules() {
    }

    public static boolean validAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        BigDecimal normalized = amount.stripTrailingZeros();
        return normalized.scale() <= 2 && normalized.precision() - normalized.scale() <= 10;
    }

    public static boolean validText(String value) {
        return value != null && !value.isBlank();
    }

    public static boolean validRequest(PaymentController.PaymentRequest request) {
        return request != null
            && request.pacienteId() != null
            && validText(request.concepto())
            && request.concepto().length() <= 180
            && validAmount(request.monto())
            && validText(request.medioPago())
            && request.medioPago().length() <= 30
            && validText(request.numeroOperacion())
            && request.numeroOperacion().trim().length() <= 80;
    }
}
