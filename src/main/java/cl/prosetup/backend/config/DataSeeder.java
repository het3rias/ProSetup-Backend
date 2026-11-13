package cl.prosetup.backend.config;

import cl.prosetup.backend.model.*;
import cl.prosetup.backend.repository.CategoriaRepository;
import cl.prosetup.backend.repository.ProductoRepository;
import cl.prosetup.backend.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Carga datos iniciales la primera vez que la base de datos está vacía. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final CategoriaRepository categorias;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public DataSeeder(CategoriaRepository categorias, ProductoRepository productos,
                      UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.categorias = categorias;
        this.productos = productos;
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        if (categorias.count() == 0) {
            sembrarCatalogo();
        }
        if (usuarios.count() == 0) {
            sembrarUsuarios();
        }
    }

    private void sembrarCatalogo() {
        Categoria cpu = categorias.save(new Categoria("Procesadores", "CPU para armar tu PC"));
        Categoria gpu = categorias.save(new Categoria("Tarjetas de video", "GPU para gaming y diseño"));
        Categoria ram = categorias.save(new Categoria("Memoria RAM", "Módulos de memoria DDR4 y DDR5"));
        Categoria ssd = categorias.save(new Categoria("Almacenamiento", "SSD y discos duros"));
        Categoria mb = categorias.save(new Categoria("Placas madre", "Motherboards AM4, AM5 y LGA"));
        Categoria per = categorias.save(new Categoria("Periféricos", "Teclados, mouse y audífonos"));

        producto("CPU-001", "AMD Ryzen 5 5600", "6 núcleos, 12 hilos, socket AM4", 139990, 15, 5, false, 0, cpu);
        producto("CPU-002", "Intel Core i5-12400F", "6 núcleos, 12 hilos, socket LGA1700", 149990, 10, 4, true, 10, cpu);
        producto("GPU-001", "NVIDIA GeForce RTX 4060 8GB", "Ideal para 1080p en ultra", 329990, 6, 3, false, 0, gpu);
        producto("GPU-002", "AMD Radeon RX 7600 8GB", "Ray tracing y 1080p fluido", 279990, 3, 3, true, 15, gpu);
        producto("RAM-001", "Kingston Fury 16GB DDR4 3200MHz", "Kit 2x8GB", 42990, 25, 8, false, 0, ram);
        producto("SSD-001", "WD Blue SN580 1TB NVMe", "Lectura hasta 4150 MB/s", 64990, 18, 6, true, 20, ssd);
        producto("MB-001", "ASUS TUF Gaming B550-Plus", "Placa madre AM4 ATX", 139990, 8, 3, false, 0, mb);
        producto("PER-001", "Teclado Mecánico Redragon Kumara", "Switch rojo, formato TKL", 29990, 2, 4, false, 0, per);
    }

    private void producto(String codigo, String nombre, String descripcion, int precio, int stock,
                          int stockCritico, boolean oferta, int descuento, Categoria categoria) {
        Producto p = new Producto();
        p.setCodigo(codigo);
        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(precio);
        p.setStock(stock);
        p.setStockCritico(stockCritico);
        p.setImagen("img/productos/" + codigo.toLowerCase() + ".jpg");
        p.setOferta(oferta);
        p.setDescuento(descuento);
        p.setCategoria(categoria);
        productos.save(p);
    }

    private void sembrarUsuarios() {
        usuario("11111111-1", "Admin", "ProSetup", "admin@duoc.cl", Role.ADMIN);
        usuario("12345678-5", "Vicente", "Vendedor", "vendedor@duoc.cl", Role.VENDEDOR);
        usuario("18765432-7", "Carla", "Cliente", "cliente@gmail.com", Role.CLIENTE);
    }

    private void usuario(String run, String nombre, String apellidos, String correo, Role rol) {
        Usuario u = new Usuario();
        u.setRun(run.replace("-", ""));
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setPassword(encoder.encode("1234"));
        u.setRegion("Región Metropolitana de Santiago");
        u.setComuna("La Florida");
        u.setDireccion("Av. Vicuña Mackenna 1234");
        u.setRol(rol);
        u.setActivo(true);
        usuarios.save(u);
    }
}
