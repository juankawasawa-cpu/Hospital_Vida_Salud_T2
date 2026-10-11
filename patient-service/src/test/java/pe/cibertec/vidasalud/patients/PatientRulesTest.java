package pe.cibertec.vidasalud.patients;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatientRulesTest {
    @Test
    void acceptsFictionalDocument() {
        assertTrue(PatientRules.validDocument("DNI-DEMO", "00000001"));
    }

    @Test
    void rejectsBlankDocumentNumber() {
        assertFalse(PatientRules.validDocument("DNI-DEMO", " "));
    }

    @Test
    void rejectsBlankName() {
        assertFalse(PatientRules.validName(" "));
    }

    @Test
    void acceptsOnlyDefinedPatientStatuses() {
        assertTrue(PatientRules.validStatus("ACTIVO"));
        assertTrue(PatientRules.validStatus("inactivo"));
        assertFalse(PatientRules.validStatus("SUSPENDIDO"));
    }

    @Test
    void rejectsMissingRequiredFields() {
        PatientController.PatientRequest request = new PatientController.PatientRequest(
            "DNI-DEMO", "00000001", "Ana", "", null, null, null, null, null
        );
        assertFalse(PatientRules.validRequest(request));
    }
}
