# Documento técnico — Hospital Vida Salud

## 1. Problema y alcance
El monolito acopla procesos administrativos y dificulta despliegues y escalado selectivo. Esta solución separa el registro de pacientes y la programación de citas, y usa eventos para notificación/auditoría. Es una demo académica y no gestiona atención clínica real.

## 2. Actores y reglas
- Admisión: registra pacientes y citas.
- Personal de salud (simulado): consulta información administrativa.
- Administrador: revisa Eureka, logs y mensajería.
- Desarrollador: mantiene código, pruebas y CI.
- Documento único por tipo+número; campos esenciales obligatorios.
- La cita requiere paciente existente, fecha futura, especialidad y profesional.
- No se permite que el paciente ni el profesional tengan otra cita activa en el mismo horario.
- Transiciones de cita controladas; una cita cancelada no se confirma.

## 3. Límites de microservicio
| Servicio | Propiedad de datos | Operaciones | Comunicación |
|---|---|---|---|
| patient-service | `pacientes` | CRUD y detección de documento duplicado | REST reactivo; R2DBC |
| appointment-service | `citas` | crear/consultar cita, cambiar estado | REST a pacientes + RabbitMQ |
| payment-service | `pagos` | registrar y consultar pagos ficticios; validar monto y operación única | REST a pacientes + R2DBC |
| notification-service | No persiste en esta demo | consume eventos y escribe log de aviso | RabbitMQ |
| audit-service | No persiste en esta demo | consume eventos y escribe log de trazabilidad | RabbitMQ |
| api-gateway | Ninguna | entrada y enrutamiento | Eureka/LoadBalancer |
| discovery-server | Registro de instancias | descubre servicios | Eureka |

## 4. Contratos principales
- `POST /api/pacientes`: 201, 400 si faltan campos, 409 si documento duplicado.
- `GET /api/pacientes/{id}`: 200 o 404.
- `POST /api/citas`: 201, 400 si datos/fecha inválidos o paciente no disponible, 409 si agenda duplicada.
- `PATCH /api/citas/{id}/estado`: 200 o 404; 409 si transición inválida.
- `POST /api/pagos`: 201; 400 si monto o campos son inválidos, 404 si el paciente no existe, 409 si se repite el número de operación.
- `CitaConfirmada` y `CitaEstadoActualizado`: recursoId, pacienteId, evento, estadoAnterior, estado, fecha y origen.

## 5. Comunicación
**Síncrona:** citas consulta `GET /api/pacientes/{id}` antes de guardar. Es necesaria para validar existencia inmediata. En esta demo se falla de forma conservadora si el servicio está caído; no se implementa circuit breaker.

**Asíncrona:** exchange topic `vidasalud.eventos`; routing key `cita.<estado>`; colas `notificaciones.citas` y `auditoria.eventos`. Los consumidores no necesitan conocer el endpoint ni el código interno del productor. Cada consumidor declara su cola y su binding, así el sistema no exige que el productor se haya iniciado antes. Para robustez adicional: DLQ, reintentos, idempotencia y outbox transaccional.

## 6. Persistencia y programación reactiva
PostgreSQL contiene las tablas `pacientes`, `citas` y `pagos`. Los consumidores actuales de notificaciones y auditoría registran eventos en logs y todavía no persisten esos registros en tablas. La demo usa R2DBC en servicios de dominio; `Mono` representa cero/uno y `Flux` listas. No se usan relaciones FK entre dominios: `paciente_id` en citas es una referencia lógica validada mediante contrato HTTP.

## 7. Gateway y Eureka
Los clientes solo consumen el puerto 8080. Gateway resuelve `lb://patient-service`, `lb://appointment-service` y `lb://payment-service` a través de Eureka/LoadBalancer; así no dependen de puertos internos ni de una instancia concreta.

## 8. CI
GitHub Actions ejecuta `mvn --batch-mode --no-transfer-progress verify` en push y pull request. Si compila o falla una prueba, el job termina rojo y no debe integrarse el cambio hasta corregirlo. La entrega continua/despliegue no está incluida.

## 9. Extensiones pendientes

La propuesta para ampliar el alcance mantiene un propietario de datos por dominio y evita compartir tablas internas:

| Servicio propuesto | Datos propios | Operaciones previstas | Integración |
|---|---|---|---|
| care-service | `atenciones` | registrar y consultar una atención administrativa | consulta la cita por HTTP antes de crearla; publica `AtencionRegistrada` |
| laboratory-service | `solicitudes_laboratorio` | crear solicitudes, cambiar estado y guardar un resultado ficticio | valida referencias por identificadores; publica `SolicitudLaboratorioActualizada` |
| hospitalization-service | `hospitalizaciones` | ingreso, asignación de cama y alta administrativa | valida paciente; impide asignar una cama con ingreso activo duplicado |
| pharmacy-service | `medicamentos`, `dispensaciones` | administrar catálogo de prueba y registrar dispensaciones | controla stock de demostración y emite evento al dispensar |

Atención, laboratorio, hospitalización y farmacia están modelados como extensiones recomendadas, no como servicios implementados en esta versión. El registro de pagos sí dispone de un servicio funcional con validación de monto positivo y operación única. Antes de declarar la evaluación completa, se deben desarrollar y probar si el docente exige todos los procesos. También se recomienda añadir persistencia real para auditoría y notificaciones, pruebas de integración con PostgreSQL/RabbitMQ, seguridad del Gateway, documentación OpenAPI y tolerancia a fallos.
