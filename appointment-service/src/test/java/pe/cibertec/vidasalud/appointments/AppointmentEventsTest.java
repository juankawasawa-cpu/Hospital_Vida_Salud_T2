package pe.cibertec.vidasalud.appointments;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AppointmentEventsTest {
    @Test
    void publishesConfirmationToRabbitExchange() {
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        AppointmentEvents publisher = new AppointmentEvents(rabbitTemplate, new ObjectMapper().findAndRegisterModules());
        AppointmentEvents.AppointmentEvent event = new AppointmentEvents.AppointmentEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "CitaConfirmada",
            "PENDIENTE",
            "CONFIRMADA",
            OffsetDateTime.parse("2027-01-20T10:00:00-05:00"),
            "appointment-service"
        );

        publisher.publish(event).block();

        verify(rabbitTemplate).convertAndSend(eq("vidasalud.eventos"), eq("cita.confirmada"), anyString());
    }
}
