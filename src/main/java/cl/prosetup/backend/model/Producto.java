package cl.prosetup.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    /** Precio en pesos chilenos (CLP), sin decimales. */
    @Column(nullable = false)
    private int precio;

    @Column(nullable = false)
    private int stock;

    /** Cuando stock <= stockCritico el producto aparece en "Productos críticos". */
    @Column(nullable = false)
    private int stockCritico;

    @Column(length = 500)
    private String imagen;

    @Column(nullable = false)
    private boolean oferta;

    /** Porcentaje de descuento (0-100). Solo aplica si oferta = true. */
    @Column(nullable = false)
    private int descuento;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getPrecio() { return precio; }
    public void setPrecio(int precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public int getStockCritico() { return stockCritico; }
    public void setStockCritico(int stockCritico) { this.stockCritico = stockCritico; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public boolean isOferta() { return oferta; }
    public void setOferta(boolean oferta) { this.oferta = oferta; }
    public int getDescuento() { return descuento; }
    public void setDescuento(int descuento) { this.descuento = descuento; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    /** Precio final con el descuento aplicado (se calcula, no se guarda en la BD). */
    public int getPrecioFinal() {
        if (oferta && descuento > 0) {
            return precio - (precio * descuento / 100);
        }
        return precio;
    }
}
