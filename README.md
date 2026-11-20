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

1. En `POST /api/v1/auth/login` entrar con una cuenta de prueba y copiar el `accessToken`.
2. Presionar el botón **Authorize** (arriba a la derecha) y pegar el token.
3. Ya se pueden probar los endpoints protegidos según el rol.

## Endpoints

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | /api/v1/auth/register | Público | Registra un cliente (rol siempre CLIENTE) |
| POST | /api/v1/auth/login | Público | Body `{email, password}`. Devuelve accessToken, refreshToken y el usuario |
| POST | /api/v1/auth/refresh | Público | Renueva los tokens con el refreshToken |
| GET | /api/v1/auth/me | Autenticado | Usuario actual (restaurar sesión al recargar) |
| GET | /api/v1/productos | Público | Lista productos (filtros `q`, `categoriaId`, `oferta`) |
| GET | /api/v1/productos/{id} | Público | Detalle de un producto |
| GET | /api/v1/productos/criticos | ADMIN | Productos con stock crítico |
| POST | /api/v1/productos | ADMIN | Crea un producto |
| PUT | /api/v1/productos/{id} | ADMIN | Actualiza un producto (el VENDEDOR solo consulta) |
| DELETE | /api/v1/productos/{id} | ADMIN | Elimina un producto |
| GET | /api/v1/categorias, /api/v1/categorias/{id} | Público | Lee categorías |
| POST, PUT, DELETE | /api/v1/categorias[/{id}] | ADMIN | Crea, actualiza y elimina categorías |
| GET, PUT | /api/v1/usuarios/me | Autenticado | Ver y editar mi perfil |
| GET, POST | /api/v1/usuarios | ADMIN | Lista y crea usuarios (cualquier rol) |
| GET, PUT, DELETE | /api/v1/usuarios/{id} | ADMIN | Ver, actualizar y eliminar usuarios |
| POST | /api/v1/ordenes | Público (invitado o con sesión) | Crea una compra: el servidor calcula precios, envío y total, y descuenta el stock. Si falta stock o se simula rechazo queda `Rechazada` |
| GET | /api/v1/ordenes/mias | Autenticado | Mis órdenes |
| GET | /api/v1/ordenes | ADMIN, VENDEDOR | Todas las órdenes |
| GET | /api/v1/ordenes/{id} | Dueño, ADMIN, VENDEDOR | Detalle de una orden |
| PUT | /api/v1/ordenes/{id}/estado | ADMIN | Cambia el estado de la orden |
| GET | /api/v1/admin/dashboard | ADMIN | Estadísticas del panel |

Todas las rutas llevan el prefijo de versión `/api/v1`. El VENDEDOR solo puede consultar productos y órdenes (lista y detalle); el resto de las acciones le responde 403.

## Modelo de datos

Tablas: `usuario` → `boleta` → `detalle_boleta` → `producto` ← `categoria`.

- `boleta` guarda la compra (cliente, entrega, subtotal, envío, total, estado). El usuario es opcional, porque se permite comprar como invitado.
- `detalle_boleta` guarda cada línea con el nombre y el precio del producto al momento de la compra.
- Entidades JPA: `Orden` (tabla `boleta`) y `OrdenItem` (tabla `detalle_boleta`).

Códigos de respuesta: 200/201/204 éxito, 400 datos inválidos, 401 sin token o token inválido, 403 sin permiso por rol, 404 no encontrado, 409 conflicto (duplicado o registro relacionado).

## Cómo funciona la autenticación

- Al hacer login se validan correo y contraseña (almacenada con **BCrypt**) y se entregan dos tokens JWT firmados (HMAC-SHA):
  - **accessToken** (30 min): se envía en cada petición con `Authorization: Bearer <token>`.
  - **refreshToken** (7 días): solo sirve en `/api/v1/auth/refresh` para obtener un nuevo par de tokens.
- Cada token lleva el correo, el rol y un claim `type` (`access` o `refresh`) para que uno no pueda usarse como el otro.
- `JwtAuthFilter` revisa el token en cada petición y deja al usuario autenticado con su rol. La API es **stateless** (sin sesiones en el servidor).
- `SecurityConfig` define qué rol puede usar cada ruta.

## Formato de los datos (contrato con el frontend)

- Usuario: `{id, run, nombre, apellidos, email, telefono, fechaNacimiento, tipoUsuario, region, comuna, direccion}`. `tipoUsuario` es `Administrador`, `Vendedor` o `Cliente`. La contraseña nunca se devuelve.
- Producto: `{id, codigo, nombre, descripcion, precio, precioOferta, stock, stockCritico, categoria, imagen}` (`categoria` es el nombre).
- Orden: `{id, codigo ("ORDER-n"), fecha, estado, motivo, usuarioEmail, cliente, direccion, entrega, items, subtotal, costoEnvio, total}`. Estados: `Pagada`, `Enviada`, `Entregada`, `Rechazada`.
- Envío: gratis con retiro en tienda o compras desde $50.000; si no, $3.990.

## Reglas de negocio

- RUN validado con módulo 11; correos solo `@duoc.cl`, `@profesor.duoc.cl` o `@gmail.com`; contraseña de 4 a 10 caracteres.
- El registro público siempre crea clientes; solo un ADMIN puede crear usuarios con otro rol.
- Al comprar: se verifica stock (con bloqueo para evitar ventas dobles), el precio con descuento, el envío y el total se calculan en el servidor, y se descuenta el stock. Una orden rechazada se guarda con su motivo y no toca el stock. La boleta guarda el nombre y precio del producto al momento de la compra.
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
