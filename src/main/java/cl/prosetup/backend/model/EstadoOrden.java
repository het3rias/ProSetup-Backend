package cl.prosetup.backend.model;

public enum EstadoOrden {
    PAGADA("Pagada"), ENVIADA("Enviada"), ENTREGADA("Entregada"), RECHAZADA("Rechazada");

    private final String etiqueta;

    EstadoOrden(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Texto que usa el frontend: "Pagada", "Enviada", "Entregada" o "Rechazada". */
    public String etiqueta() {
        return etiqueta;
    }

    public static EstadoOrden desdeEtiqueta(String texto) {
        for (EstadoOrden e : values()) {
            if (e.etiqueta.equalsIgnoreCase(texto) || e.name().equalsIgnoreCase(texto)) return e;
        }
        throw new IllegalArgumentException("Estado no válido: " + texto);
    }
}
