# Matriz de endpoints por rol

Fuente: reglas de `SecurityConfig`, anotaciones `@PreAuthorize` de los controladores y rutas declaradas en los `@RequestMapping`.

- **Público**: no requiere token JWT.
- **Autenticado**: requiere un JWT válido, sin rol específico.
- **ADMIN**: requiere autoridad `ROLE_ADMIN`.
- **ORGANIZER**: requiere autoridad `ROLE_ORGANIZER`.
- **Dueño o ADMIN**: además del rol, el servicio valida que el usuario sea el organizador propietario del evento o un administrador.

| Endpoint | Método | Rol requerido |
|---|---|---|
| `/auth/register` | POST | Público |
| `/auth/login` | POST | Público |
| `/auth/me` | GET | Autenticado |
| `/events` | GET | Público |
| `/api/categories` | GET | Autenticado |
| `/api/categories/{id}` | GET | Autenticado |
| `/api/categories` | POST | ADMIN |
| `/api/categories/{id}` | PUT | ADMIN |
| `/api/categories/{id}/status` | PATCH | ADMIN |
| `/api/events` | GET | Autenticado |
| `/api/events/organizer` | GET | Autenticado (filtra por el organizador autenticado) |
| `/api/events/{id}` | GET | Autenticado (no publicado: dueño o ADMIN) |
| `/api/events` | POST | ORGANIZER o ADMIN |
| `/api/events/{id}` | PUT | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/events/{id}/status` | PATCH | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/events/{id}/active` | PATCH | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/events/{eventId}/ticket-types` | GET | Autenticado |
| `/api/events/{eventId}/ticket-types/page` | GET | Autenticado |
| `/api/events/{eventId}/ticket-types` | POST | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/ticket-types/{id}` | GET | Autenticado |
| `/api/ticket-types/{id}` | PUT | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/ticket-types/{id}/status` | PATCH | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/ticket-types/{id}` | DELETE | ORGANIZER o ADMIN (dueño o ADMIN) |
| `/api/admin/categories` | GET | ADMIN |
| `/api/admin/categories` | POST | ADMIN |
| `/api/admin/categories/{id}` | PUT | ADMIN |
| `/api/admin/categories/{id}/status` | PATCH | ADMIN |
| `/api/admin/users` | GET | ADMIN |
| `/api/admin/users` | POST | ADMIN |
| `/api/admin/users/{id}` | PUT | ADMIN |
| `/api/admin/users/{id}/status` | PATCH | ADMIN |
| `/api/admin/monitoring` | GET | ADMIN |
| `/api/admin/monitoring/kpis` | GET | ADMIN |
| `/api/admin/monitoring/revenue-timeline` | GET | ADMIN |
| `/api/admin/monitoring/ticket-flow` | GET | ADMIN |
| `/api/admin/monitoring/active-events` | GET | ADMIN |
| `/api/organizer/dashboard` | GET | ORGANIZER |

## Notas

- `GET /events` es el catálogo público. `GET /api/events` sigue siendo autenticado, igual que antes de la matriz de roles.
- Las escrituras de eventos y tipos de entrada aplican defensa en profundidad: primero la regla de rol y después la validación de ownership del servicio.
- Los rechazos de autenticación y autorización se devuelven como JSON (`401` y `403`) mediante `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`.
- `/h2-console/**` es público únicamente con el perfil `dev`; no forma parte de la API de negocio.
