# Lista de evidencias de evaluación

- [ ] Diagrama de arquitectura (exportar `arquitectura.mmd` a PNG/SVG desde Mermaid).
- [ ] Eureka con patient-service, appointment-service, notification-service, audit-service y api-gateway registrados.
- [ ] POST paciente ficticio con HTTP 201.
- [ ] Segundo POST con mismo tipo/número y HTTP 409.
- [ ] GET paciente por UUID.
- [ ] POST cita con paciente existente y HTTP 201.
- [ ] POST cita con UUID inexistente y rechazo.
- [ ] PATCH estado PENDIENTE → CONFIRMADA.
- [ ] Transición inválida con HTTP 409.
- [ ] Colas/exchange en RabbitMQ Management y logs de ambos consumidores después de iniciar los servicios en distinto orden.
- [ ] `mvn --batch-mode verify` local verde.
- [ ] GitHub Actions verde tras push.
- [ ] Fallo de prueba controlado en rama, captura del job rojo y captura de la corrección verde.
- [ ] README, documento técnico y repositorio sin secretos.
