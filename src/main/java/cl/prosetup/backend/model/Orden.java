package cl.prosetup.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de compra (boleta). Guarda una copia de los datos del cliente y de la dirección, porque
 * también se puede comprar sin iniciar sesión (usuario = null).
 */
@Entity
@Table(name = "boleta")
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.PAGADA;

    @Column(length = 300)
    private String motivo;

    // ---- Datos del cliente (copia al momento de la compra)
    @Column(nullable = false, length = 50)
    private String clienteNombre;
    @Column(nullable = false, length = 100)
    private String clienteApellidos;
    @Column(nullable = false, length = 100)
    private String clienteEmail;

    // ---- Entrega
    @Column(nullable = false, length = 20)
    private String entrega;
    @Column(length = 300)
    private String calle;
    @Column(length = 50)
    private String departamento;
    @Column(length = 80)
    private String region;
    @Column(length = 80)
    private String comuna;
    @Column(length = 300)
    private String indicaciones;

    // ---- Totales (siempre calculados en el servidor)
    private int subtotal;
    private int costoEnvio;
    private int total;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdenItem> items = new ArrayList<>();

    public void agregarItem(OrdenItem item) {
        item.setOrden(this);
        items.add(item);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public EstadoOrden getEstado() { return estado; }
    public void setEstado(EstadoOrden estado) { this.estado = estado; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre; }
    public String getClienteApellidos() { return clienteApellidos; }
    public void setClienteApellidos(String clienteApellidos) { this.clienteApellidos = clienteApellidos; }
    public String getClienteEmail() { return clienteEmail; }
    public void setClienteEmail(String clienteEmail) { this.clienteEmail = clienteEmail; }
    public String getEntrega() { return entrega; }
    public void setEntrega(String entrega) { this.entrega = entrega; }
    public String getCalle() { return calle; }
    public void setCalle(String calle) { this.calle = calle; }
    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public String getComuna() { return comuna; }
    public void setComuna(String comuna) { this.comuna = comuna; }
    public String getIndicaciones() { return indicaciones; }
    public void setIndicaciones(String indicaciones) { this.indicaciones = indicaciones; }
    public int getSubtotal() { return subtotal; }
    public void setSubtotal(int subtotal) { this.subtotal = subtotal; }
    public int getCostoEnvio() { return costoEnvio; }
    public void setCostoEnvio(int costoEnvio) { this.costoEnvio = costoEnvio; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public List<OrdenItem> getItems() { return items; }
    public void setItems(List<OrdenItem> items) { this.items = items; }
}
