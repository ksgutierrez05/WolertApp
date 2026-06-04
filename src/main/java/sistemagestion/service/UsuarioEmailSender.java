/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

/**
 *
 * @author Maria Cristina
 */


import sistemagestion.model.Usuario;
import java.io.InputStream;
import java.util.Base64;

public class UsuarioEmailSender {

    // ── Logo en Base64 (se carga una sola vez) ────────────────
    private static final String LOGO_BASE64 = cargarLogoBase64();

    private static String cargarLogoBase64() {
        try (InputStream is = UsuarioEmailSender.class
                .getResourceAsStream("/LogoWolertAPP.png")) {
            if (is == null) return null;
            return Base64.getEncoder().encodeToString(is.readAllBytes());
        } catch (Exception e) {
            return null;
        }
    }

    private static String logoTag() {
        if (LOGO_BASE64 != null) {
            return "<img src='data:image/png;base64," + LOGO_BASE64 + "' "
                 + "width='54' height='54' "
                 + "style='border-radius:10px;display:block;' "
                 + "alt='WolertApp'/>";
        }
        return "<span style='font-size:28px;'>🐺</span>";
    }

    // ── Correo de suspensión ──────────────────────────────────
    public static void enviarSuspension(Usuario u) {
        if (u == null || u.getCorreo() == null) return;
        String nombre = u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "Usuario";
        String cuerpo = buildHtml(
                "#e53935",          // color header
                "#ffcdd2",          // color subtítulo header
                "Cuenta suspendida",
                "Tu cuenta en WolertApp ha sido <b style='color:#e53935'>suspendida</b> "
                + "por un administrador del sistema.",
                "Si crees que esto es un error, comunícate con el administrador.",
                nombre
        );
        enviar(u.getCorreo(), "⚠️ Tu cuenta ha sido suspendida — WolertApp", cuerpo);
    }

    // ── Correo de reactivación ────────────────────────────────
    public static void enviarReactivacion(Usuario u) {
        if (u == null || u.getCorreo() == null) return;
        String nombre = u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "Usuario";
        String cuerpo = buildHtml(
                "#43a047",          // color header
                "#c8e6c9",          // color subtítulo header
                "Cuenta reactivada",
                "Tu cuenta en WolertApp ha sido <b style='color:#43a047'>reactivada</b>. "
                + "Ya puedes volver a iniciar sesión normalmente.",
                null,               // sin párrafo extra
                nombre
        );
        enviar(u.getCorreo(), "✅ Tu cuenta ha sido reactivada — WolertApp", cuerpo);
    }

    // ── Envío en hilo separado ────────────────────────────────
    private static void enviar(String correo, String asunto, String cuerpo) {
        new Thread(() -> {
            boolean ok = EmailService.enviarCorreo(correo, asunto, cuerpo);
            System.out.println(ok
                    ? "✅ Correo enviado a " + correo
                    : "❌ Error al enviar correo a " + correo);
        }, "email-sender").start();
    }

    // ── HTML reutilizable ─────────────────────────────────────
    private static String buildHtml(String headerColor, String subColor,
            String titulo, String parrafo1, String parrafo2, String nombre) {

        String p2Html = (parrafo2 != null && !parrafo2.isBlank())
                ? "<p style='color:#374151;font-size:14px;margin:0 0 16px;line-height:1.6;'>"
                  + parrafo2 + "</p>"
                : "";

        return """
            <!DOCTYPE html>
            <html>
            <body style="margin:0;padding:0;background:#f4f6fb;font-family:Arial,sans-serif;">
            <div style="max-width:600px;margin:24px auto;background:#f4f6fb;">

              <!-- HEADER -->
              <div style="background:#1f3a56;padding:24px 32px;
                          border-radius:16px 16px 0 0;
                          display:flex;align-items:center;gap:14px;">
                <div style="width:58px;height:58px;
                            background:rgba(255,255,255,0.12);
                            border-radius:12px;overflow:hidden;
                            display:flex;align-items:center;
                            justify-content:center;flex-shrink:0;">
                  %s
                </div>
                <div>
                  <div style="font-size:20px;font-weight:bold;color:white;">
                    WolertApp</div>
                  <div style="font-size:11px;color:#8899bb;margin-top:3px;">
                    Sistema de Alertas Comunitarias</div>
                </div>
              </div>

              <!-- BANNER COLOREADO -->
              <div style="background:%s;padding:16px 32px;">
                <h2 style="color:white;margin:0;font-size:17px;">%s</h2>
                <p style="color:%s;margin:4px 0 0;font-size:12px;">
                  Notificación de cuenta — WolertApp</p>
              </div>

              <!-- CUERPO -->
              <div style="background:white;padding:28px 32px;">
                <p style="font-size:15px;color:#111827;margin:0 0 14px;">
                  Hola, <strong>%s</strong>
                </p>
                <p style="color:#374151;font-size:14px;margin:0 0 16px;line-height:1.6;">
                  %s
                </p>
                %s
                <hr style="border:none;border-top:1px solid #e5e7eb;margin:20px 0"/>
                <p style="color:#6b7280;font-size:12px;margin:0;line-height:1.6;">
                  Este es un mensaje automático de WolertApp.<br/>
                  Por favor no responda este correo.
                </p>
              </div>

              <!-- FOOTER -->
              <div style="background:white;border-top:1px solid #e5e7eb;
                          padding:18px 32px;text-align:center;
                          border-radius:0 0 16px 16px;">
                <p style="font-size:12px;color:#6b7280;margin:0 0 4px;">
                  Recibiste este correo porque tienes una cuenta en WolertApp.
                </p>
                <p style="font-size:12px;color:#6b7280;margin:0 0 8px;">
                  <a href="#" style="color:#6b7280;text-decoration:underline;">
                    Política de privacidad</a>
                </p>
                <p style="font-size:11px;color:#9ca3af;margin:0;">
                  © 2026 WolertApp — Valledupar, Colombia
                </p>
              </div>

            </div>
            </body>
            </html>
            """.formatted(
                logoTag(),      // logo header
                headerColor,    // color banner
                titulo,         // título banner
                subColor,       // color subtítulo banner
                nombre,         // Hola, {nombre}
                parrafo1,       // párrafo principal
                p2Html          // párrafo extra (opcional)
        );
    }
}