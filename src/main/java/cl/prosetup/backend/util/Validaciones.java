package cl.prosetup.backend.util;

import java.util.List;

/** Reglas de validación del cliente (las mismas que usa el frontend). */
public final class Validaciones {

    private static final List<String> DOMINIOS_PERMITIDOS =
            List.of("@duoc.cl", "@profesor.duoc.cl", "@gmail.com");

    private Validaciones() {}

    /** Quita puntos y guion y deja el RUN en mayúsculas: 12.345.678-5 -> 123456785. */
    public static String normalizarRun(String run) {
        if (run == null) return "";
        return run.replace(".", "").replace("-", "").trim().toUpperCase();
    }

    /** Valida el RUN con el algoritmo módulo 11 (7 u 8 dígitos + dígito verificador). */
    public static boolean runValido(String run) {
        String r = normalizarRun(run);
        if (!r.matches("\\d{7,8}[0-9K]")) return false;

        String cuerpo = r.substring(0, r.length() - 1);
        char dv = r.charAt(r.length() - 1);

        int suma = 0;
        int factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += (cuerpo.charAt(i) - '0') * factor;
            factor = (factor == 7) ? 2 : factor + 1;
        }
        int resto = 11 - (suma % 11);
        char esperado = (resto == 11) ? '0' : (resto == 10) ? 'K' : (char) ('0' + resto);
        return dv == esperado;
    }

    /** Solo se aceptan correos @duoc.cl, @profesor.duoc.cl y @gmail.com. */
    public static boolean correoPermitido(String correo) {
        if (correo == null) return false;
        String c = correo.trim().toLowerCase();
        return DOMINIOS_PERMITIDOS.stream().anyMatch(c::endsWith);
    }

    /** La contraseña debe tener entre 4 y 10 caracteres. */
    public static boolean passwordValida(String password) {
        return password != null && password.length() >= 4 && password.length() <= 10;
    }
}
