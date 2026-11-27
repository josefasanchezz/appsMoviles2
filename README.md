# Caso 007D – Gestión móvil de oportunidades y activaciones corporativas

Kotlin · Jetpack Compose · MVVM · Room · Retrofit (opcional)

## Cómo abrirlo
1. Android Studio (Koala o superior) → *Open* → carpeta del proyecto.
2. Esperar el *Gradle Sync* (descarga Gradle 8.9 y dependencias).
3. Ejecutar en emulador o dispositivo (minSdk 24).

## Usuarios de prueba (ficticios)
| Usuario | Clave | Rol |
|---|---|---|
| ejecutivo | 1234 | Ejecutivo comercial |
| ejecutivo2 | 1234 | Ejecutivo comercial |
| supervisor | 1234 | Supervisor comercial |

## Estructura
- `data/` → entidades Room, DAO, base de datos, repositorio, Retrofit.
- `vm/` → ViewModels (Auth, Lista, Formulario, Detalle, Catálogo).
- `ui/` → pantallas Compose.

## Flujo implementado (según diagrama)
- Login → perfil Ejecutivo / Supervisor.
- Ejecutivo: registrar oportunidad (evento, fecha, comuna, organización, contacto, público, prioridad), registrar gestiones y propuestas, definir próxima acción.
- Supervisor: ver todas, filtrar/buscar, ver detalle e historial, ajustar prioridad, reasignar responsable.
- Módulo Gestión de activaciones: condiciones (espacio, fecha, costo, requisitos), responsable, evidencia, confirmación; registro de resultados y cierre.
- Estados: identificado → contacto pendiente → contacto realizado → propuesta enviada → en espera → confirmada / descartada.
- Historial de cambios automático en cada acción.
- API REST opcional: catálogo de eventos (JSONPlaceholder) con respaldo local si no hay internet.
