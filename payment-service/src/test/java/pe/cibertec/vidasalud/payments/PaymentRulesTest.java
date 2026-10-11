package pe.cibertec.vidasalud.payments;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentRulesTest {
    @Test
    void acceptsPositiveAmount() {
        assertTrue(PaymentRules.validAmount(new BigDecimal("25.50")));
    }

    @Test
    void rejectsZeroAmount() {
        assertFalse(PaymentRules.validAmount(BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeAmount() {
        assertFalse(PaymentRules.validAmount(new BigDecimal("-0.01")));
    }

    @Test
    void rejectsAmountsWithMoreThanTwoDecimalPlaces() {
        assertFalse(PaymentRules.validAmount(new BigDecimal("0.001")));
    }

    @Test
    void rejectsAmountsOutsideDatabaseRange() {
        assertFalse(PaymentRules.validAmount(new BigDecimal("10000000000.00")));
    }

    @Test
    void rejectsIncompletePayment() {
        PaymentController.PaymentRequest request = new PaymentController.PaymentRequest(
            null, "Consulta administrativa", BigDecimal.ONE, "EFECTIVO", "OP-DEMO-01"
        );
        assertFalse(PaymentRules.validRequest(request));
    }
}
