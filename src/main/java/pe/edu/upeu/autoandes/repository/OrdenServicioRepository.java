package pe.edu.upeu.autoandes.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;

public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {

    long countByVehiculo_Id(Long vehiculoId);

    Optional<OrdenServicio> findTopByVehiculo_IdOrderByFechaCreacionDescIdDesc(Long vehiculoId);

    @Query("""
            select o
            from OrdenServicio o
            join o.vehiculo v
            where (:placa is null or lower(v.placa) = lower(:placa))
              and (:estado is null or o.estado = :estado)
              and (:desde is null or o.fechaCreacion >= :desde)
              and (:hasta is null or o.fechaCreacion < :hasta)
            order by o.fechaCreacion desc
            """)
    List<OrdenServicio> buscar(
            @Param("placa") String placa,
            @Param("estado") EstadoOrden estado,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);

    @Query("""
            select s.codigo as codigoServicio,
                   s.nombre as servicio,
                   sum(d.cantidad) as cantidadTotal,
                   sum(d.subtotal) as importeTotal
            from DetalleOrden d
            join d.servicio s
            join d.orden o
            where o.estado <> :estadoAnulada
              and (:desde is null or o.fechaCreacion >= :desde)
              and (:hasta is null or o.fechaCreacion < :hasta)
            group by s.codigo, s.nombre
            order by sum(d.subtotal) desc
            """)
    List<IngresoPorServicioProjection> ingresosPorServicio(
            @Param("estadoAnulada") EstadoOrden estadoAnulada,
            @Param("desde") OffsetDateTime desde,
            @Param("hasta") OffsetDateTime hasta);
}
