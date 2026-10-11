package pe.cibertec.vidasalud.payments;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

@Configuration
class PaymentClientConfiguration {
    @Bean
    @LoadBalanced
    WebClient.Builder paymentLoadBalancedWebClient() {
        return WebClient.builder();
    }
}

@Component
class PaymentPatientClient {
    private final WebClient client;

    PaymentPatientClient(
        WebClient.Builder builder,
        @Value("${services.patient-url:http://patient-service}") String baseUrl
    ) {
        client = builder.baseUrl(baseUrl).build();
    }

    Mono<Boolean> exists(UUID id) {
        return client.get()
            .uri("/api/pacientes/{id}", id)
            .retrieve()
            .toBodilessEntity()
            .map(response -> true)
            .onErrorResume(WebClientResponseException.NotFound.class, error -> Mono.just(false))
            .onErrorMap(WebClientRequestException.class, error -> new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "El servicio de pacientes no está disponible", error))
            .onErrorMap(WebClientResponseException.class, error -> new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE, "No se pudo validar el paciente", error));
    }
}
