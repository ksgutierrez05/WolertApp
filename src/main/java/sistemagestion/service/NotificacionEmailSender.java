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
import sistemagestion.model.Notificacion;


public class NotificacionEmailSender {

    private static String logoTag() {
        return "<img src='https://i.imgur.com/TWqPylo.png' "
             + "width='54' height='54' "
             + "style='border-radius:10px;display:block;' "
             + "alt='WolertApp'/>";
    }

    public static void enviar(Notificacion n) {
        String correo = resolverCorreo(n);
        if (correo == null) {
            return;
        }

        String asunto = "🔔 Nueva alerta en tu barrio — WolertApp";
        String cuerpo = buildHtml(n);

        new Thread(() -> {
            boolean ok = EmailService.enviarCorreo(correo, asunto, cuerpo);
            System.out.println(ok
                    ? "✅ Correo enviado a " + correo
                    : "❌ Error al enviar correo a " + correo);
        }, "email-sender").start();
    }

    private static String resolverCorreo(Notificacion n) {
        if (n.getCorreodestinatario() != null) {
            return n.getCorreodestinatario();
        }
        if (n.getUsuario() != null) {
            return n.getUsuario().getCorreo();
        }
        return null;
    }

    private static String safe(String s) {
        return s == null ? "—" : s;
    }

    private static String[] recomendacionesPorTipo(String tipo) {
        if (tipo == null) {
            tipo = "";
        }
        return switch (tipo.toUpperCase().trim()) {
            case "ROBO", "HURTO" ->
                new String[]{
                    "No persigas ni confrontes al sospechoso.",
                    "Llama al 123 o a la línea de emergencias local.",
                    "Anota características físicas y dirección de huida.",
                    "Avisa a tus vecinos para que estén alertas."
                };
            case "SOSPECHOSO", "PERSONA SOSPECHOSA" ->
                new String[]{
                    "Mantén distancia y no lo confrontes.",
                    "Observa y anota características para informar a las autoridades.",
                    "Cierra puertas y ventanas si estás en casa.",
                    "Informa a la policía llamando al 123."
                };
            case "ACCIDENTE" ->
                new String[]{
                    "Llama al 123 o al 132 (Cruz Roja) de inmediato.",
                    "No muevas a las personas lesionadas salvo peligro inminente.",
                    "Señaliza el área para evitar más accidentes.",
                    "Mantén despejada la vía para el ingreso de socorro."
                };
            case "INCENDIO" ->
                new String[]{
                    "Evacúa el área de inmediato por las rutas de escape.",
                    "Llama al 119 (Bomberos) o al 123.",
                    "No uses ascensores durante la evacuación.",
                    "Si hay humo, desplázate agachado para evitar inhalarlo."
                };
            case "VANDALISMO" ->
                new String[]{
                    "No intervengas directamente, llama al 123.",
                    "Documenta con fotos o video desde un lugar seguro.",
                    "Informa a la administración del sector o junta de acción comunal.",
                    "Reporta daños a las autoridades para el registro oficial."
                };
            case "ANIMAL", "ANIMAL PELIGROSO" ->
                new String[]{
                    "Mantén distancia y no intentes capturarlo por tu cuenta.",
                    "Llama al 123 o a la autoridad ambiental local.",
                    "Aleja a niños y mascotas de la zona.",
                    "Avisa a tus vecinos para que eviten el área."
                };
            case "EMERGENCIA MÉDICA" ->
                new String[]{
                    "Llama al 123 o al 132 de inmediato.",
                    "Si sabes RCP, aplícala solo si la persona no respira.",
                    "No administres medicamentos sin indicación médica.",
                    "Mantén a la persona calmada y abrigada hasta que llegue el auxilio."
                };
            case "HOMICIDIO" ->
                new String[]{
                    "No te acerques al área del incidente.",
                    "Llama al 123 de inmediato.",
                    "No toques ni muevas nada en la escena.",
                    "Aleja a otras personas del lugar y espera a las autoridades."
                };
            case "DESASTRE NATURAL", "INUNDACIÓN", "DESLIZAMIENTO" ->
                new String[]{
                    "Evacúa hacia zonas altas y seguras de inmediato.",
                    "Llama al 123 o a la Defensa Civil (144).",
                    "No cruces ríos ni zonas inundadas a pie o en vehículo.",
                    "Desconecta servicios de gas, agua y electricidad si puedes."
                };
            default ->
                new String[]{
                    "Permanece en un lugar seguro.",
                    "Informa a tus vecinos cercanos sobre la situación.",
                    "Contacta a las autoridades si el peligro persiste.",
                    "Llama al 123 ante cualquier emergencia."
                };
        };
    }

