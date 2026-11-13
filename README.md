# ProSetup Backend

API REST de **ProSetup** (tienda de componentes de PC) hecha con **Spring Boot 3**, **MySQL**, **Swagger** y autenticación **JWT** con roles.
Proyecto de la asignatura DSY1104 – Desarrollo Fullstack II (Evaluación Parcial 3). El frontend está en el repositorio `ProSetup-React`.

## Requisitos

- Java 17 o superior
- Maven 3.9+ (o usar el Maven integrado del IDE)
- XAMPP con **MySQL** iniciado (puerto 3306). Opcional: MySQL Workbench para ver las tablas.

## Cómo ejecutarlo

1. Abrir XAMPP y presionar **Start** en MySQL.
2. Ejecutar:
   ```bash
   mvn spring-boot:run
   ```
3. La base de datos `prosetup` se crea sola y se cargan datos de ejemplo la primera vez.
4. API en `http://localhost:8080` y documentación Swagger en **http://localhost:8080/swagger-ui.html**

Si MySQL tiene contraseña, definir las variables de entorno `DB_USER` y `DB_PASSWORD`. Para producción definir también `JWT_SECRET` (mínimo 32 caracteres).

## Cuentas de prueba

| Rol | Correo | Contraseña |
|---|---|---|
| ADMIN | admin@duoc.cl | 1234 |
| VENDEDOR | vendedor@duoc.cl | 1234 |
| CLIENTE | cliente@gmail.com | 1234 |

## Probar en Swagger

1. En `POST /api/auth/login` entrar con una cuenta de prueba y copiar el `accessToken`.
2. Presionar el botón **Authorize** (arriba a la derecha) y pegar el token.
3. Ya se pueden probar los endpoints protegidos según el rol.

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | /api/auth/register | Público | Registra un cliente (rol siempre CLIENTE) |
| POST | /api/auth/login | Público | Devuelve accessToken y refreshToken |
| POST | /api/auth/refresh | Público | Renueva los tokens con el refreshToken |
| GET | /api/auth/me | Autenticado | Usuario actual (restaurar sesión al recargar) |
| GET | /api/productos | Público | Lista productos (filtros `q`, `categoriaId`, `oferta`) |
| GET | /api/productos/{id} | Público | Detalle de un producto |
| GET | /api/productos/criticos | ADMIN, VENDEDOR | Productos con stock crítico |
| POST | /api/productos | ADMIN, VENDEDOR | Crea un producto |
| PUT | /api/productos/{id} | ADMIN, VENDEDOR | Actualiza un producto |
| DELETE | /api/productos/{id} | ADMIN | Elimina un producto |
| GET | /api/categorias, /api/categorias/{id} | Público | Lee categorías |
| POST, PUT, DELETE | /api/categorias[/{id}] | ADMIN | Crea, actualiza y elimina categorías |
| GET, PUT | /api/usuarios/me | Autenticado | Ver y editar mi perfil |
| GET, POST | /api/usuarios | ADMIN | Lista y crea usuarios (cualquier rol) |
| GET, PUT, DELETE | /api/usuarios/{id} | ADMIN | Ver, actualizar y eliminar usuarios |
| POST | /api/ordenes | Autenticado | Crea una compra (descuenta stock, calcula total) |
| GET | /api/ordenes/mias | Autenticado | Mis órdenes |
| GET | /api/ordenes | ADMIN, VENDEDOR | Todas las órdenes |
| GET | /api/ordenes/{id} | Dueño, ADMIN, VENDEDOR | Detalle de una orden |
| PUT | /api/ordenes/{id}/estado | ADMIN, VENDEDOR | Cambia el estado (si se cancela, devuelve el stock) |
| GET | /api/admin/dashboard | ADMIN, VENDEDOR | Estadísticas del panel |

Códigos de respuesta: 200/201/204 éxito, 400 datos inválidos, 401 sin token o token inválido, 403 sin permiso por rol, 404 no encontrado, 409 conflicto (duplicado o registro relacionado).

## Cómo funciona la autenticación

- Al hacer login se validan correo y contraseña (almacenada con **BCrypt**) y se entregan dos tokens JWT firmados (HMAC-SHA):
  - **accessToken** (30 min): se envía en cada petición con `Authorization: Bearer <token>`.
  - **refreshToken** (7 días): solo sirve en `/api/auth/refresh` para obtener un nuevo par de tokens.
- Cada token lleva el correo, el rol y un claim `type` (`access` o `refresh`) para que uno no pueda usarse como el otro.
- `JwtAuthFilter` revisa el token en cada petición y deja al usuario autenticado con su rol. La API es **stateless** (sin sesiones en el servidor).
- `SecurityConfig` define qué rol puede usar cada ruta.

## Reglas de negocio

- RUN validado con módulo 11; correos solo `@duoc.cl`, `@profesor.duoc.cl` o `@gmail.com`; contraseña de 4 a 10 caracteres.
- El registro público siempre crea clientes; solo un ADMIN puede crear usuarios con otro rol.
- Al comprar: se verifica stock, el precio con descuento y el total se calculan en el servidor, y se descuenta el stock. La boleta guarda el nombre y precio del producto al momento de la compra.
- No se puede eliminar una categoría con productos, ni productos o usuarios que ya tienen órdenes (responde 409).

## Estructura

```
src/main/java/cl/prosetup/backend/
  config/      Seguridad, CORS, Swagger, datos iniciales
  controller/  Endpoints REST
  dto/         Objetos de entrada y salida
  exception/   Manejo de errores
  model/       Entidades JPA
  repository/  Acceso a datos
  security/    JWT y filtro de autenticación
  service/     Lógica de negocio
  util/        Validaciones (RUN, correo, contraseña)
```

## Pruebas

```bash
mvn test
```

Usan una base H2 en memoria (no necesitan MySQL) y verifican las validaciones, el login, el refresh, los permisos por rol y la compra.
