package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "VEHICULO")
@Getter
@Setter
@NoArgsConstructor
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "CLIENTE_ID", nullable = false)
    private Cliente cliente;

    @Column(name = "PLACA", nullable = false, unique = true, length = 7)
    private String placa;

    @Column(name = "MARCA", nullable = false, length = 60)
    private String marca;

    @Column(name = "MODELO", nullable = false, length = 60)
    private String modelo;

    @Column(name = "ANIO", nullable = false)
    private Integer anio;

    @Column(name = "KILOMETRAJE_ACTUAL", nullable = false)
    private Integer kilometrajeActual;

    @OneToMany(mappedBy = "vehiculo")
    private List<OrdenServicio> ordenes = new ArrayList<>();
}