    private static String buildHtml(Notificacion n) {

        // ── Datos destinatario ────────────────────────────────
        String nombreUsuario = "Usuario";
        if (n.getUsuario() != null) {
            String pn = safe(n.getUsuario().getPrimer_nombre());
            String pa = safe(n.getUsuario().getPrimer_apellido());
            String nombre = (pn + " " + pa).trim();
            if (!nombre.isEmpty() && !nombre.equals("— —")) {
                nombreUsuario = nombre;
            }
        }

        // ── Datos alerta ──────────────────────────────────────
        Alerta a = n.getAlerta();

        String tipoAlerta = (a != null && a.getTipoalerta() != null) ? safe(a.getTipoalerta().getNombre()) : "—";
        String barrio = (a != null && a.getBarrio() != null) ? safe(a.getBarrio().getNombre()) : "—";
        String descripcion = (a != null && a.getDescripcion() != null) ? a.getDescripcion() : "—";
        String mensaje = n.getMensaje() != null ? n.getMensaje() : "";
        String reportadoPor = "Vecino anónimo";

        if (a != null && a.getUsuario() != null) {
            String rn = safe(a.getUsuario().getPrimer_nombre());
            String ra = safe(a.getUsuario().getPrimer_apellido());
            String nombreR = (rn + " " + ra).trim();
            if (!nombreR.isEmpty() && !nombreR.equals("— —")) {
                reportadoPor = nombreR;
            }
        }

        String estado = (a != null && a.getEstado() != null)
                ? a.getEstado().name().replace("_", " ") : "—";

        String fechaHora = "—";
        if (a != null && a.getFechaHora() != null) {
            fechaHora = a.getFechaHora().format(
                    java.time.format.DateTimeFormatter.ofPattern(
                            "dd/MM/yyyy – hh:mm a", new java.util.Locale("es", "CO")));
        }

        // ── Colores según estado ──────────────────────────────
        String estadoColor = "#6b7280";
        String estadoBg = "#f3f4f6";
        if (a != null && a.getEstado() != null) {
            estadoColor = switch (a.getEstado()) {
                case PENDIENTE ->
                    "#e53935";
                case EN_ATENCION, UNIDAD_ASIGNADA ->
                    "#fb8c00";
                case RESUELTA ->
                    "#43a047";
                default ->
                    "#6b7280";
            };
            estadoBg = switch (a.getEstado()) {
                case PENDIENTE ->
                    "#fff0f0";
                case EN_ATENCION, UNIDAD_ASIGNADA ->
                    "#fff8e1";
                case RESUELTA ->
                    "#e8f5e9";
                default ->
                    "#f3f4f6";
            };
        }

        // ── Recomendaciones ───────────────────────────────────
        String[] recs = recomendacionesPorTipo(tipoAlerta);
        StringBuilder listaRecs = new StringBuilder();
        for (String rec : recs) {
            listaRecs.append("<li style='margin-bottom:4px;'>").append(rec).append("</li>");
        }

        // ── HTML ──────────────────────────────────────────────
        return "<!DOCTYPE html><html><body style='margin:0;padding:0;background:#f4f6fb;font-family:Arial,sans-serif;'>"
                + "<div style='max-width:600px;margin:24px auto;background:#f4f6fb;'>"

                // HEADER
                + "<div style='background:#1f3a56;padding:24px 32px;border-radius:16px 16px 0 0;display:flex;align-items:center;gap:14px;'>"
                + "<div style='width:58px;height:58px;border-radius:12px;overflow:hidden;display:flex;align-items:center;justify-content:center;flex-shrink:0;'>"
                + logoTag()
                + "</div>"
                + "<div>"
                + "<div style='font-size:20px;font-weight:bold;color:white;letter-spacing:0.3px;'>WolertApp</div>"
                + "<div style='font-size:11px;color:#8899bb;margin-top:3px;'>Sistema de Alertas Comunitarias</div>"
                + "</div>"
                + "</div>"

                // BANNER
                + "<div style='background:#fff0f0;border-bottom:1.5px solid #fecaca;padding:13px 32px;display:flex;align-items:center;gap:10px;'>"
                + "<span style='font-size:15px;color:#a32d2d;'>&#9888;</span>"
                + "<span style='font-size:13px;font-weight:bold;color:#a32d2d;'>Nueva alerta registrada en tu barrio</span>"
                + "</div>"

                // CUERPO
                + "<div style='background:white;padding:28px 32px;'>"
                + "<p style='font-size:15px;color:#111827;margin:0 0 6px;'>Hola, <strong>" + nombreUsuario + "</strong></p>"
                + "<p style='font-size:13px;color:#6b7280;margin:0 0 6px;line-height:1.6;'>" + mensaje + "</p>"
                + "<p style='font-size:13px;color:#6b7280;margin:0 0 22px;line-height:1.6;'>Se ha registrado una nueva alerta en tu zona. Te informamos para que tomes las medidas necesarias.</p>"

                // TARJETA ALERTA
                + "<div style='border:1px solid #e5e7eb;border-radius:12px;overflow:hidden;margin-bottom:22px;'>"
                + "<div style='background:#f8fafc;padding:11px 16px;border-bottom:1px solid #e5e7eb;display:flex;align-items:center;justify-content:space-between;'>"
                + "<div style='display:flex;align-items:center;gap:8px;'>"
                + "<div style='width:9px;height:9px;border-radius:50%;background:" + estadoColor + ";flex-shrink:0;'></div>"
                + "<span style='font-size:13px;font-weight:bold;color:#111827;'>" + tipoAlerta + "</span>"
                + "</div>"
                + "<span style='font-size:11px;font-weight:bold;color:" + estadoColor + ";background:" + estadoBg + ";padding:3px 12px;border-radius:20px;'>" + estado + "</span>"
                + "</div>"
                + "<table style='width:100%;border-collapse:collapse;'>"
                + "<tr style='border-bottom:1px solid #f3f4f6;'>"
                + "<td style='padding:10px 16px;font-size:12px;font-weight:bold;color:#6b7280;width:38%;'>Barrio:</td>"
                + "<td style='padding:10px 16px;font-size:12px;color:#111827;'>" + barrio + "</td>"
                + "</tr>"
                + "<tr style='border-bottom:1px solid #f3f4f6;'>"
                + "<td style='padding:10px 16px;font-size:12px;font-weight:bold;color:#6b7280;'>Fecha y hora:</td>"
                + "<td style='padding:10px 16px;font-size:12px;color:#111827;'>" + fechaHora + "</td>"
                + "</tr>"
                + "<tr style='border-bottom:1px solid #f3f4f6;'>"
                + "<td style='padding:10px 16px;font-size:12px;font-weight:bold;color:#6b7280;'>Reportado por:</td>"
                + "<td style='padding:10px 16px;font-size:12px;color:#111827;'>" + reportadoPor + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style='padding:10px 16px;font-size:12px;font-weight:bold;color:#6b7280;vertical-align:top;'>Descripci&oacute;n:</td>"
                + "<td style='padding:10px 16px;font-size:12px;color:#111827;line-height:1.5;'>" + descripcion + "</td>"
                + "</tr>"
                + "</table>"
                + "</div>"

                // RECOMENDACIONES
                + "<div style='background:#f8fafc;border:1px solid #e5e7eb;border-radius:10px;padding:14px 16px;margin-bottom:8px;'>"
                + "<p style='font-size:13px;font-weight:bold;color:#111827;margin:0 0 10px;'>&#8505;&nbsp; Recomendaciones para: " + tipoAlerta + "</p>"
                + "<ul style='margin:0;padding-left:18px;font-size:13px;color:#6b7280;line-height:1.9;'>"
                + listaRecs
                + "</ul>"
                + "</div>"
                + "</div>"

                // FOOTER
                + "<div style='background:white;border-top:1px solid #e5e7eb;padding:18px 32px;text-align:center;border-radius:0 0 16px 16px;'>"
                + "<p style='font-size:12px;color:#6b7280;margin:0 0 4px;'>Recibiste este correo porque est&aacute;s suscrito a alertas de tu barrio.</p>"
                + "<p style='font-size:12px;color:#6b7280;margin:0 0 8px;'>"
                + "<a href='#' style='color:#6b7280;text-decoration:underline;'>Cancelar suscripci&oacute;n</a>"
                + "&nbsp;&middot;&nbsp;"
                + "<a href='#' style='color:#6b7280;text-decoration:underline;'>Pol&iacute;tica de privacidad</a>"
                + "</p>"
                + "<p style='font-size:11px;color:#9ca3af;margin:0;'>&copy; 2026 WolertApp &mdash; Valledupar, Colombia</p>"
                + "</div>"

                + "</div></body></html>";
    }
}