/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.wolertappsistemaalertas;


import sistemagestion.view.LoginApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sistemagestion.service.EmailService;

//import sistemagestion.view.MapaView;


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
        //new MapaUnidadesPoliciales().mostrar();*/
      
    

        // ── Dashboard administrador ──────────────────
        
      /*Usuario usuario = new Usuario();
        usuario.setPrimer_nombre("Katherine");
        usuario.setPrimer_apellido("Gutierrez");
        usuario.setUsername("admin");

        AdministradorPoliciaApp app =
                new AdministradorPoliciaApp(usuario);

        app.show(stage);*/
        

        // ── Dashboard policía ──────────────────────
        
       /* Usuario policia = new Usuario();
        policia.setPrimer_nombre("Carlos");
        policia.setPrimer_apellido("Ramirez");
        policia.setUsername("cramirezt");

        new PoliciaApp(policia).show(stage);*/
       
        // ── Mapas de prueba ──────────────────────
        //new MapaAlarmas().mostrar();
        //new MapaAlarmasRegistradas().mostrar();
        //new MapaAlarmasRegistada().mostrar();
        //new MapaAlerta(null, null, null, null).mostrar();
        //new MapaUnidadesPoliciales().mostrar();

        //new MApaOperaciones().
        //new MApaOperaciones().


        // Temporalmente abre el mapa directo, sin login ni dashboard
        //new MapaView().mostrar();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
