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
public class Suscripcion {
    private int id_suscripcion;
    Usuario usuario;
    Comuna comuna;
    Barrio barrio;
    private boolean activa;
    private LocalDateTime fechahoracreacion;

    public int getId_suscripcion() {
        return id_suscripcion;
    }

    public void setId_suscripcion(int id_suscripcion) {
        this.id_suscripcion = id_suscripcion;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Comuna getComuna() {
        return comuna;
    }

    public void setComuna(Comuna comuna) {
        this.comuna = comuna;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getFechahoracreacion() {
        return fechahoracreacion;
    }

    public void setFechahoracreacion(LocalDateTime fechahoracreacion) {
        this.fechahoracreacion = fechahoracreacion;
    }
    
}
