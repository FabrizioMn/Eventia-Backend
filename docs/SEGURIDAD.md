# Matriz de endpoints por rol

Fuente: reglas de `SecurityConfig`, anotaciones `@PreAuthorize` de los controladores y rutas declaradas en los `@RequestMapping`.

- **Público**: no requiere token JWT.
- **Autenticado**: requiere un JWT válido, sin rol específico.
- **ORGANIZER**: requiere autoridad `ROLE_ORGANIZER`.
- **Dueño**: además del rol, el servicio valida que el usuario sea el organizador propietario del evento.

| Endpoint | Método | Rol requerido |
|---|---|---|
| `/auth/register` | POST | Público |
| `/auth/login` | POST | Público |
| `/auth/me` | GET | Autenticado |
| `/api/categories` | GET | Público |
| `/api/categories/{id}` | GET | Público |
| `/api/events` | GET | Público |
| `/api/events/{id}` | GET | Público (no publicado: dueño) |
| `/api/events/organizer` | GET | ORGANIZER |
| `/api/events` | POST | ORGANIZER |
| `/api/events/{id}` | PUT | ORGANIZER (dueño) |
| `/api/events/{id}/status` | PATCH | ORGANIZER (dueño) |
| `/api/events/{id}/active` | PATCH | ORGANIZER (dueño) |
| `/api/events/{eventId}/ticket-types` | GET | Público |
| `/api/events/{eventId}/ticket-types` | POST | ORGANIZER (dueño) |
| `/api/ticket-types/{id}` | PUT | ORGANIZER (dueño) |
| `/api/ticket-types/{id}/status` | PATCH | ORGANIZER (dueño) |
| `/api/ticket-types/{id}` | DELETE | ORGANIZER (dueño) |
| `/api/organizer/dashboard` | GET | ORGANIZER |

## Notas

- Las categorías son un catálogo de solo lectura, sembrado por `DataInitializer`.
- Las escrituras de eventos y tipos de entrada aplican defensa en profundidad: primero la regla de rol y después la validación de ownership del servicio.
- Los rechazos de autenticación y autorización se devuelven como JSON (`401` y `403`) mediante `JwtAuthenticationEntryPoint` y `JwtAccessDeniedHandler`.
- `/h2-console/**` es público únicamente con el perfil `dev`; no forma parte de la API de negocio.
