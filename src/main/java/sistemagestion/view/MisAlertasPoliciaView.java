package sistemagestion.view;

/*
 * ══════════════════════════════════════════════════════════════════════════════
 *  MisAlertasPoliciaView  —  Principios GRASP aplicados
 * ══════════════════════════════════════════════════════════════════════════════
 *
 *  GRASP APLICADOS EN ESTE ARCHIVO:
 *
 *  1. INFORMATION EXPERT
 *     ► AlertaBadgeResolver  → conoce las reglas de color/icono por tipo/estado.
 *       La lógica "qué badge muestra una alerta" vive donde está la información
 *       de tipos, no dispersa en la vista.
 *
 *  2. CREATOR
 *     ► AtencionAlertaFactory  → crea instancias de AtencionAlerta.
 *       Cumple Creator porque agrega/contiene los datos necesarios para construir
 *       el objeto (policia, alerta, formulario).
 *
 *  3. CONTROLLER
 *     ► AlertaController  → recibe eventos de la UI (guardar, cambiar estado)
 *       y delega en los servicios. Desacopla la vista de la lógica de negocio.
 *
 *  4. LOW COUPLING
 *     ► La vista solo conoce AlertaController; no llama directamente a
 *       alertaService, atencionService, tipoArmaService ni medioTransporteService.
 *       Un único punto de entrada reduce el acoplamiento.
 *
 *  5. HIGH COHESION
 *     ► Cada clase interna tiene una responsabilidad única y bien definida:
 *       - StyleKit          → estilos CSS centralizados (Pure Fabrication)
 *       - AlertaBadgeResolver → mapeo tipo/estado → presentación
 *       - AtencionAlertaFactory → creación de entidades
 *       - AlertaController  → coordinación de casos de uso
 *       - MisAlertasPoliciaView → construcción del árbol de nodos JavaFX
 *
 *  6. PURE FABRICATION
 *     ► StyleKit  → no tiene equivalente en el dominio del negocio; existe solo
 *       para centralizar los estilos y evitar duplicación en la vista.
 *     ► FechaFormatter y DireccionFormatter → utilidades de presentación puras.
 *
 *  7. POLYMORPHISM
 *     ► EstadoPresentation (enum con comportamiento) reemplaza el switch/case
 *       original de badgeEstado().  Cada constante sabe renderizarse a sí misma.
 *     ► TipoAlertaPresentation hace lo mismo para badgeAlerta().
 *
 *  8. INDIRECTION
 *     ► AlertaController actúa como intermediario entre la vista y los servicios,
 *       evitando dependencia directa Vista → Servicios (desacoplamiento por nivel
 *       de indirección).
 *
 * ══════════════════════════════════════════════════════════════════════════════
 */

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sistemagestion.model.*;
import sistemagestion.service.*;

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: PURE FABRICATION — StyleKit
//  Centraliza TODOS los strings CSS. No pertenece al dominio; existe para
//  eliminar la repetición de literales de estilo a lo largo de la vista.
// ══════════════════════════════════════════════════════════════════════════════
class StyleKit {

    // Colores base
    static final String WHITE      = "#ffffff";
    static final String BG         = "#f4f6fb";
    static final String BLUE       = "#1565c0";
    static final String GREEN      = "#43a047";
    static final String RED        = "#e53935";
    static final String RED_LIGHT  = "#fff0f0";
    static final String ORANGE     = "#fb8c00";
    static final String YELLOW_BG  = "#fffde7";
    static final String GRAY_TEXT  = "#6b7280";
    static final String BORDER     = "#e5e7eb";
    static final String DARK_GRAD  =
            "linear-gradient(from 0% 0% to 100% 0%, #16283d, #1f3a56)";

    // ── Estilos de botón primario ─────────────────────────────────
    static String btnPrimary() {
        return "-fx-background-color: " + DARK_GRAD
                + "; -fx-background-radius: 10; -fx-padding: 0 22; -fx-cursor: hand;";
    }

    static String btnPrimaryHover() {
        return "-fx-background-color: #0d47a1;"
                + "-fx-background-radius: 10; -fx-padding: 0 22; -fx-cursor: hand;";
    }

    // ── Estilo de TextArea ────────────────────────────────────────
    static String textAreaBase() {
        return "-fx-background-color: white; -fx-background-insets: 0;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #CBD5E1; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B;"
                + "-fx-prompt-text-fill: #94A3B8; -fx-padding: 12 14 12 14;"
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
    }

    static String textAreaFocus() {
        return "-fx-background-color: white; -fx-background-insets: 0;"
                + "-fx-background-radius: 12; -fx-border-radius: 12;"
                + "-fx-border-color: #93c5fd; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-text-fill: #1E293B;"
                + "-fx-prompt-text-fill: #94A3B8; -fx-padding: 12 14 12 14;"
                + "-fx-focus-color: transparent; -fx-faint-focus-color: transparent;";
    }

    // ── Estilo de TextField de búsqueda ──────────────────────────
    static String searchFieldBase() {
        return "-fx-background-color: #f8fafc; -fx-background-radius: 10;"
                + "-fx-border-color: " + BORDER + "; -fx-border-radius: 10;"
                + "-fx-border-width: 1.5; -fx-font-size: 13px;"
                + "-fx-text-fill: #1E293B; -fx-prompt-text-fill: #94A3B8;"
                + "-fx-padding: 0 14; -fx-focus-color: transparent;"
                + "-fx-faint-focus-color: transparent;";
    }

    static String searchFieldFocus() {
        return searchFieldBase().replace(BORDER, DARK_GRAD);
    }

    // ── Estilo base de card ───────────────────────────────────────
    static String cardBase() {
        return "-fx-background-color: white; -fx-background-radius: 18;";
    }

