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
    
    
    
    
}
