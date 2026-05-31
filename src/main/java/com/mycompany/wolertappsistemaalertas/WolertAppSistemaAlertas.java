/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package com.mycompany.wolertappsistemaalertas;

import sistemagestion.view.LoginApp;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sistemagestion.service.EmailService;
import sistemagestion.view.MapaView;

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
        // Temporalmente abre el mapa directo, sin login ni dashboard
        //new MapaView().mostrar();
        
        
         boolean enviado = EmailService.enviarCorreo(
            "mariamartinez.joseumar@gmail.com", // pon tu correo personal aquí
            "🚨 Prueba WolertApp",
            "<h2 style='color:#e53935'>¡Funciona!</h2><p>El sistema de notificaciones de WolertApp está activo.</p>"
    );

    if (enviado) {
    System.out.println("✅ Correo enviado correctamente");
    } else {
    System.out.println("❌ Error al enviar");
      }  
    }

    
    
    

    public static void main(String[] args) {
        launch(args);

    }
}