    // ── Sombra estándar ───────────────────────────────────────────
    static DropShadow shadow() {
        return new DropShadow(12, 0, 2, Color.web("#0000001a"));
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: POLYMORPHISM + INFORMATION EXPERT — EstadoPresentation
//  Cada constante sabe su propio icono, fondo y color de texto.
//  Reemplaza el switch/case original de badgeEstado().
// ══════════════════════════════════════════════════════════════════════════════
enum EstadoPresentation {

    PENDIENTE       ("\uf017", StyleKit.RED_LIGHT,  StyleKit.RED),
    RECIBIDA        ("\uf058", "#e8f5e9",            StyleKit.GREEN),
    EN_ATENCION     ("\uf0e7", StyleKit.YELLOW_BG,  StyleKit.ORANGE),
    UNIDAD_ASIGNADA ("\uf505", "#e3f2fd",            StyleKit.BLUE),
    RESUELTA        ("\uf058", "#e8f5e9",            StyleKit.GREEN),
    CANCELADA       ("\uf057", "#f3f4f6",            StyleKit.GRAY_TEXT),
    DESCONOCIDO     ("\uf128", "#f3f4f6",            StyleKit.GRAY_TEXT);

    final String icono;
    final String fondoBadge;
    final String colorTexto;

    EstadoPresentation(String icono, String fondoBadge, String colorTexto) {
        this.icono      = icono;
        this.fondoBadge = fondoBadge;
        this.colorTexto = colorTexto;
    }

    /** INFORMATION EXPERT: el enum sabe cómo resolverse desde el modelo. */
    static EstadoPresentation desde(EstadoAlerta estado) {
        if (estado == null) return DESCONOCIDO;
        try {
            return valueOf(estado.name());
        } catch (IllegalArgumentException e) {
            return DESCONOCIDO;
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: POLYMORPHISM + INFORMATION EXPERT — TipoAlertaPresentation
//  Reemplaza el if-else en cadena de badgeAlerta().
//  Cada instancia conoce la regla que la activa y los valores visuales.
// ══════════════════════════════════════════════════════════════════════════════
enum TipoAlertaPresentation {

    ROBO     (n -> n.contains("ROB")    || n.contains("ASALT"),
              "\uf505", StyleKit.RED_LIGHT,  StyleKit.RED),
    HOMICIDIO(n -> n.contains("HOMICID"),
              "\uf071", "#fff0f0",            StyleKit.RED),
    SOSPECHA (n -> n.contains("SOSPECH"),
              "\uf441", "#fef9c3",            "#92400e"),
    ANIMAL   (n -> n.contains("ANIMAL"),
              "\uf6d3", "#ecfdf5",            "#065f46"),
    INCENDIO (n -> n.contains("INCEND"),
              "\uf06d", "#fff7ed",            "#c2410c"),
    RUIDO    (n -> n.contains("RUIDO")  || n.contains("ALTER"),
              "\uf028", "#fffbeb",            "#b45309"),
    MEDICA   (n -> n.contains("MÉDI")   || n.contains("MEDIC"),
              "\uf0fa", "#f0fdf4",            "#15803d"),
    ACCIDENTE(n -> n.contains("ACCID"),
              "\uf5e4", "#eff6ff",            StyleKit.BLUE),
    GENERAL  (n -> true,
              "\uf0f3", "#f3f4f6",            StyleKit.GRAY_TEXT);

    private final java.util.function.Predicate<String> regla;
    final String icono;
    final String fondoBadge;
    final String colorTexto;

    TipoAlertaPresentation(java.util.function.Predicate<String> regla,
                           String icono, String fondo, String color) {
        this.regla      = regla;
        this.icono      = icono;
        this.fondoBadge = fondo;
        this.colorTexto = color;
    }

    /** INFORMATION EXPERT: resuelve la presentación según el nombre del tipo. */
    static TipoAlertaPresentation desde(String nombreTipo) {
        if (nombreTipo == null) return GENERAL;
        String n = nombreTipo.toUpperCase();
        for (TipoAlertaPresentation t : values()) {
            if (t.regla.test(n)) return t;
        }
        return GENERAL;
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: PURE FABRICATION — FechaFormatter
//  Utilidad de presentación sin equivalente en el dominio.
// ══════════════════════════════════════════════════════════════════════════════
class FechaFormatter {

    private FechaFormatter() {}   // Solo métodos estáticos; no instanciar.

    static String formato(LocalDateTime dt) {
        if (dt == null) return "—";
        long mins = java.time.Duration.between(dt, LocalDateTime.now()).toMinutes();
        if (mins < 1)    return "Hace un momento";
        if (mins < 60)   return "Hace " + mins + " min";
        if (mins < 1440) return "Hace " + (mins / 60) + " h";
        return dt.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: PURE FABRICATION — DireccionFormatter
//  Convierte Direccion → String de presentación. Extraída de la vista
//  para mantener alta cohesión: la vista no debería conocer cómo formatear
//  una dirección.
// ══════════════════════════════════════════════════════════════════════════════
class DireccionFormatter {

    private DireccionFormatter() {}

    static String formato(Direccion d) {
        if (d == null) return "—";
        StringBuilder sb = new StringBuilder();
        if (d.getCalle()   != null && !d.getCalle().isBlank())
            sb.append("Calle ").append(d.getCalle()).append(" ");
        if (d.getCarrera() != null && !d.getCarrera().isBlank())
            sb.append("# ").append(d.getCarrera());
        if (d.getCasa()    != null && !d.getCasa().isBlank())
            sb.append(" Casa ").append(d.getCasa());
        return sb.isEmpty() ? "Sin dirección" : sb.toString().trim();
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: CREATOR — AtencionAlertaFactory
//  Crea instancias de AtencionAlerta.  Cumple CREATOR porque:
//   - Agrega/registra la alerta y el policía que atiende.
//   - Contiene los datos necesarios (formulario) para inicializar el objeto.
//  Separarlo de la vista cumple también HIGH COHESION.
// ══════════════════════════════════════════════════════════════════════════════
class AtencionAlertaFactory {

    private AtencionAlertaFactory() {}

    /**
     * Construye un AtencionAlerta listo para persistir.
     *
     * @param alerta          alerta a la que se responde
     * @param descripcion     texto del formulario
     * @param observacion     nota adicional del formulario
     * @param estadoAtencion  valor del combo de estado
     * @param armaSeleccionada nombre del arma elegida (puede ser null / "— Sin arma —")
     * @param transSeleccionado nombre del transporte elegido (puede ser null)
     * @param policia         policía que registra la atención
     * @param usuarioNombre   nombre del usuario para la observación por defecto
     */
    static AtencionAlerta crear(Alerta alerta,
                                String descripcion,
                                String observacion,
                                String estadoAtencion,
                                String armaSeleccionada,
                                String transSeleccionado,
                                Policia policia,
                                String usuarioNombre) {

        AtencionAlerta at = new AtencionAlerta();
        at.setAlerta(alerta);
        at.setDescripcion(descripcion);
        at.setEstado(EstadoAtencionAlerta.valueOf(estadoAtencion));

        // Observación: usa el texto del formulario o un valor por defecto
        String obs = (observacion != null && !observacion.isBlank())
                ? observacion
                : "Registrado por: " + usuarioNombre;
        at.setObservacion(obs);

        // Unidad policial
        UnidadPolicial unidad = (policia != null && policia.getUnidadpolicial() != null)
                ? policia.getUnidadpolicial()
                : new UnidadPolicial();
        if (unidad.getNombre() == null) unidad.setNombre("Sin unidad");
        at.setUnidad(unidad);

        // Tipo de arma — si el combo no tiene selección válida, hereda de la alerta
        if (armaSeleccionada != null && !armaSeleccionada.startsWith("—")) {
            TipoArma ta = new TipoArma();
            ta.setNombre(armaSeleccionada);
            at.setTipoarma(ta);
        } else {
            at.setTipoarma(alerta.getTipoarma());
        }

        // Medio de transporte — igual que arma
        if (transSeleccionado != null && !transSeleccionado.startsWith("—")) {
            MedioTransporte mt = new MedioTransporte();
            mt.setNombre(transSeleccionado);
            at.setMediotransporte(mt);
        } else {
            at.setMediotransporte(alerta.getMediotransporte());
        }

        return at;
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  GRASP: CONTROLLER + INDIRECTION — AlertaController
//  Recibe eventos de la UI y los delega a los servicios.
//  Actúa como intermediario (INDIRECTION) entre la vista y la capa de servicio,
//  reduciendo el acoplamiento directo (LOW COUPLING).
// ══════════════════════════════════════════════════════════════════════════════
class AlertaController {

    // Resultado de una operación de guardado (objeto de valor simple)
    record ResultadoGuardado(boolean exito, String mensaje) {}

    private final AlertaService          alertaService;
    private final AtencionAlertaService  atencionService;
    private final TipoArmaService        tipoArmaService;
    private final MedioTransporteService medioTransporteService;
    private final Usuario                usuarioActual;
    private final Policia                policiaActual;

    AlertaController(AlertaService alertaService,
                     AtencionAlertaService atencionService,
                     TipoArmaService tipoArmaService,
                     MedioTransporteService medioTransporteService,
                     Usuario usuarioActual,
                     Policia policiaActual) {
        this.alertaService          = alertaService;
        this.atencionService        = atencionService;
        this.tipoArmaService        = tipoArmaService;
        this.medioTransporteService = medioTransporteService;
        this.usuarioActual          = usuarioActual;
        this.policiaActual          = policiaActual;
    }

    // ── Casos de uso expuestos a la vista ─────────────────────────

    /** Carga alertas filtradas por unidad del policía. */
    List<Alerta> cargarAlertas() {
        if (alertaService == null) return List.of();
        try {
            if (policiaActual != null
                    && policiaActual.getUnidadpolicial() != null
                    && policiaActual.getUnidadpolicial().getNombre() != null) {

                List<Alerta> lista = alertaService.listarPorUnidad(
                        policiaActual.getUnidadpolicial().getNombre());

                System.out.println("===== ALERTAS RECIBIDAS =====");
                lista.forEach(a ->
                        System.out.println("ID: " + a.getId_alerta() + " Estado: " + a.getEstado()));
                System.out.println("============================");
                return lista;
            }
            return alertaService.listar();
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /** Devuelve la primera atención registrada para una alerta (puede ser null). */
    AtencionAlerta obtenerAtencion(int idAlerta) {
        if (atencionService == null) return null;
        try {
            List<AtencionAlerta> lista = atencionService.listarPorAlerta(idAlerta);
            return lista.isEmpty() ? null : lista.get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /** ¿Ya existe una atención para esta alerta? */
    boolean tieneAtencion(int idAlerta) {
        return obtenerAtencion(idAlerta) != null;
    }

    /** Lista de nombres de armas disponibles (con opción vacía inicial). */
    List<String> obtenerArmas() {
        List<String> armas = new ArrayList<>();
        armas.add("— Sin arma —");
        if (tipoArmaService != null)
            tipoArmaService.listar().forEach(a -> armas.add(a.getNombre()));
        return armas;
    }

    /** Lista de nombres de medios de transporte disponibles. */
    List<String> obtenerMedios() {
        List<String> medios = new ArrayList<>();
        medios.add("— Sin transporte —");
        if (medioTransporteService != null)
            medioTransporteService.listar().forEach(m -> medios.add(m.getNombre()));
        return medios;
    }

    /**
     * Guarda la atención: primero actualiza el estado de la alerta,
     * luego inserta la atención.  Devuelve un resultado descriptivo.
     */
    ResultadoGuardado guardarAtencion(Alerta alerta,
                                      String descripcion,
                                      String observacion,
                                      String estadoAlerta,
                                      String estadoAtencion,
                                      String armaSeleccionada,
                                      String transSeleccionado) {
        if (descripcion == null || descripcion.isBlank())
            return new ResultadoGuardado(false, "Debes describir la situación antes de guardar.");

        // Actualizar estado de la alerta
        try {
            if (estadoAlerta != null && alertaService != null)
                alertaService.actualizarEstado(alerta.getId_alerta(), estadoAlerta);
        } catch (Exception ex) {
            return new ResultadoGuardado(false, "Error al actualizar estado: " + ex.getMessage());
        }

        // Crear y persistir la atención — usa CREATOR (AtencionAlertaFactory)
        try {
            String nombreUsuario = usuarioActual != null ? usuarioActual.getUsername() : "—";
            AtencionAlerta at = AtencionAlertaFactory.crear(
                    alerta, descripcion, observacion, estadoAtencion,
                    armaSeleccionada, transSeleccionado, policiaActual, nombreUsuario);

            int idPolicia = (policiaActual != null) ? policiaActual.getId_policia() : 0;

            System.out.println("=== DEBUG ATENCION ===");
            System.out.println("ID Alerta: "   + alerta.getId_alerta());
            System.out.println("Unidad: ["      + at.getUnidad().getNombre() + "]");
            System.out.println("ID Policia: "   + idPolicia);
            System.out.println("Descripcion: [" + at.getDescripcion() + "]");
            System.out.println("Estado: ["      + estadoAtencion + "]");
            System.out.println("=====================");

            boolean ok = atencionService.insertar(at, idPolicia);
            return ok
                    ? new ResultadoGuardado(true,  "Atención registrada correctamente.")
                    : new ResultadoGuardado(false, "No se pudo registrar la atención.");

        } catch (Exception ex) {
            return new ResultadoGuardado(false, "Error: " + ex.getMessage());
        }
    }

    /** Nombre de usuario actual para mostrar en la interfaz. */
    String nombreUsuario() {
        return usuarioActual != null ? usuarioActual.getUsername() : "—";
    }

    /** Nombre de la unidad policial actual. */
    String nombreUnidad() {
        return (policiaActual != null
                && policiaActual.getUnidadpolicial() != null
                && policiaActual.getUnidadpolicial().getNombre() != null)
                ? policiaActual.getUnidadpolicial().getNombre()
                : "Sin unidad";
    }

    /**
     * INFORMATION EXPERT: el controlador conoce las opciones de combo;
     * busca la primera que coincida con el valor previo del ciudadano.
     */
    static String resolverValorCombo(String valorCiudadano, List<String> opciones) {
        if (valorCiudadano == null || valorCiudadano.isBlank())
            return opciones.get(0);
        String v = valorCiudadano.trim().toLowerCase();
        return opciones.stream()
                .filter(o -> o.toLowerCase().contains(v) || v.contains(o.toLowerCase()))
                .findFirst()
                .orElse(opciones.get(0));
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  VISTA PRINCIPAL — MisAlertasPoliciaView
//  HIGH COHESION: solo construye el árbol de nodos JavaFX.
//  LOW COUPLING: solo depende de AlertaController (no de los servicios directamente).
// ══════════════════════════════════════════════════════════════════════════════
public class MisAlertasPoliciaView {

    // ── Dependencias ──────────────────────────────────────────────
    private final AlertaController controller;   // INDIRECTION hacia servicios
    private final BorderPane root;

    // ── Estado de la vista ────────────────────────────────────────
    private boolean acordeonAbierto = false;
    private VBox    listaContainer;

    /**
     * Constructor que recibe el Controller ya construido.
     * La vista solo necesita un punto de entrada (LOW COUPLING).
     */
    public MisAlertasPoliciaView(AlertaController controller, BorderPane root) {
        Font.loadFont(getClass().getResourceAsStream("/fa-solid-900.ttf"), 20);
        this.controller = controller;
        this.root       = root;
    }

    /**
     * Constructor de conveniencia que instancia el Controller internamente.
     * Mantiene compatibilidad con el código existente.
     */
    public MisAlertasPoliciaView(Usuario usuarioActual,
                                  Policia policiaActual,
                                  AlertaService alertaService,
                                  AtencionAlertaService atencionService,
                                  TipoArmaService tipoArmaService,
                                  MedioTransporteService medioTransporteService,
                                  BorderPane root) {
        this(new AlertaController(alertaService, atencionService,
                                  tipoArmaService, medioTransporteService,
                                  usuarioActual, policiaActual),
             root);
    }

    // ══════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ══════════════════════════════════════════════════════════════
    public ScrollPane build() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(24));
        content.setStyle("-fx-background-color: " + StyleKit.BG + ";");

        List<Alerta> todasAlertas = controller.cargarAlertas();

        listaContainer = new VBox(16);
        renderAlertas(listaContainer, todasAlertas, null, "");

        content.getChildren().addAll(
                buildTopBar(),
                buildStatsRow(todasAlertas),
                buildFiltros(todasAlertas, listaContainer),
                listaContainer
        );

        // Auto-refresh cada 30 s (solo cuando el acordeón está cerrado)
        Timeline autoRefresh = new Timeline(new KeyFrame(Duration.seconds(30), e -> {
            if (acordeonAbierto) return;
            List<Alerta> nuevas = controller.cargarAlertas();
            listaContainer.getChildren().clear();
            renderAlertas(listaContainer, nuevas, null, "");
        }));
        autoRefresh.setCycleCount(Timeline.INDEFINITE);
        autoRefresh.play();

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle("-fx-background-color: " + StyleKit.BG
                + "; -fx-background: " + StyleKit.BG + ";"
                + "-fx-border-color: transparent; -fx-border-width: 0;");
        return scroll;
    }

    // ══════════════════════════════════════════════════════════════
    //  TOP BAR
    // ══════════════════════════════════════════════════════════════
    private HBox buildTopBar() {
        HBox bar = new HBox();
        bar.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Mis Alertas Asignadas");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        Label sub = new Label("Gestiona y responde a las alertas de tu zona");
        sub.setFont(Font.font("System", 13));
        sub.setTextFill(Color.web(StyleKit.GRAY_TEXT));

        bar.getChildren().add(new VBox(4, title, sub));
        return bar;
    }

    // ══════════════════════════════════════════════════════════════
    //  STATS ROW — INFORMATION EXPERT: recibe la lista ya cargada
    // ══════════════════════════════════════════════════════════════
    private HBox buildStatsRow(List<Alerta> lista) {
        long pendientes = lista.stream().filter(a -> a.getEstado() == EstadoAlerta.PENDIENTE).count();
        long enAtencion = lista.stream().filter(a ->
                a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA).count();
        long resueltas  = lista.stream().filter(a -> a.getEstado() == EstadoAlerta.RESUELTA).count();
        long total      = lista.size();

        HBox row = new HBox(16);
        HBox.setHgrow(row, Priority.ALWAYS);
        row.getChildren().addAll(
                statCard(StyleKit.RED_LIGHT, StyleKit.RED,    "\uf0f3", "Pendientes",   num(pendientes, StyleKit.RED),    "Sin atender"),
                statCard("#fff8e1",           StyleKit.ORANGE, "\uf017", "En atención",  num(enAtencion, StyleKit.ORANGE), "En proceso"),
                statCard("#e8f5e9",           StyleKit.GREEN,  "\uf058", "Resueltas",    num(resueltas,  StyleKit.GREEN),  "Completadas"),
                statCard("#e3f2fd",           StyleKit.BLUE,   "\uf0c9", "Total",        num(total,      StyleKit.BLUE),   "Alertas asignadas")
        );
        return row;
    }

    private Label num(long val, String color) {
        Label l = new Label(String.valueOf(val));
        l.setFont(Font.font("System", FontWeight.BOLD, 36));
        l.setTextFill(Color.web(color));
        return l;
    }

    private VBox statCard(String bgIcono, String acento, String icono,
                          String titulo, Label valorLbl, String sub) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20, 22, 20, 22));
        card.setStyle(StyleKit.cardBase());
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setEffect(StyleKit.shadow());

        StackPane iconWrap = new StackPane();
        iconWrap.setPrefSize(52, 52);
        iconWrap.setMinSize(52, 52);
        iconWrap.setMaxSize(52, 52);
        Rectangle iconBg = new Rectangle(52, 52);
        iconBg.setArcWidth(16); iconBg.setArcHeight(16);
        iconBg.setFill(Color.web(bgIcono));
        Label iconLbl = new Label(icono);
        iconLbl.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: 22px; -fx-text-fill: " + acento + ";");
        iconWrap.getChildren().addAll(iconBg, iconLbl);

        Label tituloLbl = new Label(titulo);
        tituloLbl.setFont(Font.font("System", FontWeight.BOLD, 13));
        tituloLbl.setTextFill(Color.web("#374151"));
        Label subLbl = new Label(sub);
        subLbl.setFont(Font.font("System", 11));
        subLbl.setTextFill(Color.web(StyleKit.GRAY_TEXT));

        HBox top = new HBox(16, iconWrap, new VBox(3, tituloLbl, valorLbl, subLbl));
        top.setAlignment(Pos.CENTER_LEFT);
        card.getChildren().add(top);
        card.setOnMouseEntered(e -> card.setTranslateY(-3));
        card.setOnMouseExited(e  -> card.setTranslateY(0));
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  BARRA DE FILTROS
    // ══════════════════════════════════════════════════════════════
    private VBox buildFiltros(List<Alerta> todas, VBox lista) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16, 20, 16, 20));
        panel.setStyle(StyleKit.cardBase().replace("18;", "16;"));
        panel.setEffect(StyleKit.shadow());

        // Título
        Label filtroFA  = faLabel("\uf0b0", 12, StyleKit.BLUE);
        Label filtroTxt = new Label("Filtrar alertas");
        filtroTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        filtroTxt.setTextFill(Color.web(StyleKit.BLUE));
        HBox titulo = new HBox(8, filtroFA, filtroTxt);
        titulo.setAlignment(Pos.CENTER_LEFT);

        // Búsqueda
        TextField busqueda = new TextField();
        busqueda.setPromptText("\uf002  Buscar por descripción, barrio, tipo...");
        busqueda.setPrefWidth(260);
        busqueda.setPrefHeight(38);
        busqueda.setStyle(StyleKit.searchFieldBase());
        busqueda.focusedProperty().addListener((o, ov, f) ->
                busqueda.setStyle(f ? StyleKit.searchFieldFocus() : StyleKit.searchFieldBase()));

        // Botones pill
        record PillDef(String label, String estado, String icono, String color, String bgActive) {}
        List<PillDef> pills = List.of(
                new PillDef("Todos",        null,              "\uf0c9", StyleKit.DARK_GRAD, StyleKit.WHITE),
                new PillDef("Pendientes",   "PENDIENTE",       "\uf017", StyleKit.RED,       StyleKit.RED_LIGHT),
                new PillDef("Recibidas",    "RECIBIDA",        "\uf058", StyleKit.GREEN,     "#e8f5e9"),
                new PillDef("En atención",  "EN_ATENCION",     "\uf0e7", StyleKit.ORANGE,    StyleKit.YELLOW_BG),
                new PillDef("Unidad asig.", "UNIDAD_ASIGNADA", "\uf505", StyleKit.BLUE,      "#e3f2fd"),
                new PillDef("Resueltas",    "RESUELTA",        "\uf058", StyleKit.GREEN,     "#e8f5e9"),
                new PillDef("Canceladas",   "CANCELADA",       "\uf057", StyleKit.GRAY_TEXT, "#f3f4f6")
        );

        HBox pillRow = new HBox(8);
        pillRow.setAlignment(Pos.CENTER_LEFT);
        final String[] estadoActivo = {null};
        final Button[] activoBtn   = {null};

        for (PillDef pd : pills) {
            Button btn = new Button();
            Label bFA  = faLabel(pd.icono(), 10, StyleKit.GRAY_TEXT);
            Label bTxt = new Label(pd.label());
            bTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
            HBox content = new HBox(5, bFA, bTxt);
            content.setAlignment(Pos.CENTER);
            btn.setGraphic(content);
            btn.setPrefHeight(34);
            btn.setCursor(javafx.scene.Cursor.HAND);

            Runnable inactive = () -> {
                btn.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20;"
                        + "-fx-border-color: " + StyleKit.BORDER
                        + "; -fx-border-radius: 20; -fx-border-width: 1;");
                bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                        + "-fx-font-size: 10px; -fx-text-fill: " + StyleKit.GRAY_TEXT + ";");
                bTxt.setStyle("-fx-text-fill: " + StyleKit.GRAY_TEXT + ";");
            };
            Runnable active = () -> {
                btn.setStyle("-fx-background-color: " + pd.bgActive()
                        + "; -fx-background-radius: 20; -fx-border-color: " + pd.color()
                        + "; -fx-border-radius: 20; -fx-border-width: 1.5;");
                bFA.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                        + "-fx-font-size: 10px; -fx-text-fill: " + pd.color() + ";");
                bTxt.setStyle("-fx-text-fill: " + pd.color() + "; -fx-font-weight: bold;");
            };

            if (pd.estado() == null) { active.run(); activoBtn[0] = btn; }
            else inactive.run();

            btn.setOnMouseEntered(e -> {
                if (activoBtn[0] != btn) btn.setStyle(
                        "-fx-background-color: " + pd.bgActive()
                        + "; -fx-background-radius: 20; -fx-border-color: "
                        + pd.color() + "88; -fx-border-radius: 20; -fx-border-width: 1;");
            });
            btn.setOnMouseExited(e -> {
                if (activoBtn[0] != btn) inactive.run();
            });
            btn.setOnAction(e -> {
                if (activoBtn[0] != null) {
                    Button prev = activoBtn[0];
                    prev.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20;"
                            + "-fx-border-color: " + StyleKit.BORDER
                            + "; -fx-border-radius: 20; -fx-border-width: 1;");
                    if (prev.getGraphic() instanceof HBox ph) {
                        ph.getChildren().forEach(n -> {
                            if (n instanceof Label ll) {
                                if (ll.getStyle().contains("Font Awesome"))
                                    ll.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                                            + "-fx-font-size: 10px; -fx-text-fill: " + StyleKit.GRAY_TEXT + ";");
                                else
                                    ll.setStyle("-fx-text-fill: " + StyleKit.GRAY_TEXT + ";");
                            }
                        });
                    }
                }
                activoBtn[0]    = btn;
                estadoActivo[0] = pd.estado();
                active.run();
                renderAlertas(lista, todas, estadoActivo[0], busqueda.getText().trim());
            });
            pillRow.getChildren().add(btn);
        }

        busqueda.textProperty().addListener((o, ov, nv) ->
                renderAlertas(lista, todas, estadoActivo[0], nv.trim()));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox controles = new HBox(12, busqueda, pillRow, spacer);
        controles.setAlignment(Pos.CENTER_LEFT);

        panel.getChildren().addAll(titulo, controles);
        return panel;
    }

    // ══════════════════════════════════════════════════════════════
    //  RENDER DINÁMICO DE LA LISTA
    // ══════════════════════════════════════════════════════════════
    private void renderAlertas(VBox container, List<Alerta> todas,
                                String estadoFiltro, String textoBusqueda) {
        container.getChildren().clear();

        List<Alerta> filtradas = todas.stream().filter(a -> {
            if (estadoFiltro != null) {
                if (a.getEstado() == null) return false;
                if (!a.getEstado().name().equals(estadoFiltro)) return false;
            }
            if (!textoBusqueda.isEmpty()) {
                String q = textoBusqueda.toLowerCase();
                return coincide(a, q);
            }
            return true;
        }).collect(Collectors.toList());

        if (filtradas.isEmpty()) {
            container.getChildren().add(buildVacioPlaceholder());
        } else {
            filtradas.forEach(a -> container.getChildren().add(buildAlertaCard(a)));
        }
    }

    /** INFORMATION EXPERT: sabe cómo hacer la búsqueda textual en una alerta. */
    private boolean coincide(Alerta a, String q) {
        if (a.getDescripcion()   != null && a.getDescripcion().toLowerCase().contains(q)) return true;
        if (a.getBarrio()        != null && a.getBarrio().getNombre() != null
                && a.getBarrio().getNombre().toLowerCase().contains(q)) return true;
        if (a.getTipoalerta()    != null && a.getTipoalerta().getNombre() != null
                && a.getTipoalerta().getNombre().toLowerCase().contains(q)) return true;
        if (a.getTipoarma()      != null && a.getTipoarma().getNombre() != null
                && a.getTipoarma().getNombre().toLowerCase().contains(q)) return true;
        if (a.getMediotransporte() != null && a.getMediotransporte().getNombre() != null
                && a.getMediotransporte().getNombre().toLowerCase().contains(q)) return true;
        return false;
    }

    private VBox buildVacioPlaceholder() {
        VBox vacio = new VBox(12);
        vacio.setAlignment(Pos.CENTER);
        vacio.setPadding(new Insets(50));
        vacio.setStyle(StyleKit.cardBase());
        vacio.setEffect(StyleKit.shadow());
        Label ico  = faLabel("\uf0b0", 40, StyleKit.GRAY_TEXT);
        Label msg1 = new Label("Sin resultados para este filtro");
        msg1.setFont(Font.font("System", FontWeight.BOLD, 15));
        msg1.setTextFill(Color.web(StyleKit.GRAY_TEXT));
        Label msg2 = new Label("Prueba con otro estado o limpia la búsqueda");
        msg2.setFont(Font.font("System", 12));
        msg2.setTextFill(Color.web(StyleKit.GRAY_TEXT));
        vacio.getChildren().addAll(ico, msg1, msg2);
        return vacio;
    }

    // ══════════════════════════════════════════════════════════════
    //  TARJETA DE ALERTA
    // ══════════════════════════════════════════════════════════════
    private VBox buildAlertaCard(Alerta a) {
        // POLYMORPHISM: TipoAlertaPresentation y EstadoPresentation saben
        // cómo presentarse a sí mismos, sin if/switch en la vista.
        TipoAlertaPresentation tipoPres  = TipoAlertaPresentation.desde(
                a.getTipoalerta() != null ? a.getTipoalerta().getNombre() : null);
        EstadoPresentation     estadoPres = EstadoPresentation.desde(a.getEstado());

        // Override visual si está resuelta o en atención
        String acento, bgLight, iconAvatar;
        if (a.getEstado() == EstadoAlerta.RESUELTA) {
            acento = StyleKit.GREEN;  bgLight = "#e8f5e9";        iconAvatar = "\uf058";
        } else if (a.getEstado() == EstadoAlerta.EN_ATENCION
                || a.getEstado() == EstadoAlerta.UNIDAD_ASIGNADA) {
            acento = StyleKit.ORANGE; bgLight = StyleKit.YELLOW_BG; iconAvatar = "\uf0e7";
        } else {
            acento = tipoPres.colorTexto; bgLight = tipoPres.fondoBadge; iconAvatar = tipoPres.icono;
        }

        String tipo   = a.getTipoalerta()  != null ? a.getTipoalerta().getNombre()  : "Alerta";
        String barrio = a.getBarrio()       != null ? a.getBarrio().getNombre()      : "—";
        String estado = a.getEstado()       != null
                ? a.getEstado().name().replace("_", " ") : "—";

        VBox card = new VBox(0);
        card.setStyle(StyleKit.cardBase());
        card.setEffect(StyleKit.shadow());

        // ── Cabecera ───────────────────────────────────────────────
        HBox cardHeader = new HBox(14);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(18, 20, 14, 20));
        cardHeader.setStyle(
                "-fx-background-color: " + bgLight + ";"
                + "-fx-background-radius: 18 18 0 0;"
                + "-fx-border-color: transparent transparent " + StyleKit.BORDER + " transparent;"
                + "-fx-border-width: 0 0 1 0;");

        StackPane avatarBox = new StackPane();
        Circle avatar = new Circle(24, Color.web(acento));
        Label  avatarFA = faLabel(iconAvatar, 18, StyleKit.WHITE);
        avatarBox.getChildren().addAll(avatar, avatarFA);

        Label tipoFA  = faLabel(iconAvatar, 13, acento);
        Label tipoLbl = new Label(tipo + " en " + barrio);
        tipoLbl.setFont(Font.font("System", FontWeight.BOLD, 15));
        tipoLbl.setTextFill(Color.web("#111827"));
        HBox tipoRow = new HBox(8, tipoFA, tipoLbl);
        tipoRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tipoRow, Priority.ALWAYS);

        // PURE FABRICATION: FechaFormatter y DireccionFormatter
        Label calFA   = faLabel("\uf073", 11, StyleKit.GRAY_TEXT);
        Label fechaTxt = new Label(FechaFormatter.formato(a.getFechaHora()));
        fechaTxt.setFont(Font.font("System", 11));
        fechaTxt.setTextFill(Color.web(StyleKit.GRAY_TEXT));
        Label pinFA   = faLabel("\uf3c5", 11, StyleKit.GRAY_TEXT);
        Label dirTxt  = new Label(DireccionFormatter.formato(a.getDireccion()));
        dirTxt.setFont(Font.font("System", 11));
        dirTxt.setTextFill(Color.web(StyleKit.GRAY_TEXT));
        HBox fechaRow = new HBox(8, calFA, fechaTxt, pinFA, dirTxt);
        fechaRow.setAlignment(Pos.CENTER_LEFT);

        VBox headerInfo = new VBox(4, tipoRow, fechaRow);
        HBox.setHgrow(headerInfo, Priority.ALWAYS);

        // Badge de estado — usa EstadoPresentation (POLYMORPHISM)
        HBox estadoBox = new HBox(5);
        estadoBox.setAlignment(Pos.CENTER);
        estadoBox.setPadding(new Insets(4, 12, 4, 12));
        estadoBox.setStyle("-fx-background-color: " + estadoPres.fondoBadge
                + "; -fx-background-radius: 20;");
        Label estadoFA  = faLabel(estadoPres.icono, 11, estadoPres.colorTexto);
        Label estadoTxt = new Label(estado);
        estadoTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        estadoTxt.setTextFill(Color.web(estadoPres.colorTexto));
        estadoBox.getChildren().addAll(estadoFA, estadoTxt);

        cardHeader.getChildren().addAll(avatarBox, headerInfo, estadoBox);

        // ── Cuerpo ─────────────────────────────────────────────────
        VBox body = new VBox(12);
        body.setPadding(new Insets(16, 20, 8, 20));

        Label descTit = new Label("Descripción");
        descTit.setFont(Font.font("System", FontWeight.BOLD, 11));
        descTit.setTextFill(Color.web(StyleKit.GRAY_TEXT));
        Label descLbl = new Label(a.getDescripcion() != null ? a.getDescripcion() : "Sin descripción");
        descLbl.setFont(Font.font("System", 13));
        descLbl.setTextFill(Color.web("#334155"));
        descLbl.setWrapText(true);

        HBox chips = new HBox(10);
        chips.setAlignment(Pos.CENTER_LEFT);
        if (a.getTipoarma()       != null) chips.getChildren().add(
                chipImagen("/PistolaPin.png", a.getTipoarma().getNombre(),       "#fef2f2", StyleKit.RED));
        if (a.getMediotransporte() != null) chips.getChildren().add(
                chipFA("\uf1b9", a.getMediotransporte().getNombre(), "#e3f2fd",  StyleKit.BLUE));
        if (a.getBarrio()          != null) chips.getChildren().add(
                chipFA("\uf3c5", a.getBarrio().getNombre(),          "#e8f5e9",  StyleKit.GREEN));

        body.getChildren().add(new VBox(4, descTit, descLbl));
        if (!chips.getChildren().isEmpty()) body.getChildren().add(chips);

        card.getChildren().addAll(cardHeader, body, buildAtencionAcordeon(a));
        return card;
    }

    // ══════════════════════════════════════════════════════════════
    //  ACORDEÓN "REGISTRAR ATENCIÓN"
    // ══════════════════════════════════════════════════════════════
    private VBox buildAtencionAcordeon(Alerta a) {
        VBox acordeon = new VBox(0);
        acordeon.setStyle("-fx-border-color: " + StyleKit.BORDER
                + " transparent transparent transparent; -fx-border-width: 1 0 0 0;");

        // Cabecera del acordeón
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(12, 20, 12, 20));
        header.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 0 0 18 18;"
                + " -fx-cursor: hand;");

        Label arrowFA = faLabel("\uf078", 11, StyleKit.BLUE);
        Label editFA  = faLabel("\uf303", 13, StyleKit.BLUE);

        // CONTROLLER: delega la consulta de existencia de atención
        boolean tieneAtencion = controller.tieneAtencion(a.getId_alerta());
        Label headerTxt = new Label(tieneAtencion ? "Ver / Actualizar atención" : "Registrar atención");
        headerTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        headerTxt.setTextFill(Color.web(tieneAtencion ? StyleKit.ORANGE : StyleKit.BLUE));

        HBox resumenChips = new HBox(8);
        resumenChips.setAlignment(Pos.CENTER_LEFT);
        String armaResumen  = a.getTipoarma()        != null ? a.getTipoarma().getNombre()        : null;
        String transResumen = a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : null;
        if (armaResumen  != null) resumenChips.getChildren().add(
                chipImagen("/PistolaPin.png", "Arma: " + armaResumen, "#fef2f2", StyleKit.RED));
        if (transResumen != null) resumenChips.getChildren().add(
                miniChip("\uf1b9", "Transporte: " + transResumen, "#e3f2fd", StyleKit.BLUE));
        if (armaResumen == null && transResumen == null) {
            Label noInfo = new Label("Sin datos previos del ciudadano");
            noInfo.setFont(Font.font("System", 11));
            noInfo.setTextFill(Color.web(StyleKit.GRAY_TEXT));
            resumenChips.getChildren().add(noInfo);
        }

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(editFA, headerTxt, spacer, resumenChips, arrowFA);

        VBox formBody = buildAtencionFormBody(a);
        formBody.setVisible(false);
        formBody.setManaged(false);

        final boolean[] abierto = {false};
        header.setOnMouseClicked(e -> {
            abierto[0]      = !abierto[0];
            acordeonAbierto = abierto[0];
            formBody.setVisible(abierto[0]);
            formBody.setManaged(abierto[0]);
            arrowFA.setText(abierto[0] ? "\uf077" : "\uf078");
            header.setStyle(abierto[0]
                    ? "-fx-background-color: #f0f7ff; -fx-background-radius: 0; -fx-cursor: hand;"
                    : "-fx-background-color: #f8fafc; -fx-background-radius: 0 0 18 18; -fx-cursor: hand;");
        });
        header.setOnMouseEntered(e -> header.setStyle(
                "-fx-background-color: #e8f0fe; -fx-background-radius: "
                + (abierto[0] ? "0" : "0 0 18 18") + "; -fx-cursor: hand;"));
        header.setOnMouseExited(e  -> header.setStyle(
                "-fx-background-color: " + (abierto[0] ? "#f0f7ff" : "#f8fafc")
                + "; -fx-background-radius: " + (abierto[0] ? "0" : "0 0 18 18")
                + "; -fx-cursor: hand;"));

        acordeon.getChildren().addAll(header, formBody);
        return acordeon;
    }

    // ══════════════════════════════════════════════════════════════
    //  FORMULARIO DE ATENCIÓN
    // ══════════════════════════════════════════════════════════════
    private VBox buildAtencionFormBody(Alerta a) {
        // CONTROLLER: obtiene datos existentes sin que la vista toque el servicio
        AtencionAlerta atenExistente = controller.obtenerAtencion(a.getId_alerta());

        VBox box = new VBox(16);
        box.setPadding(new Insets(18, 20, 24, 20));
        box.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 0 0 18 18;");

        // Banner informativo del ciudadano
        String armaOriginal  = a.getTipoarma()        != null ? a.getTipoarma().getNombre()        : null;
        String transOriginal = a.getMediotransporte() != null ? a.getMediotransporte().getNombre() : null;
        if (armaOriginal != null || transOriginal != null) {
            box.getChildren().add(buildBannerCiudadano(armaOriginal, transOriginal));
        }

        // ── Combos de estado ──────────────────────────────────────
        ComboBox<String> estadoAlertaCombo = styledCombo(
                List.of("PENDIENTE","RECIBIDA","EN_ATENCION","UNIDAD_ASIGNADA","RESUELTA","CANCELADA"),
                a.getEstado() != null ? a.getEstado().name() : "PENDIENTE");

        ComboBox<String> estadoAtencionCombo = styledCombo(
                List.of("PENDIENTE","EN_PROCESO","FINALIZADA","CANCELADA"),
                atenExistente != null && atenExistente.getEstado() != null
                        ? atenExistente.getEstado().name() : "EN_PROCESO");

        VBox estadoAlertaBox   = new VBox(6, labelCampo("\uf0f3","Estado de la alerta"),   estadoAlertaCombo);
        VBox estadoAtencionBox = new VBox(6, labelCampo("\uf46d","Estado de la atención"), estadoAtencionCombo);
        HBox combosRow = new HBox(16, estadoAlertaBox, estadoAtencionBox);
        combosRow.setAlignment(Pos.CENTER_LEFT);

        // ── Descripción ───────────────────────────────────────────
        TextArea situacion = new TextArea();
        situacion.setPromptText("Describe la situación, acciones tomadas y resultado...");
        if (atenExistente != null && atenExistente.getDescripcion() != null)
            situacion.setText(atenExistente.getDescripcion());
        situacion.setPrefRowCount(3);
        situacion.setWrapText(true);
        applyTextAreaStyle(situacion);
        VBox situacionBox = new VBox(6, labelCampo("\uf15c","Descripción de la situación"), situacion);

        // ── Combos arma / transporte ──────────────────────────────
        // CONTROLLER: provee las listas (LOW COUPLING)
        List<String> armasDisp  = controller.obtenerArmas();
        List<String> mediosDisp = controller.obtenerMedios();

        String armaPrevia  = atenExistente != null && atenExistente.getTipoarma()        != null
                ? atenExistente.getTipoarma().getNombre()        : armaOriginal;
        String transPrevia = atenExistente != null && atenExistente.getMediotransporte() != null
                ? atenExistente.getMediotransporte().getNombre() : transOriginal;

        ComboBox<String> armaCombo  = styledComboFull(armasDisp,
                AlertaController.resolverValorCombo(armaPrevia,  armasDisp));
        ComboBox<String> transCombo = styledComboFull(mediosDisp,
                AlertaController.resolverValorCombo(transPrevia, mediosDisp));

        VBox armaBox  = new VBox(6, labelCampo("\uf6de","Tipo de arma"),           armaCombo);
        VBox transBox = new VBox(6, labelCampo("\uf1b9","Medio de transporte"),    transCombo);
        HBox.setHgrow(armaBox,  Priority.ALWAYS);
        HBox.setHgrow(transBox, Priority.ALWAYS);
        HBox extrasRow = new HBox(16, armaBox, transBox);
        extrasRow.setAlignment(Pos.CENTER_LEFT);

        // ── Observación ───────────────────────────────────────────
        TextArea obsArea = new TextArea();
        obsArea.setPromptText("Notas adicionales, testigos, coordenadas...");
        if (atenExistente != null && atenExistente.getObservacion() != null)
            obsArea.setText(atenExistente.getObservacion());
        obsArea.setPrefRowCount(2);
        obsArea.setWrapText(true);
        applyTextAreaStyle(obsArea);
        VBox obsBox = new VBox(6, labelCampo("\uf249","Observación adicional"), obsArea);

        // ── Botón guardar ─────────────────────────────────────────
        Label feedback = new Label("");
        feedback.setFont(Font.font("System", 12));
        feedback.setWrapText(true);

        Button guardar = buildGuardarBtn();
        guardar.setOnAction(ev -> {
            // CONTROLLER: toda la lógica de negocio delegada (CONTROLLER + INDIRECTION)
            AlertaController.ResultadoGuardado resultado = controller.guardarAtencion(
                    a,
                    situacion.getText().trim(),
                    obsArea.getText().trim(),
                    estadoAlertaCombo.getValue(),
                    estadoAtencionCombo.getValue(),
                    armaCombo.getValue(),
                    transCombo.getValue()
            );
            if (resultado.exito()) {
                feedback.setTextFill(Color.web(StyleKit.GREEN));
                feedback.setText(resultado.mensaje());
                situacion.clear();
                acordeonAbierto = false;
                // Refresca la lista completa
                List<Alerta> nuevas = controller.cargarAlertas();
                listaContainer.getChildren().clear();
                renderAlertas(listaContainer, nuevas, null, "");
            } else {
                feedback.setTextFill(Color.web(StyleKit.RED));
                feedback.setText(resultado.mensaje());
            }
        });

        HBox btnRow = new HBox(12, guardar, feedback);
        btnRow.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(combosRow, situacionBox, extrasRow, obsBox, btnRow);
        return box;
    }

    // ══════════════════════════════════════════════════════════════
    //  HELPERS DE UI — PURE FABRICATION (StyleKit en acción)
    // ══════════════════════════════════════════════════════════════

    /** Crea un Label con fuente FontAwesome, tamaño y color dados. */
    private Label faLabel(String codepoint, double size, String colorHex) {
        Label l = new Label(codepoint);
        l.setStyle("-fx-font-family: 'Font Awesome 6 Free Solid';"
                + "-fx-font-size: " + (int) size + "px;"
                + "-fx-text-fill: " + colorHex + ";");
        return l;
    }

    private HBox buildBannerCiudadano(String armaOriginal, String transOriginal) {
        HBox banner = new HBox(10);
        banner.setAlignment(Pos.CENTER_LEFT);
        banner.setPadding(new Insets(10, 14, 10, 14));
        banner.setStyle("-fx-background-color: #fffbeb; -fx-background-radius: 10;"
                + "-fx-border-color: #fcd34d; -fx-border-radius: 10; -fx-border-width: 1;");
        Label infoFA  = faLabel("\uf05a", 13, "#b45309");
        Label infoTxt = new Label("El ciudadano reportó: "
                + (armaOriginal  != null ? "arma (" + armaOriginal + ") " : "")
                + (transOriginal != null ? "· transporte (" + transOriginal + ")" : ""));
        infoTxt.setFont(Font.font("System", FontWeight.BOLD, 11));
        infoTxt.setTextFill(Color.web("#92400e"));
        infoTxt.setWrapText(true);
        banner.getChildren().addAll(infoFA, infoTxt);
        return banner;
    }

    private Button buildGuardarBtn() {
        Label guardFA  = faLabel("\uf0c7", 13, StyleKit.WHITE);
        Label guardTxt = new Label("Guardar atención");
        guardTxt.setFont(Font.font("System", FontWeight.BOLD, 13));
        guardTxt.setStyle("-fx-text-fill: white;");
        HBox content = new HBox(8, guardFA, guardTxt);
        content.setAlignment(Pos.CENTER);
        Button btn = new Button();
        btn.setGraphic(content);
        btn.setPrefHeight(42);
        btn.setStyle(StyleKit.btnPrimary());
        btn.setOnMouseEntered(e -> btn.setStyle(StyleKit.btnPrimaryHover()));
        btn.setOnMouseExited(e  -> btn.setStyle(StyleKit.btnPrimary()));
        return btn;
    }

    private void applyTextAreaStyle(TextArea ta) {
        ta.setStyle(StyleKit.textAreaBase());
        ta.focusedProperty().addListener((obs, o, focused) ->
                ta.setStyle(focused ? StyleKit.textAreaFocus() : StyleKit.textAreaBase()));
        ta.skinProperty().addListener((obs, o, skin) -> {
            if (skin == null) return;
            javafx.scene.Node sp = ta.lookup(".scroll-pane");
            if (sp != null) sp.setStyle("-fx-background-color: white; -fx-background-insets: 0;");
            javafx.scene.Node vp = ta.lookup(".scroll-pane .viewport");
            if (vp != null) vp.setStyle("-fx-background-color: white;");
            javafx.scene.Node ct = ta.lookup(".content");
            if (ct != null) ct.setStyle("-fx-background-color: white;");
        });
    }

    private HBox chipFA(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;"
                + "-fx-border-color: " + fg + "44; -fx-border-radius: 20; -fx-border-width: 1;");
        chip.getChildren().addAll(faLabel(iconFA, 11, fg), labelTextoChip(texto, fg));
        return chip;
    }

    private HBox miniChip(String iconFA, String texto, String bg, String fg) {
        HBox chip = new HBox(5);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(3, 8, 3, 8));
        chip.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 14;"
                + "-fx-border-color: " + fg + "55; -fx-border-radius: 14; -fx-border-width: 1;");
        Label ico = faLabel(iconFA, 10, fg);
        Label txt = new Label(texto);
        txt.setFont(Font.font("System", FontWeight.BOLD, 10));
        txt.setTextFill(Color.web(fg));
        chip.getChildren().addAll(ico, txt);
        return chip;
    }

