/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.wolertappsistemaalertas;


import sistemagestion.view.LoginApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sistemagestion.view.MapaAlarmas;
import sistemagestion.view.MapaAlarmasRegistada;
import sistemagestion.view.MapaAlarmasRegistradas;


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
        stage.setResizable(false);
        stage.show();  

        //Temporalmente abre el mapa directo, sin login ni dashboard
        //new MapaAlarmas().mostrar();
       //new MapaAlarmasRegistradas().mostrar();
        //new MapaAlarmasRegistada().mostrar();
    }

    public static void main(String[] args) {
        launch(args);

    }
}