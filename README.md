# MCP-Server

Proyecto MCP-Server

Descripción
- MCP-Server es una pequeña API REST basada en Spring Boot que modela la lógica básica de gestión de hoteles, habitaciones, beneficios, atracciones, direcciones, clientes (personas) y reservas.
- El proyecto incluye entidades JPA, DTOs para transferencia de datos, repositorios basados en Spring Data JPA, servicios con la lógica de negocio y especificaciones (Specification) para consultas dinámicas.

Estructura principal
- `src/main/java/ar/mcp/server/config` : Configuración de seguridad y beans (ej. `SecurityConfig`).
- `src/main/java/ar/mcp/server/domain` : Entidades JPA (`entities`) y objetos de transferencia (`dto`).
- `src/main/java/ar/mcp/server/repositories` : Interfaces de repositorio que extienden `JpaRepository` y `JpaSpecificationExecutor`.
- `src/main/java/ar/mcp/server/services` : Lógica de negocio por dominio (hoteles, habitaciones, reservas, beneficios, atracciones, direcciones, personas, estados, etc.).
- `src/main/resources/db/migration` : Migraciones de base de datos (`V1__Initial_Schema.sql`, `V2__Insert_Test_Data.sql`) para Flyway.

Requisitos
- Java 17+
- Maven (el repositorio incluye `./mvnw` wrapper)
- Base de datos PostgreSQL (o adaptar `application.yml`) — las migraciones están preparadas en `src/main/resources/db/migration`.

Cómo ejecutar (desarrollo)
1. Configurar las credenciales y conexión a la base de datos en `src/main/resources/application.yml`.
2. Ejecutar migraciones (automáticas con Flyway al iniciar la app) o manualmente ejecutar los scripts en `src/main/resources/db/migration`.
3. Construir y ejecutar la aplicación con Maven (wrapper incluido):

```bash
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

o ejecutar el jar:

```bash
java -jar target/*.jar
```

Endpoints y uso
- La aplicación expone controladores REST bajo rutas típicas, agrupadas por dominio (por ejemplo `/api/hotels`, `/api/rooms`, `/api/reservations`, etc.).
- Muchos servicios usan métodos `@McpTool` y `ToolParam` para documentar parámetros; revisar las clases de servicio en `src/main/java/ar/mcp/server/services` para conocer la forma de los DTOs y parámetros esperados.

Documentación y código
- He añadido Javadoc consistente a las clases en `config`, `domain`, `repositories` y `services` para facilitar el mantenimiento y la integración de nuevas funcionalidades.
- Para generar documentación Javadoc (opcional):

```bash
./mvnw javadoc:javadoc
```

Pruebas
- Para ejecutar pruebas unitarias (si existen):

```bash
./mvnw test
```

Notas y consideraciones
- Seguridad: `SecurityConfig` configura CORS y establece la política de sesiones como stateless. Actualmente CSRF está deshabilitado y la autenticación JWT está marcada como TODO. Revisar antes de exponer en producción.
- Consultas dinámicas: se usan `Specification` (Spring Data JPA) para consultas flexibles; revisar `services/*Specifications.java` para ver los predicados disponibles.
- Migraciones: las versiones iniciales de esquema y datos de prueba están en `src/main/resources/db/migration`.

Contribuir
- Crear una rama por feature: `git checkout -b feat/descripcion-corta`.
- Hacer cambios, pruebas y abrir un Pull Request hacia `main`.

Contacto
- Autor/Repo: LautaroMartVillalba / MCP-Server

Licencia
- Añadir aquí la licencia del proyecto (por ejemplo MIT) si corresponde.