    private HBox chipImagen(String rutaImagen, String texto, String bg, String fg) {
        HBox chip = new HBox(6);
        chip.setAlignment(Pos.CENTER_LEFT);
        chip.setPadding(new Insets(4, 10, 4, 10));
        chip.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 20;"
                + "-fx-border-color: " + fg + "44; -fx-border-radius: 20; -fx-border-width: 1;");
        try {
            java.awt.image.BufferedImage raw = javax.imageio.ImageIO.read(
                    getClass().getResourceAsStream(rutaImagen));
            java.awt.image.BufferedImage recortada = recortarTransparencia(raw);
            javafx.scene.image.ImageView ico = new javafx.scene.image.ImageView(
                    javafx.embed.swing.SwingFXUtils.toFXImage(recortada, null));
            ico.setFitWidth(14);
            ico.setFitHeight(14);
            ico.setPreserveRatio(true);
            chip.getChildren().add(ico);
        } catch (Exception e) {
            chip.getChildren().add(new Label("⚠"));
        }
        chip.getChildren().add(labelTextoChip(texto, fg));
        return chip;
    }

    private Label labelTextoChip(String texto, String fg) {
        Label l = new Label(texto);
        l.setFont(Font.font("System", FontWeight.BOLD, 11));
        l.setTextFill(Color.web(fg));
        return l;
    }

    private HBox labelCampo(String iconFA, String texto) {
        HBox row = new HBox(5, faLabel(iconFA, 11, "#64748b"),
                labelBold(texto, "#64748b", 11));
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Label labelBold(String texto, String color, double size) {
        Label l = new Label(texto);
        l.setFont(Font.font("System", FontWeight.BOLD, size));
        l.setTextFill(Color.web(color));
        return l;
    }

    // ── Combos ────────────────────────────────────────────────────

    private ComboBox<String> styledCombo(List<String> opciones, String valor) {
        ComboBox<String> cb = buildBaseCombo(opciones, valor);
        cb.setPrefWidth(220);
        return cb;
    }

    private ComboBox<String> styledComboFull(List<String> opciones, String valor) {
        ComboBox<String> cb = buildBaseCombo(opciones, valor);
        cb.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(cb, Priority.ALWAYS);
        return cb;
    }

    private ComboBox<String> buildBaseCombo(List<String> opciones, String valor) {
        ComboBox<String> cb = new ComboBox<>();
        cb.getItems().addAll(opciones);
        cb.setValue(valor);
        cb.setPrefHeight(44);
        cb.setStyle("-fx-background-color: white; -fx-background-radius: 10;"
                + "-fx-border-color: " + StyleKit.BORDER
                + "; -fx-border-radius: 10; -fx-border-width: 1.5;"
                + "-fx-font-size: 13px; -fx-cursor: hand;");
        cb.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(item.startsWith("—") ? item : item.replace("_", " "));
                setStyle("-fx-background-color: transparent; -fx-text-fill: #374151;"
                        + "-fx-font-size: 13px; -fx-padding: 9 14 9 14;");
                setOnMouseEntered(e -> setStyle("-fx-background-color: " + StyleKit.BLUE
                        + "; -fx-background-radius: 6; -fx-text-fill: white;"
                        + "-fx-font-size: 13px; -fx-padding: 9 14 9 14;"));
                setOnMouseExited(e  -> setStyle("-fx-background-color: transparent;"
                        + "-fx-text-fill: #374151; -fx-font-size: 13px; -fx-padding: 9 14 9 14;"));
            }
        });
        cb.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.replace("_", " "));
                setStyle("-fx-background-color: transparent; -fx-text-fill: #111827;"
                        + "-fx-font-size: 13px; -fx-padding: 0 14;");
            }
        });
        return cb;
    }

    // ── Utilidad de imagen ────────────────────────────────────────

    private java.awt.image.BufferedImage recortarTransparencia(java.awt.image.BufferedImage img) {
        if (img == null) return null;
        if (!img.getColorModel().hasAlpha()) return img;
        int w = img.getWidth(), h = img.getHeight();
        int top = h, bottom = -1, left = w, right = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (((img.getRGB(x, y) >> 24) & 0xff) > 10) {
                    if (y < top)    top    = y;
                    if (y > bottom) bottom = y;
                    if (x < left)   left   = x;
                    if (x > right)  right  = x;
                }
            }
        }
        if (bottom < 0 || right < 0) return img;
        return img.getSubimage(left, top, right - left + 1, bottom - top + 1);
    }
}
