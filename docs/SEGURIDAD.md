# Matriz de endpoints por rol

Fuente: reglas de `SecurityConfig`, anotaciones `@PreAuthorize` y rutas declaradas en los controladores.

- **Público**: no requiere token JWT.
- **Autenticado**: requiere un JWT válido, sin rol específico.
- **ADMIN / ORGANIZER**: requiere la autoridad correspondiente.
- **Dueño o ADMIN**: además del rol, el servicio valida que el usuario sea el organizador propietario del evento.

| Endpoint | Método | Rol requerido |
|---|---|---|
| `/auth/register` | POST | Público |
| `/auth/login` | POST | Público |
| `/auth/me` | GET, PATCH | Autenticado |
| `/api/events` | GET | Público |
| `/api/events/{id}` | GET | Público (no publicado: dueño o ADMIN) |
| `/api/events/{eventId}/ticket-types` | GET | Público |
| `/api/ticket-types/{id}` | GET | Público (inactivo: dueño o ADMIN) |
| `/api/categories` | GET | Público |
| `/api/categories/{id}` | GET | Público |
| `/api/categories/admin` | GET | ADMIN |
| `/api/categories` | POST | ADMIN |
| `/api/categories/{id}` | PUT | ADMIN |
| `/api/categories/{id}/active` | PATCH | ADMIN |
| `/api/events/organizer` | GET | ORGANIZER o ADMIN |
| `/api/events` | POST | ORGANIZER o ADMIN |
| `/api/events/{id}` | PUT | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/events/{id}/status`, `/api/events/{id}/active` | PATCH | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/events/{eventId}/ticket-types` | POST | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/ticket-types/{id}` | PUT, DELETE | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/ticket-types/{id}/status` | PATCH | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/orders` | POST | Autenticado |
| `/api/tickets/my` | GET | Autenticado |
| `/api/admin/events` | GET | ADMIN |
| `/api/admin/users` | GET, POST | ADMIN |
| `/api/admin/users/{id}` | PUT | ADMIN |
| `/api/admin/users/{id}/status` | PATCH | ADMIN |
| `/api/admin/monitoring` y `/api/admin/monitoring/*` | GET | ADMIN |
| `/api/organizer/dashboard` | GET | ORGANIZER o ADMIN |
| `/api/organizer/dashboard/simulate-sale` | POST | ORGANIZER o ADMIN (solo perfil `dev`) |
| `/ws-eventia/**` | handshake | Público; el frame STOMP `CONNECT` exige JWT y el `SUBSCRIBE` valida propiedad del topic |

## Notas

- El catálogo (`GET /api/events`), el detalle público, las categorías y las tarifas activas son de lectura pública.
- Las escrituras aplican defensa en profundidad: primero la regla de rol y después la validación de propiedad en el servicio.
- Los rechazos de autenticación y autorización se devuelven como JSON (`401` y `403`) mediante `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`.
- `/h2-console/**` es público únicamente con el perfil `dev`; en `prod` está desactivado.
- `JWT_SECRET` no tiene valor por defecto fuera del perfil `dev`; debe inyectarse como variable de entorno.
