package pe.cibertec.vidasalud.notifications;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RabbitTopology {
    @Bean
    TopicExchange hospitalEventExchange() {
        return new TopicExchange("vidasalud.eventos", true, false);
    }

    @Bean
    Queue notificationQueue() {
        return QueueBuilder.durable("notificaciones.citas").build();
    }

    @Bean
    Binding notificationBinding(Queue notificationQueue, TopicExchange hospitalEventExchange) {
        return BindingBuilder.bind(notificationQueue).to(hospitalEventExchange).with("cita.#");
    }
}
