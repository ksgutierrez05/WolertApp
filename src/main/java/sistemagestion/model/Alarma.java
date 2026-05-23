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
    private int latitud;
    private int longitud;
    private int radio_cobertura;
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

    public int getLatitud() {
        return latitud;
    }

    public void setLatitud(int latitud) {
        this.latitud = latitud;
    }

    public int getLongitud() {
        return longitud;
    }

    public void setLongitud(int longitud) {
        this.longitud = longitud;
    }

    public int getRadio_cobertura() {
        return radio_cobertura;
    }

    public void setRadio_cobertura(int radio_cobertura) {
        this.radio_cobertura = radio_cobertura;
    }
      
    
}
