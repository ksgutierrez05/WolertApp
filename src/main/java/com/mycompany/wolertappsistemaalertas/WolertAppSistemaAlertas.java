/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.wolertappsistemaalertas;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class WolertAppSistemaAlertas extends Application {

    @Override
    public void start(Stage stage) {

        Label texto = new Label("Hola JavaFX");

        Scene scene = new Scene(texto, 400, 200);

        stage.setTitle("WolertApp");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}