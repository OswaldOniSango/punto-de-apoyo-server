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

## Registro publico de viviendas afectadas

No requiere login ni JWT.

```bash
curl -X POST http://localhost:8080/api/public/inspection-cases \
  -H 'Content-Type: application/json' \
  -d '{
    "applicantName": "Maria Perez",
    "applicantPhone": "+58 412 555-1212",
    "address": "Av. Principal, Casa 12",
    "city": "Caracas",
    "stateRegion": "Distrito Capital",
    "description": "Grietas visibles en paredes y filtracion de agua en techo",
    "latitude": 10.5001234,
    "longitude": -66.9012345,
    "priority": "HIGH"
  }'
```

Respuesta esperada:

```json
{
  "id": 1,
  "trackingCode": "VZ-2026-00000001",
  "status": "PENDIENTE",
  "photos": []
}
```

Tambien se puede crear un caso con fotografias usando `multipart/form-data`.
El part `case` debe enviarse como JSON (`application/json`) y `photos` puede repetirse hasta 10 veces:

```bash
curl -X POST http://localhost:8080/api/public/inspection-cases \
  -F 'case={
    "applicantName": "Maria Perez",
    "applicantPhone": "+58 412 555-1212",
    "address": "Av. Principal, Casa 12",
    "city": "Caracas",
    "stateRegion": "Distrito Capital",
    "description": "Grietas visibles en paredes y filtracion de agua en techo",
    "latitude": 10.5001234,
    "longitude": -66.9012345,
    "priority": "HIGH"
  };type=application/json' \
  -F 'photos=@/ruta/foto-1.jpg;type=image/jpeg' \
  -F 'photos=@/ruta/foto-2.png;type=image/png'
```

Restricciones:

- Maximo 10 imagenes por caso.
- Maximo 10 MB por imagen.
- Solo se permiten archivos con `content_type` de imagen, por ejemplo `image/jpeg` o `image/png`.
- En desarrollo los archivos se guardan en `APP_UPLOAD_DIR`, por defecto `uploads/`.
- La API devuelve `photos[].fileUrl`, por ejemplo `/uploads/inspection-cases/VZ-2026-00000001/<archivo>`.

Consultar estado publicamente con codigo de caso y telefono:

```bash
curl "http://localhost:8080/api/public/inspection-cases/status?trackingCode=VZ-2026-00000001&phone=584125551212"
```

Los telefonos se normalizan antes de guardarse y buscarse. Por ejemplo:

- `+58 412 555-1212`
- `+58-412-555-1212`
- `+58(412)5551212`

se guardan como:

```text
+584125551212
```

La consulta publica compara por digitos, asi que puede recibir `584125551212` o `%2B584125551212`.

## Consulta de casos de inspeccion

Requiere JWT de usuario interno (`ADMIN`, `COORDINATOR` o `ENGINEER`).

Consultar por codigo:

```bash
curl "http://localhost:8080/api/inspection-cases?trackingCode=VZ-2026-00000001" \
  -H "Authorization: Bearer <accessToken>"
```

Consultar por estado:

```bash
curl "http://localhost:8080/api/inspection-cases?status=PENDIENTE" \
  -H "Authorization: Bearer <accessToken>"
```

Consultar por ciudad:

```bash
curl "http://localhost:8080/api/inspection-cases?city=Caracas" \
  -H "Authorization: Bearer <accessToken>"
```

Consultar por prioridad:

```bash
curl "http://localhost:8080/api/inspection-cases?priority=HIGH" \
  -H "Authorization: Bearer <accessToken>"
```

Consultar por fecha de creacion:

```bash
curl "http://localhost:8080/api/inspection-cases?createdDate=2026-06-28" \
  -H "Authorization: Bearer <accessToken>"
```

Los filtros se pueden combinar:

```bash
curl "http://localhost:8080/api/inspection-cases?city=Caracas&status=PENDIENTE&priority=HIGH&createdDate=2026-06-28" \
  -H "Authorization: Bearer <accessToken>"
```

Subir fotografias a un caso existente como usuario interno:

```bash
curl -X POST http://localhost:8080/api/inspection-cases/1/photos \
  -H "Authorization: Bearer <accessToken>" \
  -F 'photos=@/ruta/foto-1.jpg;type=image/jpeg' \
  -F 'photos=@/ruta/foto-2.png;type=image/png'
```

## Asignacion de ingenieros a casos

Requiere JWT de usuario interno con rol `ADMIN` o `COORDINATOR`.

Listar ingenieros activos disponibles para asignar:

```bash
curl http://localhost:8080/api/users/engineers \
  -H "Authorization: Bearer <accessToken>"
```

Asignar uno o mas ingenieros a un caso:

```bash
curl -X POST http://localhost:8080/api/inspection-cases/1/assignments \
  -H "Authorization: Bearer <accessToken>" \
  -H 'Content-Type: application/json' \
  -d '{
    "engineerIds": [3, 4]
  }'
```

Reglas:

- Los usuarios asignados deben existir.
- Los usuarios asignados deben tener rol `ENGINEER`.
- Los usuarios asignados deben estar `ACTIVE`.
- No se duplican asignaciones del mismo ingeniero al mismo caso.
- Al asignar, el caso pasa a `ASIGNADO`.

Eliminar la asignacion de un ingeniero:

```bash
curl -X DELETE http://localhost:8080/api/inspection-cases/1/assignments/3 \
  -H "Authorization: Bearer <accessToken>"
```

Donde `1` es el id del caso y `3` es el id del ingeniero.
Si el caso queda sin ingenieros asignados, vuelve automaticamente a `PENDIENTE`.
