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
public class Alerta {
    
    private int  id_alerta;
    Usuario usuario;
    TipoAlerta tipoalerta;
    Barrio barrio;
    TipoArma tipoarma;
    MedioTransporte mediotransporte;
    Direccion direccion;
    EstadoAlerta estado;
     private LocalDateTime fechaHora;
    private String  descripcion;

    public int getId_alerta() {
        return id_alerta;
    }

    public void setId_alerta(int id_alerta) {
        this.id_alerta = id_alerta;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoAlerta getTipoalerta() {
        return tipoalerta;
    }

    public void setTipoalerta(TipoAlerta tipoalerta) {
        this.tipoalerta = tipoalerta;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
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

    public Direccion getDireccion() {
        return direccion;
    }

    public void setDireccion(Direccion direccion) {
        this.direccion = direccion;
    }

    public EstadoAlerta getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlerta estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    
    
    
    
    
}
