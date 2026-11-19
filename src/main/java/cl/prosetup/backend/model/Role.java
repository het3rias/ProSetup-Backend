package cl.prosetup.backend.model;

public enum Role {
    ADMIN("Administrador"), VENDEDOR("Vendedor"), CLIENTE("Cliente");

    private final String etiqueta;

    Role(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    /** Nombre que usa el frontend: "Administrador", "Vendedor" o "Cliente". */
    public String etiqueta() {
        return etiqueta;
    }

    public static Role desdeEtiqueta(String texto) {
        for (Role r : values()) {
            if (r.etiqueta.equalsIgnoreCase(texto) || r.name().equalsIgnoreCase(texto)) return r;
        }
        throw new IllegalArgumentException("Tipo de usuario no válido: " + texto);
    }
}
