package pe.cibertec.vidasalud.audit;

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
    Queue auditQueue() {
        return QueueBuilder.durable("auditoria.eventos").build();
    }

    @Bean
    Binding auditBinding(Queue auditQueue, TopicExchange hospitalEventExchange) {
        return BindingBuilder.bind(auditQueue).to(hospitalEventExchange).with("#");
    }
}
