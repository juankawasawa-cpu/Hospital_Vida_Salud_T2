package pe.cibertec.vidasalud.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class EventConsumer {
    private static final Logger log = LoggerFactory.getLogger(EventConsumer.class);

    @RabbitListener(queues = "auditoria.eventos")
    public void receive(String event) {
        log.info("Evento recibido por audit-service: {}", event);
    }
}
