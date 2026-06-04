/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.wolertappsistemaalertas;

import sistemagestion.view.LoginApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
<<<<<<< HEAD
import sistemagestion.model.Usuario;
import sistemagestion.service.AlertaService;
import sistemagestion.view.MapaAlarmas;
import sistemagestion.view.MapaAlarmasRegistada;
import sistemagestion.view.MapaAlarmasRegistradas;
import sistemagestion.view.MapaAlerta;
import sistemagestion.view.MapaUnidadesPoliciales;
=======
import sistemagestion.service.EmailService;
import sistemagestion.view.MapaView;
>>>>>>> e125b779d85ff5635282a1967118b0f29a6fb153

/**
 *
 * @author Lenovo
 */
public class WolertAppSistemaAlertas extends Application {

    @Override
    public void start(Stage stage) {
<<<<<<< HEAD
        
       
        /*LoginApp login = new LoginApp();
=======

        LoginApp login = new LoginApp();
>>>>>>> e125b779d85ff5635282a1967118b0f29a6fb153

        Scene scene = new Scene(login.getView(), 1000, 650);

        stage.setTitle("WolertApp");
        stage.setScene(scene);
<<<<<<< HEAD
        stage.setResizable(true);
        stage.show();  */

        //Temporalmente abre el mapa directo, sin login ni dashboard
        //new MapaAlarmas().mostrar();
       //new MapaAlarmasRegistradas().mostrar();
        new MapaAlarmasRegistada().mostrar();
        //Usuario usuario = new Usuario();

        //new MapaAlerta(null, null, null, null).mostrar();
        new MapaUnidadesPoliciales().mostrar();
=======
        stage.setResizable(false);
        stage.show();
        // Temporalmente abre el mapa directo, sin login ni dashboard
        //new MapaView().mostrar();

>>>>>>> e125b779d85ff5635282a1967118b0f29a6fb153
    }

    public static void main(String[] args) {
        launch(args);

    }
}
