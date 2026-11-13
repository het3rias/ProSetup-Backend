package cl.prosetup.backend.service;

import cl.prosetup.backend.dto.PerfilRequest;
import cl.prosetup.backend.dto.RegisterRequest;
import cl.prosetup.backend.dto.UsuarioRequest;
import cl.prosetup.backend.dto.UsuarioResponse;
import cl.prosetup.backend.exception.BadRequestException;
import cl.prosetup.backend.exception.ConflictException;
import cl.prosetup.backend.exception.NotFoundException;
import cl.prosetup.backend.model.Role;
import cl.prosetup.backend.model.Usuario;
import cl.prosetup.backend.repository.UsuarioRepository;
import cl.prosetup.backend.util.Validaciones;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAll().stream().map(UsuarioResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.de(buscar(id));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse perfil(String correo) {
        return UsuarioResponse.de(buscarPorCorreo(correo));
    }

    /** Registro público: siempre crea un usuario con rol CLIENTE. */
    @Transactional
    public UsuarioResponse registrar(RegisterRequest r) {
        UsuarioRequest req = new UsuarioRequest(r.run(), r.nombre(), r.apellidos(), r.correo(), r.password(),
                r.fechaNacimiento(), r.region(), r.comuna(), r.direccion(), Role.CLIENTE, true);
        return crear(req);
    }

    /** Creación por el ADMIN (puede elegir cualquier rol). */
    @Transactional
    public UsuarioResponse crear(UsuarioRequest r) {
        String run = Validaciones.normalizarRun(r.run());
        validarDatos(run, r.correo());
        if (!Validaciones.passwordValida(r.password())) {
            throw new BadRequestException("La contraseña debe tener entre 4 y 10 caracteres");
        }
        if (usuarios.existsByRun(run)) {
            throw new ConflictException("Ya existe un usuario con ese RUN");
        }
        if (usuarios.existsByCorreoIgnoreCase(r.correo().trim())) {
            throw new ConflictException("Ya existe un usuario con ese correo");
        }
        Usuario u = new Usuario();
        u.setRun(run);
        u.setCorreo(r.correo().trim().toLowerCase());
        u.setPassword(encoder.encode(r.password()));
        copiarDatos(u, r);
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioRequest r) {
        Usuario u = buscar(id);
        String run = Validaciones.normalizarRun(r.run());
        validarDatos(run, r.correo());
        if (!u.getRun().equals(run) && usuarios.existsByRun(run)) {
            throw new ConflictException("Ya existe un usuario con ese RUN");
        }
        if (!u.getCorreo().equalsIgnoreCase(r.correo().trim())
                && usuarios.existsByCorreoIgnoreCase(r.correo().trim())) {
            throw new ConflictException("Ya existe un usuario con ese correo");
        }
        u.setRun(run);
        u.setCorreo(r.correo().trim().toLowerCase());
        if (r.password() != null && !r.password().isBlank()) {
            if (!Validaciones.passwordValida(r.password())) {
                throw new BadRequestException("La contraseña debe tener entre 4 y 10 caracteres");
            }
            u.setPassword(encoder.encode(r.password()));
        }
        copiarDatos(u, r);
        return UsuarioResponse.de(usuarios.save(u));
    }

    /** Cada usuario puede editar sus propios datos básicos (no su rol ni su correo). */
    @Transactional
    public UsuarioResponse actualizarPerfil(String correo, PerfilRequest r) {
        Usuario u = buscarPorCorreo(correo);
        u.setNombre(r.nombre().trim());
        u.setApellidos(r.apellidos().trim());
        u.setRegion(r.region());
        u.setComuna(r.comuna());
        u.setDireccion(r.direccion());
        if (r.password() != null && !r.password().isBlank()) {
            u.setPassword(encoder.encode(r.password()));
        }
        return UsuarioResponse.de(usuarios.save(u));
    }

    @Transactional
    public void eliminar(Long id, String correoSolicitante) {
        Usuario u = buscar(id);
        if (u.getCorreo().equalsIgnoreCase(correoSolicitante)) {
            throw new BadRequestException("No puedes eliminar tu propia cuenta");
        }
        usuarios.delete(u);
        usuarios.flush(); // si tiene órdenes, falla aquí y responde 409
    }

    // ---------- auxiliares ----------

    private void validarDatos(String runNormalizado, String correo) {
        if (!Validaciones.runValido(runNormalizado)) {
            throw new BadRequestException("El RUN no es válido");
        }
        if (!Validaciones.correoPermitido(correo)) {
            throw new BadRequestException("El correo debe ser @duoc.cl, @profesor.duoc.cl o @gmail.com");
        }
    }

    private void copiarDatos(Usuario u, UsuarioRequest r) {
        u.setNombre(r.nombre().trim());
        u.setApellidos(r.apellidos().trim());
        u.setFechaNacimiento(r.fechaNacimiento());
        u.setRegion(r.region());
        u.setComuna(r.comuna());
        u.setDireccion(r.direccion());
        u.setRol(r.rol());
        u.setActivo(r.activo() == null || r.activo());
    }

    private Usuario buscar(Long id) {
        return usuarios.findById(id)
                .orElseThrow(() -> new NotFoundException("No existe el usuario con id " + id));
    }

    private Usuario buscarPorCorreo(String correo) {
        return usuarios.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new NotFoundException("No existe el usuario " + correo));
    }
}
