package pe.edu.upeu.autoandes.repository;

import java.math.BigDecimal;

public interface IngresoPorServicioProjection {

    String getCodigoServicio();

    String getServicio();

    Long getCantidadTotal();

    BigDecimal getImporteTotal();
}
