# Política de seguridad

## Versiones con soporte

Este es un proyecto personal mantenido por una sola persona. Solo se corrigen vulnerabilidades en la **última versión
publicada**.

| Versión              | Soporte |
| -------------------- | ------- |
| Última release (1.x) | ✅      |
| Anteriores           | ❌      |

## Cómo reportar una vulnerabilidad

**No abras un issue público.** Usa el reporte privado de
GitHub: [Reportar una vulnerabilidad](https://github.com/JSamuelAP/oikonomia-api/security/advisories/new)
(pestaña **Security** → **Report a vulnerability**).

De preferencia, incluye:

- Descripción y el impacto que crees que tiene.
- Pasos para reproducirlo o una prueba de concepto.
- Versión afectada.

## Qué esperar

Se atiende con el mejor esfuerzo posible, sin acuerdos de nivel de servicio:

- Acuse de recibo en 7 días.
- Evaluación inicial en 14 días.
- La corrección se publica en una nueva versión con un aviso de seguridad (security advisory). Si lo deseas, se te
  mencionará como autor del reporte.

## Alcance

### Dentro del alcance

- Autenticación y sesiones: JWT, refresh tokens, rotación y revocación.
- Autorización: acceso a datos de otros usuarios (IDOR).
- Inyección y exposición de datos sensibles en respuestas o logs.
- Evasión del rate limiting.
- Configuración insegura de CORS o cabeceras de seguridad con impacto demostrable.
- Vulnerabilidades en dependencias con una ruta de explotación demostrable.

### Fuera del alcance

- Denegación de servicio volumétrica y pruebas de carga.
- Resultados de escáneres automáticos sin prueba de concepto.
- Dependencias vulnerables sin ruta de explotación (ya se monitorean con Dependabot).
- Ingeniería social y ataques que requieran acceso físico.
- Fallos en instancias desplegadas por terceros.

Si el problema está en la interfaz web, repórtalo en
el [repositorio del frontend](https://github.com/JSamuelAP/oikonomia-ui/security/policy).

## Divulgación coordinada

Por favor, no hagas público el problema hasta que haya una corrección disponible o hayan pasado 90 días desde el reporte,
lo que ocurra primero.