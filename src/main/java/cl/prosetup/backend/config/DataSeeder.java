package cl.prosetup.backend.config;

import cl.prosetup.backend.model.*;
import cl.prosetup.backend.repository.CategoriaRepository;
import cl.prosetup.backend.repository.OrdenRepository;
import cl.prosetup.backend.repository.ProductoRepository;
import cl.prosetup.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Carga los mismos datos iniciales que tenía el frontend, la primera vez que la base de datos está vacía. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final OrdenRepository ordenes;
    private final PasswordEncoder encoder;

    public DataSeeder(CategoriaRepository categorias, ProductoRepository productos, UsuarioRepository usuarios,
                      OrdenRepository ordenes, PasswordEncoder encoder) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.ordenes = ordenes;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categorias.count() == 0) {
            sembrarCatalogo();
        }
        if (usuarios.count() == 0) {
            sembrarUsuarios();
            sembrarOrdenes();
        }
    }

    private void sembrarCatalogo() {
        Categoria perifericos = categorias.save(new Categoria("Perifericos", "Teclados, mouse y todo lo que conectas a tu PC.", "/img/teclado.jpg"));
        Categoria audio = categorias.save(new Categoria("Audio", "Audífonos, parlantes y micrófonos para jugar y transmitir.", "/img/7.1.png"));
        Categoria monitores = categorias.save(new Categoria("Monitores", "Pantallas de alta tasa de refresco para gaming y trabajo.", "/img/monitor144.webp"));
        Categoria accesorios = categorias.save(new Categoria("Accesorios", "Mousepads, webcams, hubs y soportes para tu escritorio.", "/img/mousepadrgb.jpeg"));
        Categoria componentes = categorias.save(new Categoria("Componentes", "Almacenamiento y piezas para armar o mejorar tu equipo.", "/img/SSD.png"));
        Categoria sillas = categorias.save(new Categoria("Sillas y Mobiliario", "Sillas y muebles para largas sesiones frente al PC.", "/img/silla.webp"));

        producto("TEC-001", "Teclado Mecánico RGB", "Teclado mecánico con switches rojos e iluminación RGB personalizable.", 45990, null, 15, 5, perifericos, "/img/teclado.jpg");
        producto("MOU-001", "Mouse Gamer 16000 DPI", "Mouse óptico gamer de alta precisión, sensor de 16000 DPI ajustable.", 29990, 24990, 20, 5, perifericos, "/img/mouse.jpg");
        producto("AUD-001", "Audífonos Gaming 7.1", "Audífonos con sonido envolvente 7.1 y micrófono desmontable.", 38990, null, 12, 4, audio, "/img/7.1.png");
        producto("MON-001", "Monitor Gamer 24\" 144Hz", "Monitor Full HD de 24 pulgadas con tasa de refresco de 144Hz.", 149990, 129990, 8, 3, monitores, "/img/monitor144.webp");
        producto("ACC-001", "Mousepad XL RGB", "Mousepad extendido con borde iluminado RGB.", 14990, 9990, 30, 8, accesorios, "/img/mousepadrgb.jpeg");
        producto("ACC-002", "Webcam Full HD 1080p", "Webcam con resolución 1080p y corrección automática de luz.", 35990, null, 14, 4, accesorios, "/img/webcam.webp");
        producto("SIL-001", "Silla Gamer Ergonómica", "Silla gamer con respaldo reclinable, apoyabrazos ajustables y base de 5 ruedas.", 119990, null, 3, 3, sillas, "/img/silla.webp");
        producto("AUD-002", "Parlantes Bluetooth 2.1", "Set de parlantes 2.1 con subwoofer, conexión Bluetooth y entrada auxiliar.", 42990, 34990, 10, 3, audio, "/img/parlantes.webp");
        producto("AUD-003", "Micrófono de Condensador USB", "Micrófono USB plug and play para streaming y grabación, con soporte antivibración.", 54990, null, 9, 3, audio, "/img/microfono.jpg");
        producto("ACC-003", "Hub USB-C 7 en 1", "Adaptador USB-C con salida HDMI, lector de tarjetas SD/microSD y 3 puertos USB 3.0.", 24990, null, 18, 5, accesorios, "/img/conectar71.png");
        producto("ACC-004", "Soporte de Monitor Articulado", "Brazo articulado de escritorio para un monitor, con ajuste de altura e inclinación.", 32990, null, 2, 3, accesorios, "/img/soporte.png");
        producto("COM-001", "SSD NVMe 1TB", "Unidad de estado sólido NVMe M.2 de 1TB, velocidades de lectura de hasta 3500 MB/s.", 64990, 54990, 16, 4, componentes, "/img/SSD.png");
    }

    private void producto(String codigo, String nombre, String descripcion, int precio, Integer precioOferta,
                          int stock, int stockCritico, Categoria categoria, String imagen) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setPrecioOferta(precioOferta);
        p.setStock(stock);
        p.setStockCritico(stockCritico);
        p.setCategoria(categoria);
        p.setImagen(imagen);
        productos.save(p);
    }

    private void sembrarUsuarios() {
        usuario("190110222", "Admin", "ProSetup", "admin@duoc.cl", Role.ADMIN, "Santiago", "Av. Siempre Viva 123", null, null);
        usuario("18057784K", "Vero", "Vendedora", "vendedor@duoc.cl", Role.VENDEDOR, "Maipú", "Calle Comercio 456", null, null);
        usuario("204567891", "Camila", "Rojas Pérez", "cliente@gmail.com", Role.CLIENTE, "La Florida", "Los Crisantemos 1450",
                "912345678", LocalDate.of(2001, 5, 14));
    }

    private void usuario(String run, String nombre, String apellidos, String correo, Role rol, String comuna,
                         String direccion, String telefono, LocalDate nacimiento) {
        Usuario u = new Usuario();
        u.setRun(run);
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setPassword(encoder.encode("1234"));
        u.setTelefono(telefono);
        u.setFechaNacimiento(nacimiento);
        u.setRegion("Región Metropolitana de Santiago");
        u.setComuna(comuna);
        u.setDireccion(direccion);
        u.setRol(rol);
        u.setActivo(true);
        usuarios.save(u);
    }

    /** Dos órdenes de ejemplo para que el dashboard, las boletas y los reportes no partan vacíos. */
    private void sembrarOrdenes() {
        Usuario camila = usuarios.findByCorreoIgnoreCase("cliente@gmail.com").orElseThrow();
        Producto teclado = productos.findAll().stream().filter(p -> p.getCodigo().equals("TEC-001")).findFirst().orElseThrow();
        Producto mouse = productos.findAll().stream().filter(p -> p.getCodigo().equals("MOU-001")).findFirst().orElseThrow();
        Producto ssd = productos.findAll().stream().filter(p -> p.getCodigo().equals("COM-001")).findFirst().orElseThrow();

        Orden o1 = ordenBase(camila, LocalDateTime.of(2026, 9, 28, 15, 20), EstadoOrden.PAGADA, "", null);
        agregar(o1, teclado, 1);
        agregar(o1, mouse, 1);
        cerrar(o1);
        ordenes.save(o1);

        Orden o2 = ordenBase(camila, LocalDateTime.of(2026, 10, 3, 19, 5), EstadoOrden.ENVIADA, "Depto 302", "Dejar con conserjería");
        agregar(o2, ssd, 1);
        cerrar(o2);
        ordenes.save(o2);
    }

    private Orden ordenBase(Usuario u, LocalDateTime fecha, EstadoOrden estado, String depto, String indicaciones) {
        Orden o = new Orden();
        o.setUsuario(u);
        o.setFecha(fecha);
        o.setEstado(estado);
        o.setClienteNombre(u.getNombre());
        o.setClienteApellidos(u.getApellidos());
        o.setClienteEmail(u.getCorreo());
        o.setEntrega("domicilio");
        o.setCalle(u.getDireccion());
        o.setDepartamento(depto);
        o.setRegion(u.getRegion());
        o.setComuna(u.getComuna());
        o.setIndicaciones(indicaciones);
        return o;
    }

    private void agregar(Orden o, Producto p, int cantidad) {
        OrdenItem item = new OrdenItem();
        item.setProducto(p);
        item.setCodigo(p.getCodigo());
        item.setNombre(p.getNombre());
        item.setImagen(p.getImagen());
        item.setPrecioUnitario(p.precioFinal());
        item.setCantidad(cantidad);
        o.agregarItem(item);
    }

    private void cerrar(Orden o) {
        int subtotal = o.getItems().stream().mapToInt(OrdenItem::subtotal).sum();
        int envio = cl.prosetup.backend.service.OrdenService.calcularEnvio(subtotal, o.getEntrega());
        o.setSubtotal(subtotal);
        o.setCostoEnvio(envio);
        o.setTotal(subtotal + envio);
    }
}
