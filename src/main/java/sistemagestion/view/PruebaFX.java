/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class PruebaFX {

    public void mostrar(Stage stage) {

        Label texto = new Label("Hola JavaFX");

        Scene scene = new Scene(texto, 400, 200);

        stage.setTitle("WolertApp");
        stage.setScene(scene);
        stage.show();
    }
}
