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

## Dominio

Para conectar `puntodeapoyo.org`:

1. Agregar `puntodeapoyo.org` como site/zone en Cloudflare.
2. Cambiar los nameservers en el registrador del dominio por los nameservers que indique Cloudflare.
3. Ir a Cloudflare > Workers & Pages > `punto-de-apoyo` > Custom domains.
4. Seleccionar `Set up a domain`.
5. Agregar `puntodeapoyo.org`.
6. Repetir con `www.puntodeapoyo.org` si tambien se quiere publicar `www`.
