package pe.edu.upeu.autoandes.security;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class PermisosPorRol {

    private static final Map<String, Set<Permiso>> PERMISOS = Map.of(
            "ADMIN",
            Set.copyOf(EnumSet.allOf(Permiso.class)),
            "ASESOR",
            Set.of(
                    Permiso.SERVICIO_LEER,
                    Permiso.VEHICULO_LEER,
                    Permiso.VEHICULO_ESCRIBIR,
                    Permiso.ORDEN_LEER,
                    Permiso.ORDEN_REGISTRAR,
                    Permiso.ORDEN_ANULAR),
            "JEFE_TALLER",
            Set.of(
                    Permiso.SERVICIO_LEER,
                    Permiso.SERVICIO_ESCRIBIR,
                    Permiso.VEHICULO_LEER,
                    Permiso.ORDEN_LEER,
                    Permiso.ORDEN_REGISTRAR,
                    Permiso.ORDEN_ANULAR,
                    Permiso.REPORTE_VER));

    private PermisosPorRol() {}

    public static Set<Permiso> obtener(Collection<String> roles) {
        Set<Permiso> permisos = EnumSet.noneOf(Permiso.class);
        roles.forEach(rol -> permisos.addAll(PERMISOS.getOrDefault(rol, Set.of())));
        return Set.copyOf(permisos);
    }

    public static Set<String> nombres(Collection<String> roles) {
        Set<String> nombres = new HashSet<>();
        obtener(roles).forEach(permiso -> nombres.add(permiso.name()));
        return Set.copyOf(nombres);
    }
}
