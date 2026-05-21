/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.wolertappsistemaalertas;

import javafx.application.Application;
import javafx.stage.Stage;
import sistemagestion.view.PruebaFX;

public class WolertAppSistemaAlertas extends Application {

    @Override
    public void start(Stage stage) {

        PruebaFX view = new PruebaFX();
        view.mostrar(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}