/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.model;

/**
 *
 * @author Maria Cristina
 */
public class Barrio {
    private int id_barrio;
    private String nombre;
    Comuna comuna;
    private double latitudcentro;
    private double longitudcentro;

    public int getId_barrio() {
        return id_barrio;
    }

    public void setId_barrio(int id_barrio) {
        this.id_barrio = id_barrio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Comuna getComuna() {
        return comuna;
    }

    public void setComuna(Comuna comuna) {
        this.comuna = comuna;
    }

    public double getLatitudcentro() {
        return latitudcentro;
    }

    public void setLatitudcentro(double latitudcentro) {
        this.latitudcentro = latitudcentro;
    }

    public double getLongitudcentro() {
        return longitudcentro;
    }

    public void setLongitudcentro(double longitudcentro) {
        this.longitudcentro = longitudcentro;
    }
    
}
