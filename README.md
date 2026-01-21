# GoldenRoseAPK

Aplicación móvil teórica para la compra de skins de Valorant. El proyecto ahora funciona en modo local para desarrollo, usando datos almacenados en el dispositivo en lugar de consumir APIs externas.

## Datos locales actuales
- **Catálogo de productos**: se carga desde `app/src/main/assets/products.json` y se renderiza usando imágenes locales (`android.resource://`).
- **Usuarios**: se guardan en SQLite (Room) para simular registro e inicio de sesión sin depender de servicios externos.
- **Órdenes**: se guardan en archivos JSON dentro del almacenamiento interno de la app para simular compras (pendiente migración a SQLite).
- **Boletas**: se guardan en SQLite (Room) para mantener el historial de comprobantes de compra.
