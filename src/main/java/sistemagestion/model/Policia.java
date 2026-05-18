/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.model;

/**
 *
 * @author Maria Cristina
 */
public class Policia extends Persona {
    private int id_policia;
    private String placa;
    private String rango;
    EstadoPolicia estadopolicial;
    private UnidadPolicial unidadpolicial;
    RolUsuario rol;

    public int getId_policia() {
        return id_policia;
    }

    public void setId_policia(int id_policia) {
        this.id_policia = id_policia;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public String getRango() {
        return rango;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public EstadoPolicia getEstadopolicial() {
        return estadopolicial;
    }

    public void setEstadopolicial(EstadoPolicia estadopolicial) {
        this.estadopolicial = estadopolicial;
    }

    public UnidadPolicial getUnidadpolicial() {
        return unidadpolicial;
    }

    public void setUnidadpolicial(UnidadPolicial unidadpolicial) {
        this.unidadpolicial = unidadpolicial;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

}
