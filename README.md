# Punto de Apoyo

Backend Spring Boot con MySQL y Flyway.

## Requisitos

- Java 17+
- Maven 3.9+
- Docker y Docker Compose

## Desarrollo local

Levantar MySQL:

```bash
cp .env.example .env
# Editar .env y reemplazar los valores replace_me por secretos locales.
docker compose up -d
```

Si ya tenias una base local creada con migraciones anteriores y queres recrearla desde cero:

```bash
docker compose down -v
docker compose up -d
```

Ejecutar la aplicacion:

```bash
set -a
source .env
set +a
mvn spring-boot:run
```

La aplicacion toma la conexion desde variables de entorno:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `JWT_SECRET`

Flyway ejecuta automaticamente las migraciones ubicadas en `src/main/resources/db/migration`.

Health check:

```bash
curl http://localhost:8080/actuator/health
```

## Autenticacion

Flyway crea un usuario admin inicial inactivo como placeholder:

- Email: `admin@puntodeapoyo.local`
- Role: `ADMIN`

Para usar login en desarrollo, activar ese usuario y setearle un hash BCrypt propio en `password_hash`.

Login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@puntodeapoyo.local","password":"<password>"}'
```

Usar el `accessToken` devuelto para acceder a endpoints protegidos:

```bash
curl http://localhost:8080/api/users/me \
  -H "Authorization: Bearer <accessToken>"
```

## Administracion de usuarios internos

Solo usuarios con rol `ADMIN` pueden usar estos endpoints.

Listar usuarios:

```bash
curl http://localhost:8080/api/users \
  -H "Authorization: Bearer <accessToken>"
```

Crear usuario:

```bash
curl -X POST http://localhost:8080/api/users \
  -H "Authorization: Bearer <accessToken>" \
  -H 'Content-Type: application/json' \
  -d '{
    "firstName": "Ada",
    "lastName": "Lovelace",
    "email": "ada@example.com",
    "phone": "+54 11 5555-5555",
    "password": "<password>",
    "role": "ENGINEER",
    "status": "ACTIVE"
  }'
```

Actualizar usuario por id:

```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <accessToken>" \
  -H 'Content-Type: application/json' \
  -d '{"status":"INACTIVE"}'
```

El body es parcial: solo se actualizan los campos enviados.

```bash
curl -X PATCH http://localhost:8080/api/users/1 \
  -H "Authorization: Bearer <accessToken>" \
  -H 'Content-Type: application/json' \
  -d '{"role":"COORDINATOR","status":"ACTIVE"}'
```

Los errores de validacion devuelven `400` con detalle por campo:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "La solicitud contiene campos invalidos",
  "fieldErrors": [
    {
      "field": "password",
      "message": "La password debe tener entre 8 y 100 caracteres"
    }
  ]
}
```
