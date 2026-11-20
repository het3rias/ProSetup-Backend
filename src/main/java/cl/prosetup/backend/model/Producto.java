package cl.prosetup.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "producto")
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

    /** Precio normal en pesos chilenos (CLP), sin decimales. */
    @Column(nullable = false)
    private int precio;

    /** Precio rebajado. Null si el producto no está en oferta. */
    private Integer precioOferta;

    @Column(nullable = false)
    private int stock;

    /** Cuando stock <= stockCritico el producto aparece en "Productos críticos". Null = sin alerta. */
    private Integer stockCritico;

    @Column(length = 500)
    private String imagen;

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
    public Integer getPrecioOferta() { return precioOferta; }
    public void setPrecioOferta(Integer precioOferta) { this.precioOferta = precioOferta; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public Integer getStockCritico() { return stockCritico; }
    public void setStockCritico(Integer stockCritico) { this.stockCritico = stockCritico; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    /** Precio que realmente se cobra: el de oferta si existe y es menor al normal. */
    public int precioFinal() {
        return (precioOferta != null && precioOferta < precio) ? precioOferta : precio;
    }

    public boolean stockEsCritico() {
        return stockCritico != null && stock <= stockCritico;
    }
}
