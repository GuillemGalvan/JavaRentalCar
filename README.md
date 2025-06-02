# HavanaRentalCars

**HavanaRentalCars** es una aplicación Java de consola que implementa un sistema básico de gestión de alquiler de coches. Incluye operaciones CRUD para **Personas**, **Coches** y **Reservas**, así como la posibilidad de crear reservas entre personas y coches.

## Características principales

- CRUD completo para:
  - Clientes (Personas)
  - Vehículos (Coches)
  - Reservas
- Asociación entre personas y coches mediante reservas.
- Proyecto estructurado en dos versiones con ramas distintas:
  - **`sin-dao`**: implementación directa sin patrón DAO, conectada directamente a **MySQL**.
  - **`con-dao`**: implementación siguiendo el **patrón DAO**, permite cambiar fácilmente de base de datos.

## Ramas del repositorio

### `sin-dao`

- Acceso a datos directamente en los métodos de servicio.
- Pensada para **uso con MySQL**.
- Código más directo, pero acoplado a la base de datos concreta.

### `con-dao`

- Arquitectura basada en el **patrón DAO (Data Access Object)**.
- Soporta múltiples bases de datos:
  - ✅ MySQL
  - ✅ MariaDB
  - ✅ MongoDB (a través de implementación específica del DAO)
- Mejor separación de responsabilidades y fácil mantenibilidad.


