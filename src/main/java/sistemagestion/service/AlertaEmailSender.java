/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

/**
 *
 * @author Maria Cristina
 */
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;

public class AlertaEmailSender {

    private static String logoTag() {
        return "<img src='https://i.imgur.com/TWqPylo.png' "
                + "width='54' height='54' "
                + "style='border-radius:10px;display:block;' "
                + "alt='WolertApp'/>";
    }

    public static void enviarCambioEstado(Alerta a) {
        if (a == null || a.getUsuario() == null) {
            return;
        }
        String correo = a.getUsuario().getCorreo();
        if (correo == null) {
            return;
        }

        String nombre = a.getUsuario().getPrimer_nombre() != null
                ? a.getUsuario().getPrimer_nombre() : "Usuario";
        String tipo = a.getTipoalerta() != null
                ? a.getTipoalerta().getNombre() : "Alerta";
        EstadoAlerta estado = a.getEstado();

        String headerColor = colorPorEstado(estado);
        String subColor = subColorPorEstado(estado);
        String icono = iconoPorEstado(estado);
        String etiqueta = etiquetaPorEstado(estado);
        String descripcion = descripcionPorEstado(estado, tipo);

        String cuerpo = buildHtml(headerColor, subColor, icono + " " + etiqueta,
                descripcion, a.getId_alerta(), tipo, nombre);

        new Thread(() -> {
            boolean ok = EmailService.enviarCorreo(
                    correo,
                    icono + " Estado de tu alerta actualizado — WolertApp",
                    cuerpo
            );
            System.out.println(ok
                    ? "✅ Correo estado enviado a " + correo
                    : "❌ Error al enviar correo a " + correo);
        }, "email-alerta-estado").start();
    }

    // ── Helpers por estado ────────────────────────────────────
    private static String colorPorEstado(EstadoAlerta e) {
        return switch (e) {
            case PENDIENTE ->
                "#f59e0b";
            case RECIBIDA ->
                "#3b82f6";
            case EN_ATENCION ->
                "#8b5cf6";
            case UNIDAD_ASIGNADA ->
                "#0ea5e9";
            case RESUELTA ->
                "#43a047";
            case CANCELADA ->
                "#e53935";
        };
    }

    private static String subColorPorEstado(EstadoAlerta e) {
        return switch (e) {
            case PENDIENTE ->
                "#fef3c7";
            case RECIBIDA ->
                "#dbeafe";
            case EN_ATENCION ->
                "#ede9fe";
            case UNIDAD_ASIGNADA ->
                "#e0f2fe";
            case RESUELTA ->
                "#c8e6c9";
            case CANCELADA ->
                "#ffcdd2";
        };
    }

    private static String iconoPorEstado(EstadoAlerta e) {
        return switch (e) {
            case PENDIENTE ->
                "⏳";
            case RECIBIDA ->
                "📥";
            case EN_ATENCION ->
                "🚨";
            case UNIDAD_ASIGNADA ->
                "🚔";
            case RESUELTA ->
                "✅";
            case CANCELADA ->
                "❌";
        };
    }

    private static String etiquetaPorEstado(EstadoAlerta e) {
        return switch (e) {
            case PENDIENTE ->
                "Alerta pendiente";
            case RECIBIDA ->
                "Alerta recibida";
            case EN_ATENCION ->
                "En atención";
            case UNIDAD_ASIGNADA ->
                "Unidad asignada";
            case RESUELTA ->
                "Alerta resuelta";
            case CANCELADA ->
                "Alerta cancelada";
        };
    }

    private static String descripcionPorEstado(EstadoAlerta e, String tipo) {
        return switch (e) {
            case PENDIENTE ->
                "Tu alerta de tipo <b>" + tipo + "</b> ha sido registrada "
                + "y está <b style='color:#f59e0b'>pendiente</b> de revisión.";
            case RECIBIDA ->
                "Tu alerta de tipo <b>" + tipo + "</b> ha sido "
                + "<b style='color:#3b82f6'>recibida</b> por nuestro equipo "
                + "y será atendida a la brevedad.";
            case EN_ATENCION ->
                "Tu alerta de tipo <b>" + tipo + "</b> está siendo "
                + "<b style='color:#8b5cf6'>atendida</b> en este momento.";
            case UNIDAD_ASIGNADA ->
                "Se ha asignado una <b style='color:#0ea5e9'>unidad</b> "
                + "para atender tu alerta de tipo <b>" + tipo + "</b>.";
            case RESUELTA ->
                "Tu alerta de tipo <b>" + tipo + "</b> ha sido "
                + "<b style='color:#43a047'>resuelta</b> exitosamente.";
            case CANCELADA ->
                "Tu alerta de tipo <b>" + tipo + "</b> ha sido "
                + "<b style='color:#e53935'>cancelada</b>.";
        };
    }

    // ── HTML ──────────────────────────────────────────────────
    private static String buildHtml(String headerColor, String subColor,
            String titulo, String parrafo, int idAlerta, String tipo, String nombre) {
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
                                        border-radius:12px;overflow:hidden;
                                        display:flex;align-items:center;
                                        justify-content:center;flex-shrink:0;">
                  %s
                </div>
                <div>
                  <div style="font-size:20px;font-weight:bold;color:white;">WolertApp</div>
                  <div style="font-size:11px;color:#8899bb;margin-top:3px;">
                    Sistema de Alertas Comunitarias</div>
                </div>
              </div>

              <!-- BANNER -->
              <div style="background:%s;padding:16px 32px;">
                <h2 style="color:white;margin:0;font-size:17px;">%s</h2>
                <p style="color:%s;margin:4px 0 0;font-size:12px;">
                  Actualización de estado — WolertApp</p>
              </div>

              <!-- CUERPO -->
              <div style="background:white;padding:28px 32px;">
                <p style="font-size:15px;color:#111827;margin:0 0 14px;">
                  Hola, <strong>%s</strong>
                </p>
                <p style="color:#374151;font-size:14px;margin:0 0 16px;line-height:1.6;">
                  %s
                </p>

                <!-- Detalle de la alerta -->
                <div style="background:#f9fafb;border:1px solid #e5e7eb;
                            border-radius:8px;padding:14px 18px;margin-bottom:16px;">
                  <p style="margin:0 0 6px;font-size:13px;color:#6b7280;">Detalle</p>
                  <p style="margin:0;font-size:13px;color:#111827;">
                    🆔 ID de alerta: <strong>#%d</strong>
                  </p>
                  <p style="margin:4px 0 0;font-size:13px;color:#111827;">
                    📋 Tipo: <strong>%s</strong>
                  </p>
                </div>

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
                logoTag(),
                headerColor,
                titulo,
                subColor,
                nombre,
                parrafo,
                idAlerta,
                tipo
        );
    }
}
