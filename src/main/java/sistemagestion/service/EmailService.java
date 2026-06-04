/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

/**
 *
 * @author Maria Cristina
 */
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailService {

    private static final String REMITENTE = "wolertapp.notificaciones@gmail.com";
    private static final String PASSWORD = "nimnpmfhopequdgp";

    public static boolean enviarCorreo(String destinatario, String asunto, String cuerpo) {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");      // ← cambia socketFactory por esto
        props.put("mail.smtp.user", REMITENTE);   // ← agrega esta línea
        props.put("mail.smtp.password", PASSWORD);    // ← agrega esta línea
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com"); // ← agrega esta línea

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(REMITENTE, PASSWORD);
            }
        });

        try {
            Message mensaje = new MimeMessage(session);
            mensaje.setFrom(new InternetAddress(REMITENTE, "WolertApp Notificaciones"));
            mensaje.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario));
            mensaje.setSubject(asunto);
            mensaje.setContent(cuerpo, "text/html; charset=utf-8");
            Transport.send(mensaje);
            return true;
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
            return false;
        }
    }
}
