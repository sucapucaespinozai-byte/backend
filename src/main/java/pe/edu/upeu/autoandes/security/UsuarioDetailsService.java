package pe.edu.upeu.autoandes.security;

import java.util.LinkedHashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.entity.Usuario;
import pe.edu.upeu.autoandes.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
        Set<String> nombresRoles = usuario.getRoles().stream().map(Rol::getNombre).collect(java.util.stream.Collectors.toSet());
        Set<GrantedAuthority> authorities = new LinkedHashSet<>();
        nombresRoles.forEach(rol -> authorities.add(new SimpleGrantedAuthority("ROLE_" + rol)));
        PermisosPorRol.obtener(nombresRoles)
                .forEach(permiso -> authorities.add(new SimpleGrantedAuthority(permiso.name())));

        return User.withUsername(usuario.getUsername())
                .password(usuario.getPasswordHash())
                .authorities(authorities)
                .disabled(!"S".equals(usuario.getActivo()))
                .build();
    }
}
