# Hospital Vida Salud — T2 Microservicios

Solución académica para registrar pacientes, programar citas y difundir eventos de estado mediante una arquitectura distribuida. Todos los datos deben ser ficticios. No ofrece diagnóstico ni recomendaciones clínicas.

## Arquitectura implementada
- **api-gateway** (8080): Spring Cloud Gateway, rutas `lb://` resueltas mediante Eureka.
- **discovery-server** (8761): Eureka Discovery Server.
- **patient-service** (8081): propietario de pacientes; WebFlux + `Mono`/`Flux` + Spring Data R2DBC/PostgreSQL.
- **appointment-service** (8082): propietario de citas; valida paciente mediante HTTP reactivo, evita solapar horarios activos de un paciente o profesional y publica eventos de estado en RabbitMQ.
- **notification-service** (8083): consumidor de eventos de citas; registra la recepción en logs (simulación académica). Declara su propia cola para no depender del orden de arranque.
- **audit-service** (8084): consumidor de eventos para trazabilidad en logs (simulación académica). Declara su propia cola para no depender del orden de arranque.
- **payment-service** (8085): registra pagos simulados, valida que el monto sea positivo, consulta al servicio de pacientes y evita números de operación duplicados.
- PostgreSQL (5432) y RabbitMQ (5672; consola 15672) se ejecutan con Docker Compose.

> Alcance actual: el flujo principal pacientes → citas → evento RabbitMQ está implementado y también se pueden registrar pagos simulados. Los servicios de notificación/auditoría son consumidores demostrativos por logs; no persisten sus eventos. Atención, laboratorio, hospitalización y farmacia siguen pendientes. Antes de entregar, ejecutar la verificación local y capturar evidencias reales.

## Requisitos
- JDK 21
- Maven 3.9+
- Docker Desktop con Compose
- Git

## Ejecutar
1. Ejecutar `Copy-Item .env.example .env`. Editar `.env` y reemplazar `CAMBIAR_PASSWORD_POSTGRES` y `CAMBIAR_PASSWORD_RABBITMQ` por contraseñas locales diferentes. No subir `.env` a Git.
2. Iniciar infraestructura: `docker compose up -d`.
3. En cada terminal de PowerShell que vaya a ejecutar un microservicio, ejecutar `. .\scripts\load-env.ps1`. Abrir otra terminal en la raíz e iniciar Eureka con `mvn -pl discovery-server spring-boot:run`.
4. Abrir terminales separadas para iniciar los servicios con estos comandos, uno en cada terminal: `mvn -pl patient-service spring-boot:run`, `mvn -pl appointment-service spring-boot:run`, `mvn -pl notification-service spring-boot:run`, `mvn -pl audit-service spring-boot:run` y `mvn -pl payment-service spring-boot:run`.
5. Abrir otra terminal, cargar las variables con `. .\scripts\load-env.ps1` e iniciar el Gateway con `mvn -pl api-gateway spring-boot:run`.
6. Eureka: http://localhost:8761. RabbitMQ UI: http://localhost:15672. El usuario y la contraseña son los configurados en tu `.env` local.

La base de datos se inicializa desde `infra/postgres/init.sql` al crear un volumen nuevo. Si ya ejecutaste la versión anterior y necesitas aplicar el esquema actualizado, `docker compose down -v` elimina también los datos locales; luego ejecuta `docker compose up -d` para crear la base limpia. Las contraseñas no tienen valores por defecto en el repositorio: se cargan desde el archivo `.env` local mediante `scripts/load-env.ps1`. No subir `.env` ni secretos.

## Endpoints (por Gateway)
- `GET /api/pacientes`
- `GET /api/pacientes/{uuid}`
- `POST /api/pacientes` — JSON: `{"tipoDocumento":"DNI-DEMO","numeroDocumento":"00000001","nombres":"Ana","apellidos":"Prueba","fechaNacimiento":"2000-01-01","telefono":"900000001","correo":"ana@example.test","direccion":"Dirección ficticia"}`
- `PUT /api/pacientes/{uuid}` — actualiza datos del paciente.
- `GET /api/citas`, `GET /api/citas/{uuid}`
- `POST /api/citas` — JSON: `{"pacienteId":"UUID_DEL_PACIENTE","especialidad":"Medicina general (demo)","fechaHora":"2027-01-20T10:00:00-05:00","profesionalCodigo":"AGENDA-DEMO-01","motivoAdministrativo":"Control administrativo ficticio"}`
- `PATCH /api/citas/{uuid}/estado` — JSON `{"estado":"CONFIRMADA"}`. Transiciones implementadas: PENDIENTE→CONFIRMADA/CANCELADA/AUSENTE; CONFIRMADA→ATENDIDA/CANCELADA/AUSENTE.
- `POST /api/pagos` — JSON: `{"pacienteId":"UUID_DEL_PACIENTE","concepto":"Servicio administrativo demo","monto":25.50,"medioPago":"EFECTIVO","numeroOperacion":"OP-DEMO-001"}`. El monto debe ser mayor que cero y el número de operación no debe repetirse.

