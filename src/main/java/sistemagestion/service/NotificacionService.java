/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.NotificacionDAO;
import sistemagestion.model.Notificacion;
import sistemagestion.util.Validador;
import sistemagestion.service.EmailService;

/**
 *
 * @author Maria Cristina
 */
public class NotificacionService {

    private NotificacionDAO notificacionDAO;

    public NotificacionService() throws SQLException {
        notificacionDAO = new NotificacionDAO();
    }

    public boolean insertar(Notificacion n) {

        Validador.validarObjeto(n);
        Validador.validarObjeto(n.getUsuario());
        Validador.validarObjeto(n.getAlerta());
        Validador.validarCampoVacio(n.getMensaje());

        boolean guardado = notificacionDAO.insertar(
                n.getAlerta().getId_alerta(),
                n.getUsuario().getIdentificacion(),
                n.getMensaje()
        );

        // Si se guardó en BD, enviar correo automáticamente
        if (guardado) {
            String correo = n.getCorreodestinatario() != null
                    ? n.getCorreodestinatario()
                    : (n.getUsuario().getCorreo() != null
                    ? n.getUsuario().getCorreo()
                    : null);

            if (correo != null) {
                String asunto = "🔔 Nueva notificación — WolertApp";
                String cuerpo = """
                <div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;
                            border:1px solid #e5e7eb;border-radius:12px;overflow:hidden'>
                  <div style='background:#1565c0;padding:20px;text-align:center'>
                    <h2 style='color:white;margin:0'>🐺 WolertApp</h2>
                    <p style='color:#90caf9;margin:4px 0 0'>Sistema de alertas ciudadanas</p>
                  </div>
                  <div style='padding:24px'>
                    <h3 style='color:#111827'>Nueva notificación</h3>
                    <p style='color:#374151;font-size:15px'>%s</p>
                    <hr style='border:none;border-top:1px solid #e5e7eb;margin:20px 0'>
                    <p style='color:#6b7280;font-size:12px'>
                      Este es un mensaje automático de WolertApp.<br>
                      Por favor no responda este correo.
                    </p>
                  </div>
                </div>
                """.formatted(n.getMensaje());

                // Hilo separado para no bloquear la app
                final String correoDest = correo;
                new Thread(() -> {
                    boolean enviado = EmailService.enviarCorreo(correoDest, asunto, cuerpo);
                    System.out.println(enviado
                            ? "✅ Correo enviado a " + correoDest
                            : "❌ Error al enviar correo a " + correoDest);
                }).start();
            }
        }

        return guardado;
    }

    public List<Notificacion> listar() {
        return notificacionDAO.listar();
    }

    public boolean eliminar(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException();
        }

        return notificacionDAO.eliminar(id);
    }
}