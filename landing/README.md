# Landing page

Pagina estatica temporal para `puntodeapoyo.org`.

## Deploy sugerido sin YAML

Cloudflare Pages conectado directamente al repo de GitHub.

Configuracion:

- Framework preset: `None`
- Build command: dejar vacio
- Output directory: `landing`
- Branch: `main`

Cada push a `main` despliega la landing automaticamente desde Cloudflare Pages.

## Formulario de voluntarios

La pagina `voluntarios.html` contiene el formulario de postulacion y guarda las respuestas en Supabase.

### Configuracion en Supabase

1. Crear un proyecto en Supabase.
2. Ir a `SQL Editor`.
3. Ejecutar el script:

```text
landing/supabase/volunteer_applications.sql
```

Ese script crea la tabla `volunteer_applications`, activa Row Level Security y permite inserts publicos usando la `anon key`.

### Configuracion en la landing

En `landing/voluntarios.html`, reemplazar estos valores:

```js
var SUPABASE_URL = "REEMPLAZAR_SUPABASE_URL";
var SUPABASE_ANON_KEY = "REEMPLAZAR_SUPABASE_ANON_KEY";
```

Por los datos del proyecto Supabase:

```js
var SUPABASE_URL = "https://TU-PROYECTO.supabase.co";
var SUPABASE_ANON_KEY = "TU_ANON_KEY";
```

La `anon key` puede vivir en el frontend porque es publica. No usar nunca la `service_role key` en la landing.

## Dominio

Para conectar `puntodeapoyo.org`:

1. Agregar `puntodeapoyo.org` como site/zone en Cloudflare.
2. Cambiar los nameservers en el registrador del dominio por los nameservers que indique Cloudflare.
3. Ir a Cloudflare > Workers & Pages > `punto-de-apoyo` > Custom domains.
4. Seleccionar `Set up a domain`.
5. Agregar `puntodeapoyo.org`.
6. Repetir con `www.puntodeapoyo.org` si tambien se quiere publicar `www`.