Ejemplo PowerShell para paciente:
```powershell
Invoke-RestMethod -Method Post -Uri http://localhost:8080/api/pacientes -ContentType 'application/json' -Body '{"tipoDocumento":"DNI-DEMO","numeroDocumento":"00000001","nombres":"Ana","apellidos":"Prueba","fechaNacimiento":"2000-01-01","telefono":"900000001","correo":"ana@example.test"}'
```

La matriz de requisitos y limitaciones está en `docs/matriz-rubrica.md`.

## Pruebas y CI
- Pruebas unitarias de campos obligatorios, estados de pacientes, transiciones de citas y rechazo de pagos con monto cero o negativo.
- Ejecutar `mvn --batch-mode verify` en la raíz.
- `.github/workflows/ci.yml` ejecuta Maven verify en cada push y pull request a `main`.
- Para evidenciar fallo controlado: en una rama temporal, cambia temporalmente una aserción esperada en `AppointmentRulesTest`, sube la rama y muestra el job rojo; luego revierte el cambio, sube la corrección y muestra el job verde. No dejes la prueba defectuosa en `main`.

## Contratos y decisiones
- La cita almacena `pacienteId` como identificador externo, sin FK entre dominios. `appointment-service` consulta `patient-service` antes de crearla. Si el servicio no responde, se rechaza la operación de forma conservadora; el cliente debe reintentar tras verificar disponibilidad.
- Exchange: `vidasalud.eventos` (topic). Routing key: `cita.<estado-en-minúscula>`. Colas: `notificaciones.citas` y `auditoria.eventos`. El productor publica cambios de estado; los consumidores procesan sin acoplamiento directo.
- `Mono` para un recurso/resultado y `Flux` para listados; R2DBC evita JDBC bloqueante en el flujo HTTP. La llamada de RabbitTemplate se desplaza a `boundedElastic` para no bloquear el event loop de WebFlux. El evento se publica después de guardar el cambio; para mayor garantía de entrega se recomienda un patrón outbox transaccional con reintentos e idempotencia.
- SQL compartido por simplicidad del laboratorio: los servicios acceden a sus tablas lógicas (`pacientes`, `citas` y `pagos`) y no modifican las tablas de otros dominios. En producción se recomienda base/esquema aislado por servicio.

## Alcance pendiente

Esta entrega implementa el flujo de pacientes y citas con validación síncrona y eventos asíncronos, además del registro de pagos simulados. No implementa todavía endpoints funcionales para atención, laboratorio, hospitalización ni farmacia. Si la evaluación exige demostrar todos esos procesos, deben agregarse como microservicios independientes y acompañarse de sus pruebas y evidencias. La auditoría y las notificaciones se simulan mediante logs, no mediante tablas persistidas.

## Evidencias a capturar
1. Panel Eureka con servicios registrados.
2. `GET` y `POST` de pacientes a través del Gateway.
3. Rechazo HTTP 409 al registrar documento duplicado.
4. Creación de cita con paciente existente y rechazo cuando el paciente no existe.
5. Cambio de estado válido y transición inválida HTTP 409.
6. RabbitMQ Management con colas; logs de notification-service y audit-service al confirmar una cita.
7. `mvn verify` exitoso.
8. GitHub Actions: ejecución verde y fallo controlado corregido.
9. Pago de prueba válido y rechazo HTTP 400 al enviar un monto cero o negativo.
10. Diagrama de arquitectura incluido en `docs/arquitectura.mmd`.

## Seguridad y límites
La configuración no incluye contraseñas predeterminadas. Usa secretos de GitHub y variables de entorno para cualquier entorno remoto. No registrar datos sensibles. Los identificadores y documentos de los ejemplos son ficticios.
