package pe.cibertec.vidasalud.appointments;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppointmentRulesTest {
    @Test
    void pendingCanBeConfirmed() {
        assertTrue(AppointmentRules.allowed("PENDIENTE", "CONFIRMADA"));
    }

    @Test
    void stateComparisonIgnoresLetterCaseAndOuterSpaces() {
        assertTrue(AppointmentRules.allowed(" pendiente ", "confirmada"));
    }

    @Test
    void cancelledAppointmentCannotBeConfirmed() {
        assertFalse(AppointmentRules.allowed("CANCELADA", "CONFIRMADA"));
    }

    @Test
    void attendedAppointmentCannotBeCancelledAgain() {
        assertFalse(AppointmentRules.allowed("ATENDIDA", "CANCELADA"));
    }

    @Test
    void unknownStateIsRejected() {
        assertFalse(AppointmentRules.validStatus("EN_ESPERA"));
    }
}
