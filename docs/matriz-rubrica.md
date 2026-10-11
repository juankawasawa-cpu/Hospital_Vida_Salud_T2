# Matriz de revisión de la evaluación T2

| Requisito | Estado en esta versión | Evidencia dentro del proyecto |
|---|---|---|
| Análisis del problema y alcance | Parcial | `diseno-tecnico.md` describe actores, reglas, límites y procesos seleccionados. |
| Registro, consulta y actualización de pacientes | Implementado | `patient-service` y `PatientControllerTest`. |
| Rechazo de documento duplicado | Implementado | Restricción única SQL y prueba de controlador. |
| Registro y consulta de citas | Implementado | `appointment-service` y `AppointmentControllerTest`. |
| Estados y transiciones de citas | Implementado | `AppointmentRules` y pruebas unitarias/de controlador. |
| Validación síncrona del paciente | Implementado | `PatientClient` utiliza `WebClient` con balanceo vía Eureka. |
| Pago ficticio y monto válido | Implementado | `payment-service`, restricción SQL y pruebas para montos inválidos. |
| Spring Cloud Gateway | Implementado | Rutas para pacientes, citas y pagos. |
| Eureka Discovery | Implementado | Servidor y clientes Eureka en los servicios. Requiere evidencia visual al ejecutarlo. |
| RabbitMQ productor y consumidores | Implementado | Exchange, routing key, colas y prueba del publicador. Consumidores registran eventos en logs. |
| PostgreSQL y Spring Data R2DBC | Implementado en servicios de dominio | Entidades y repositorios reactivos. La prueba del entorno real debe ejecutarse localmente. |
| `Mono`, `Flux` y WebFlux | Implementado | Controladores y repositorios reactivos de pacientes, citas y pagos. |
| Pruebas automatizadas | Parcial | Pruebas de reglas, controladores y publicador; todavía no hay prueba integral contra PostgreSQL y RabbitMQ reales. |
| GitHub Actions | Configurado | `.github/workflows/ci.yml`; la ejecución verde y el fallo controlado deben obtenerse desde el repositorio remoto. |
| Atención, laboratorio, hospitalización y farmacia | Pendiente | Se documentan como extensiones, pero esta versión no contiene sus endpoints. |
| Capturas de evidencias | Pendiente | Deben tomarse con los servicios ejecutándose y con GitHub Actions real. |

## Alcance seleccionado

La implementación prioriza un flujo comprobable: registrar un paciente, validar su existencia al programar una cita, cambiar el estado de la cita y publicar un evento para los consumidores. El registro de pagos se incluye como proceso administrativo adicional para cubrir su validación obligatoria. Los servicios de notificación y auditoría reciben eventos sin dependencia directa del productor.

Los dominios de atención, laboratorio, hospitalización y farmacia deben agregarse como servicios separados si se requiere demostrar esos procesos en esta entrega. Esta matriz evita presentar como implementadas funciones que aún no existen en el código.
