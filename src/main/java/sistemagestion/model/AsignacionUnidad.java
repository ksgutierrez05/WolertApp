/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.model;
import java.time.LocalDateTime;

/**
 *
 * @author Maria Cristina
 */
public class AsignacionUnidad {

    private int id_asignacion;
    private Alerta alerta;
    private UnidadPolicial unidadpolicial;
    private String observacion;
    private LocalDateTime fechahoraasignacion;

    public int getId_asignacion() {
        return id_asignacion;
    }

    public void setId_asignacion(int id_asignacion) {
        this.id_asignacion = id_asignacion;
    }

    public Alerta getAlerta() {
        return alerta;
    }

    public void setAlerta(Alerta alerta) {
        this.alerta = alerta;
    }

    public UnidadPolicial getUnidadpolicial() {
        return unidadpolicial;
    }

    public void setUnidadpolicial(UnidadPolicial unidadpolicial) {
        this.unidadpolicial = unidadpolicial;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechahoraasignacion() {
        return fechahoraasignacion;
    }

    public void setFechahoraasignacion(LocalDateTime fechahoraasignacion) {
        this.fechahoraasignacion = fechahoraasignacion;
    }
    
}
