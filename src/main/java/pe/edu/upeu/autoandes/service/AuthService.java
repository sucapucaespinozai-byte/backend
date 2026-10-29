package pe.edu.upeu.autoandes.service;

import java.util.List;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.LoginRequestDTO;
import pe.edu.upeu.autoandes.dto.LoginResponseDTO;
import pe.edu.upeu.autoandes.dto.UsuarioResponseDTO;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.exception.ResourceNotFoundException;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;
import pe.edu.upeu.autoandes.security.JwtService;
import pe.edu.upeu.autoandes.security.PermisosPorRol;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public AuthService(
            AuthenticationManager authenticationManager,
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username().trim(), request.password()));
        Usuario usuario = buscarUsuario(authentication.getName());
        List<String> roles = nombresRoles(usuario);
        return new LoginResponseDTO(
                jwtService.generarToken(usuario.getUsername()),
                "Bearer",
                jwtService.obtenerExpiracionSegundos(),
                usuario.getNombreCompleto(),
                roles,
                PermisosPorRol.nombres(roles).stream().sorted().toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO usuarioActual(String username) {
        Usuario usuario = buscarUsuario(username);
        List<String> roles = nombresRoles(usuario);
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                "S".equals(usuario.getActivo()),
                roles,
                PermisosPorRol.nombres(roles).stream().sorted().toList());
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el usuario autenticado"));
    }

    private List<String> nombresRoles(Usuario usuario) {
        return usuario.getRoles().stream().map(Rol::getNombre).sorted().toList();
    }
}
