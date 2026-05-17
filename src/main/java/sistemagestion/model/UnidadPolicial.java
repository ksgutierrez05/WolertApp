/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.model;

/**
 *
 * @author Maria Cristina
 */
public class UnidadPolicial {
    private int id_unidad;
    private String codigo;
    private String tipounidad;
    private String estado;
    private String ubicacionactual;
    Policia policiaresponsable;

    public int getId_unidad() {
        return id_unidad;
    }

    public void setId_unidad(int id_unidad) {
        this.id_unidad = id_unidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getTipounidad() {
        return tipounidad;
    }

    public void setTipounidad(String tipounidad) {
        this.tipounidad = tipounidad;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getUbicacionactual() {
        return ubicacionactual;
    }

    public void setUbicacionactual(String ubicacionactual) {
        this.ubicacionactual = ubicacionactual;
    }

    public Policia getPoliciaresponsable() {
        return policiaresponsable;
    }

    public void setPoliciaresponsable(Policia policiaresponsable) {
        this.policiaresponsable = policiaresponsable;
    }
    
}
