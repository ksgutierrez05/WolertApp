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
public class AtencionAlerta {

    private int id_atencion;
    Alerta alerta;
    EstadoAtencionAlerta estado;
    UnidadPolicial unidad;
    private LocalDateTime fechaatencion;
    private String descripcion;
    TipoArma tipoarma;
    MedioTransporte mediotransporte;
    private String observacion;
    Policia policia;

    public Policia getPolicia() {
        return policia;
    }

    public void setPolicia(Policia policia) {
        this.policia = policia;
    }

    public int getId_atencion() {
        return id_atencion;
    }

    public void setId_atencion(int id_atencion) {
        this.id_atencion = id_atencion;
    }

    public Alerta getAlerta() {
        return alerta;
    }

    public void setAlerta(Alerta alerta) {
        this.alerta = alerta;
    }

    public EstadoAtencionAlerta getEstado() {
        return estado;
    }

    public void setEstado(EstadoAtencionAlerta estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaatencion() {
        return fechaatencion;
    }

    public void setFechaatencion(LocalDateTime fechaatencion) {
        this.fechaatencion = fechaatencion;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoArma getTipoarma() {
        return tipoarma;
    }

    public void setTipoarma(TipoArma tipoarma) {
        this.tipoarma = tipoarma;
    }

    public MedioTransporte getMediotransporte() {
        return mediotransporte;
    }

    public void setMediotransporte(MedioTransporte mediotransporte) {
        this.mediotransporte = mediotransporte;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public UnidadPolicial getUnidad() {
        return unidad;
    }

    public void setUnidad(UnidadPolicial unidad) {
        this.unidad = unidad;
    }

}
