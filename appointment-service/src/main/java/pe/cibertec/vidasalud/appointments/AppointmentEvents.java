package pe.cibertec.vidasalud.appointments;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.UUID;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Configuration
class RabbitConfig {
    static final String EXCHANGE = "vidasalud.eventos";

    @Bean
    TopicExchange hospitalExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable("notificaciones.citas").build();
    }

    @Bean
    Queue auditQueue() {
        return QueueBuilder.durable("auditoria.eventos").build();
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(notificationQueue).to(hospitalExchange).with("cita.#");
    }

    @Bean
    Binding auditBinding(Queue auditQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(auditQueue).to(hospitalExchange).with("#");
    }
}

@Component
class AppointmentEvents {
    private final RabbitTemplate template;
    private final ObjectMapper mapper;

    AppointmentEvents(RabbitTemplate template, ObjectMapper mapper) {
        this.template = template;
        this.mapper = mapper;
    }

    Mono<Void> publish(AppointmentEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String routingKey = "cita." + event.estado().toLowerCase(Locale.ROOT);
                template.convertAndSend(RabbitConfig.EXCHANGE, routingKey, mapper.writeValueAsString(event));
            } catch (JsonProcessingException error) {
                throw new IllegalStateException("No se pudo serializar el evento", error);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public record AppointmentEvent(
        UUID recursoId,
        UUID pacienteId,
        String evento,
        String estadoAnterior,
        String estado,
        OffsetDateTime fecha,
        String origen
    ) {
    }
}
