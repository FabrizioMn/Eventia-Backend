# Eventia — Backend

> API REST para la gestión y venta de entradas para eventos. Backend desarrollado con Java, Spring Boot y Spring Security, integrado con el frontend de Eventia.

![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.2-6DB33F?logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-7.1.2-6DB33F?logo=springsecurity&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?logo=apachemaven&logoColor=white)
![H2](https://img.shields.io/badge/H2-Database-1E4D2B)
## 📌 Sobre el proyecto

**Eventia S.A.C.** es una plataforma web orientada a la gestión y venta de entradas para eventos.

Este repositorio contiene el **backend** del proyecto académico, desarrollado con **Spring Boot** y expuesto mediante una API REST para ser consumida por el frontend de Eventia.

El backend centraliza la lógica de negocio, persistencia de datos, autenticación, autorización y exposición de los servicios utilizados por la aplicación.

## ✨ Características

- 🔐 Registro e inicio de sesión de usuarios
- 🪪 Autenticación mediante JWT
- 👤 Consulta del usuario autenticado
- 👥 Gestión de usuarios y roles
- 🎪 Gestión y consulta de eventos
- 🏷️ Gestión de categorías
- 🎟️ Gestión de tipos de entrada
- 📊 Consulta de información para dashboards
- 🛡️ Protección de endpoints mediante Spring Security
- 🌐 Configuración CORS para comunicación con el frontend
- 🗄️ Persistencia de información mediante JPA/Hibernate

## 🛠️ Tecnologías

| **Tecnología** | **Uso** |
| -------------- | ------- |
| Java | Lenguaje principal |
| Spring Boot | Framework principal |
| Spring Web | Desarrollo de API REST |
| Spring Security | Autenticación y autorización |
| JWT | Autenticación mediante tokens |
| Spring Data JPA | Persistencia y acceso a datos |
| Hibernate | ORM |
| H2 | Base de datos |
| Maven | Gestión de dependencias y build |
| Lombok | Reducción de código repetitivo |

## 🏗️ Arquitectura

El proyecto utiliza una arquitectura organizada por capas, separando las responsabilidades principales de la aplicación.

```text
src/
└── main/
    ├── java/
    │   └── .../
    │       ├── controller/       # Endpoints REST
    │       ├── service/          # Lógica de negocio
    │       ├── repository/       # Acceso a datos
    │       ├── entity/           # Entidades de persistencia
    │       ├── dto/              # Objetos de transferencia de datos
    │       ├── security/         # Configuración de seguridad y JWT
    │       └── config/            # Configuración de la aplicación
    │
    └── resources/
        └── application.yaml      # Configuración
```

Esta organización permite separar la exposición de los endpoints, la lógica de negocio, el acceso a datos y los mecanismos de seguridad, facilitando el mantenimiento y desarrollo colaborativo.

## 🔐 Autenticación y seguridad

El backend utiliza **Spring Security** junto con **JWT (JSON Web Token)** para autenticar a los usuarios.

El flujo de autenticación principal es:

1. El usuario envía sus credenciales mediante `/auth/login`.
2. El backend valida las credenciales.
3. Si son correctas, genera un JWT.
4. El frontend almacena el token recibido.
5. Las siguientes peticiones autenticadas envían el token mediante `Authorization: Bearer <JWT>`.
6. El filtro JWT valida el token e identifica al usuario autenticado.
7. Spring Security establece la identidad del usuario en el `SecurityContext`.
8. Los endpoints protegidos pueden utilizar la identidad y rol del usuario autenticado.

El endpoint `/auth/me` permite obtener el perfil del usuario correspondiente al token autenticado.

Los roles disponibles en el sistema son:

- `USER`
- `ORGANIZER`
- `ADMIN`

## 🔌 API REST

El backend expone servicios REST para ser consumidos por el frontend de Eventia.

### Autenticación

| **Endpoint** | **Método** | **Descripción** |
| ------------ | ---------- | --------------- |
| `/auth/login` | POST | Autenticar usuario y generar JWT |
| `/auth/register` | POST | Registrar un nuevo usuario |
| `/auth/me` | GET | Obtener información del usuario autenticado |

### Eventos

| **Endpoint** | **Método** | **Descripción** |
| ------------ | ---------- | --------------- |
| `/events` | GET | Obtener catálogo de eventos activos |
| `/api/events` | GET | Consultar eventos |
| `/api/events/{id}` | GET | Consultar detalle de un evento |
| `/api/events/organizer` | GET | Consultar eventos del organizador |

La API continúa ampliándose conforme se implementan las funcionalidades de la plataforma.

## 🔧 Variables de entorno

| Variable | Descripción | Requerida |
| --- | --- | --- |
| `JWT_SECRET` | Secreto Base64 (mínimo 32 bytes) para firmar JWT. | Sí en `prod` (sin valor por defecto en `application-prod.yaml`); en `dev` usa valor por defecto solo para desarrollo. |
| `JWT_EXPIRATION_MS` | Expiración del token en milisegundos. | No (default `86400000`). |

## 🌐 Comunicación con el frontend

El frontend de Eventia consume esta API mediante **Axios**.

Durante el desarrollo local, ambos proyectos se ejecutan de manera independiente:

```text
┌─────────────────────────┐
│   Eventia Frontend      │
│   React + TypeScript    │
│   localhost:5173        │
└────────────┬────────────┘
             │
             │ HTTP / REST
             │ JWT
             ▼
┌─────────────────────────┐
│   Eventia Backend       │
│   Spring Boot           │
│   localhost:8080        │
└────────────┬────────────┘
             │
             │ JPA / Hibernate
             ▼
┌─────────────────────────┐
│      H2 Database        │
└─────────────────────────┘
```

La comunicación entre frontend y backend se encuentra habilitada mediante **CORS**.

## 🚀 Instalación

### Requisitos

- Java 21
- Maven
- Git

### Clonar el repositorio

```bash
git clone https://github.com/danielaoxeda/Eventia-Backend.git
cd Eventia-Backend
```

### Ejecutar el proyecto

En Windows:

```bash
.\mvnw.cmd spring-boot:run
```

En Linux/macOS:

```bash
./mvnw spring-boot:run
```

La API estará disponible normalmente en:

```text
http://localhost:8080
```

### Generar build

En Windows:

```bash
.\mvnw.cmd clean package
```

En Linux/macOS:

```bash
./mvnw clean package
```

## Variables de entorno

La configuración sensible se externaliza mediante variables de entorno. En desarrollo existen valores por defecto, pero en producción deben definirse explícitamente.

| **Variable** | **Descripción** | **Valor por defecto** |
| ------------ | --------------- | --------------------- |
| `JWT_SECRET` | Clave usada para firmar los JWT. Debe estar codificada en Base64 y representar al menos 32 bytes. La aplicación falla al iniciar si no cumple esta condición. | Valor de desarrollo incluido en `application.yaml` |
| `JWT_EXPIRATION_MS` | Tiempo de expiración del token en milisegundos. | `86400000` (24 horas) |

Ejemplo en Windows PowerShell:

```powershell
$env:JWT_SECRET = "<clave-base64-de-al-menos-32-bytes>"
$env:JWT_EXPIRATION_MS = "86400000"
.\mvnw.cmd spring-boot:run
```

Ejemplo en Linux/macOS:

```bash
export JWT_SECRET="<clave-base64-de-al-menos-32-bytes>"
export JWT_EXPIRATION_MS="86400000"
./mvnw spring-boot:run
```

Para generar una clave de desarrollo compatible:

```bash
openssl rand -base64 32
```

## 🗄️ Base de datos

El proyecto utiliza **H2** durante el desarrollo.

La configuración permite trabajar con una base de datos local almacenada en el directorio `data/`.

Los datos iniciales del sistema son cargados automáticamente al iniciar la aplicación.

## 🧪 Pruebas

Para ejecutar las pruebas:

```bash
.\mvnw.cmd test
```

Las pruebas verifican el comportamiento de los componentes implementados en el backend.

## 📌 Estado del proyecto

**En desarrollo 🚧**

El backend se encuentra integrado con el frontend de Eventia mediante una API REST. Actualmente se encuentran implementados los principales flujos de autenticación, seguridad, usuarios y eventos, mientras continúa el desarrollo e integración de las demás funcionalidades de la plataforma.
