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
    EstadoPolicia estadopolicia;
    private String rango;
    
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

   
    public RolUsuario getRol() {
        return rol;
    }

    public void setRol(RolUsuario rol) {
        this.rol = rol;
    }

    public EstadoPolicia getEstadopolicia() {
        return estadopolicia;
    }

    public void setEstadopolicia(EstadoPolicia estadopolicia) {
        this.estadopolicia = estadopolicia;
    }
      
    
    
}
