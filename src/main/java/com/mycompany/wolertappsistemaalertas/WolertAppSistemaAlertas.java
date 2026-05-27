/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.wolertappsistemaalertas;


import sistemagestion.view.LoginApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sistemagestion.model.Usuario;
import sistemagestion.service.AlertaService;
import sistemagestion.view.AdministradorPoliciaApp;
import sistemagestion.view.MapaAlarmas;
import sistemagestion.view.MapaAlarmasRegistada;
import sistemagestion.view.MapaAlarmasRegistradas;
import sistemagestion.view.MapaAlerta;
import sistemagestion.view.MapaUnidadesPoliciales;


/**
 *
 * @author Lenovo
 */
public class WolertAppSistemaAlertas extends Application {

           @Override
    public void start(Stage stage) {
        
       
        LoginApp login = new LoginApp();

        Scene scene = new Scene(login.getView(), 1000, 650);

        stage.setTitle("WolertApp");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show(); 

        //Temporalmente abre el mapa directo, sin login ni dashboard
        //new MapaAlarmas().mostrar();
       //new MapaAlarmasRegistradas().mostrar();
        //new MapaAlarmasRegistada().mostrar();
       // Usuario usuario = new Usuario();

        //new MapaAlerta(null, null, null, null).mostrar();
        //new MapaUnidadesPoliciales().mostrar();
        /*Usuario usuario = new Usuario();

        usuario.setPrimer_nombre("Katherine");
        usuario.setPrimer_apellido("Gutierrez");
        usuario.setUsername("admin");

        // ── Abrir dashboard administrador ──────────────────
        AdministradorPoliciaApp app =
                new AdministradorPoliciaApp(usuario);

        app.show(stage);*/
    
    }

    public static void main(String[] args) {
        launch(args);

    }
}