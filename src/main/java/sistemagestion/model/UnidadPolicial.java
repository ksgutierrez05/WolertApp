/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.model;

import java.util.List;

/**
 *
 * @author Maria Cristina
 */
public class UnidadPolicial {

    private int id_unidad;
    private String nombre;
    EstadoUnidadPolicial estado;
    private Barrio barrio;
    private List<Policia> policias;

    public int getId_unidad() {
        return id_unidad;
    }

    public void setId_unidad(int id_unidad) {
        this.id_unidad = id_unidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    

    public EstadoUnidadPolicial getEstado() {
        return estado;
    }

    public void setEstado(EstadoUnidadPolicial estado) {
        this.estado = estado;
    }

    public Barrio getBarrio() {
        return barrio;
    }

    public void setBarrio(Barrio barrio) {
        this.barrio = barrio;
    }

    public List<Policia> getPolicias() {
        return policias;
    }

    public void setPolicias(List<Policia> policias) {
        this.policias = policias;
    }
    
}
