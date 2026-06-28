# Punto de Apoyo

Backend Spring Boot con MySQL y Flyway.

## Requisitos

- Java 17+
- Maven 3.9+
- Docker y Docker Compose

## Desarrollo local

Levantar MySQL:

```bash
docker compose up -d
```

Ejecutar la aplicacion:

```bash
mvn spring-boot:run
```

La aplicacion usa por defecto:

- JDBC URL: `jdbc:mysql://localhost:3306/punto_de_apoyo`
- Usuario: `punto`
- Password: `punto`

Flyway ejecuta automaticamente las migraciones ubicadas en `src/main/resources/db/migration`.

Health check:

```bash
curl http://localhost:8080/actuator/health
```
