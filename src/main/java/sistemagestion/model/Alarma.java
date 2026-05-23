/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.model;

/**
 *
 * @author Maria Cristina
 */
public class Alarma {
    
    private int  id_alarma;
    private String nombre;
    private double latitud;
    private double longitud;
    private double radio_cobertura;
    EstadoAlarma estado;

    public EstadoAlarma getEstado() {
        return estado;
    }

    public void setEstado(EstadoAlarma estado) {
        this.estado = estado;
    }

    public int getId_alarma() {
        return id_alarma;
    }

    public void setId_alarma(int id_alarma) {
        this.id_alarma = id_alarma;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getRadio_cobertura() {
        return radio_cobertura;
    }

    public void setRadio_cobertura(double radio_cobertura) {
        this.radio_cobertura = radio_cobertura;
    }

    
      
    
}
