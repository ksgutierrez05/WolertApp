/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.view;

/**
 *
 * @author Maria Cristina
 */

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import javafx.scene.shape.Circle;
import javafx.util.Duration;
import static sistemagestion.view.AppColors.GREEN;
import static sistemagestion.view.AppColors.RED;
import static sistemagestion.view.AppColors.SIDEBAR_ACTIVE;
import static sistemagestion.view.AppColors.SIDEBAR_AVATAR_BG;
import static sistemagestion.view.AppColors.SIDEBAR_BG_END;
import static sistemagestion.view.AppColors.SIDEBAR_BG_START;
import static sistemagestion.view.AppColors.SIDEBAR_HOVER;
import static sistemagestion.view.AppColors.SIDEBAR_NAV_TEXT;
import static sistemagestion.view.AppColors.WHITE;
import static sistemagestion.view.AppDimensions.SIDEBAR_COLLAPSED;
import static sistemagestion.view.AppDimensions.SIDEBAR_EXPANDED;
import static sistemagestion.view.UIFactory.label;


/**
 * Construye el sidebar colapsable compartido por Admin y Usuario.
 *
 * Patrón GRASP: Pure Fabrication — no es un concepto del dominio;
 *               existe para eliminar duplicación entre las dos apps.
 *               Creator — crea los nodos JavaFX del sidebar.
 * Principio SOLID: SRP — única responsabilidad: construir el sidebar.
 *                  OCP — acepta SidebarConfig sin necesidad de subclases.
 *                  DIP — depende de SidebarConfig (abstracción), no de roles concretos.
 */
public class SidebarBuilder {

    // ── Referencias para animación expand/collapse ────────────────
    private VBox sidebarVBox;
    private VBox logoText;
    private Label logoutText;
    private VBox nav;

    /**
     * Construye y retorna el ScrollPane que envuelve el sidebar.
     *
     * @param config Configuración del sidebar (nombre, entradas, logout).
     * @param resourceClass Clase desde la cual cargar recursos (/LogoWolertAPP.png, /fa-solid-900.ttf).
     */
    public ScrollPane build(SidebarConfig config, Class<?> resourceClass) {
        sidebarVBox = new VBox();
        sidebarVBox.setPrefWidth(SIDEBAR_COLLAPSED);
        sidebarVBox.setMinWidth(SIDEBAR_COLLAPSED);
        sidebarVBox.setMaxWidth(SIDEBAR_COLLAPSED);
        sidebarVBox.setMaxHeight(Double.MAX_VALUE);
        sidebarVBox.setFillWidth(true);
        VBox.setVgrow(sidebarVBox, Priority.ALWAYS);
        sidebarVBox.setStyle(
                "-fx-background-color: linear-gradient(to right, "
                + SIDEBAR_BG_START + ", " + SIDEBAR_BG_END + ");");

        sidebarVBox.getChildren().addAll(
                buildLogoBox(config.appSubtitle(), resourceClass),
                buildUserCard(config.userName(), config.userSubInfo()),
                buildNav(config.entries()),
                buildSpacer(),
                buildLogout(config.onLogout())
        );

        sidebarVBox.setOnMouseEntered(e -> setExpanded(true));
        sidebarVBox.setOnMouseExited(e -> setExpanded(false));

        ScrollPane scroll = new ScrollPane(sidebarVBox);
        scroll.setFitToWidth(true);
        scroll.setFitToHeight(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setStyle(
                "-fx-background: " + SIDEBAR_BG_START + ";"
                + "-fx-background-color: " + SIDEBAR_BG_START + ";"
                + "-fx-border-color: transparent; -fx-padding: 0;");
        return scroll;
    }

    // ── Logo ──────────────────────────────────────────────────────
    private HBox buildLogoBox(String subtitle, Class<?> res) {
        HBox box = new HBox(10);
        box.setPadding(new Insets(20, 8, 20, 8));
        box.setAlignment(Pos.CENTER_LEFT);
        javafx.scene.shape.Rectangle clip =
                new javafx.scene.shape.Rectangle(SIDEBAR_EXPANDED, 80);
        box.setClip(clip);

        ImageView logo = new ImageView(
                new Image(res.getResourceAsStream("/LogoWolertAPP.png"),
                        128, 128, true, true));
        logo.setFitWidth(44);
        logo.setFitHeight(44);
        logo.setPreserveRatio(true);
        StackPane logoWrap = new StackPane(logo);
        logoWrap.setPrefSize(44, 44);

        logoText = new VBox(2);
        logoText.setAlignment(Pos.CENTER_LEFT);
        logoText.getChildren().addAll(
                label("WolertApp", 15, WHITE, true),
                label(subtitle, 9, SIDEBAR_NAV_TEXT, false));
        logoText.setVisible(false);
        logoText.setManaged(false);

        box.getChildren().addAll(logoWrap, logoText);
        return box;
    }

    // ── User card ─────────────────────────────────────────────────
    private HBox buildUserCard(String name, String subInfo) {
        HBox card = new HBox(12);
        card.setPadding(new Insets(10, 8, 10, 14));
        card.setAlignment(Pos.CENTER_LEFT);
        card.setStyle("-fx-background-color: rgba(255,255,255,0.08); -fx-background-radius: 12;");

        Circle avatar = new Circle(18, Color.web(SIDEBAR_AVATAR_BG));
        Label avatarLbl = new Label("\uf007");
        avatarLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px; -fx-text-fill:#a8c0dd;");
        StackPane avatarBox = new StackPane(avatar, avatarLbl);
        avatarBox.setPrefSize(36, 36);
        avatarBox.setMinSize(36, 36);
        avatarBox.setMaxSize(36, 36);

        VBox info = new VBox(2);
        HBox statusRow = new HBox(4);
        statusRow.setAlignment(Pos.CENTER_LEFT);
        statusRow.getChildren().addAll(
                new Circle(4, Color.web(GREEN)),
                label("En línea", 10, GREEN, false));
        info.getChildren().addAll(
                label(name, 12, WHITE, true),
                label(subInfo, 9, SIDEBAR_NAV_TEXT, false),
                statusRow);
        card.getChildren().addAll(avatarBox, info);
        return card;
    }

    // ── Nav ───────────────────────────────────────────────────────
    private VBox buildNav(java.util.List<SidebarConfig.NavEntry> entries) {
        nav = new VBox(2);
        nav.setPadding(new Insets(12, 4, 12, 4));
        entries.forEach(e -> nav.getChildren().add(buildNavItem(e)));
        return nav;
    }

    private HBox buildNavItem(SidebarConfig.NavEntry entry) {
        HBox item = new HBox(10);
        item.setPadding(new Insets(10, 12, 10, 12));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setCursor(javafx.scene.Cursor.HAND);
        item.setMaxWidth(Double.MAX_VALUE);
        item.setStyle("-fx-background-radius: 8;");

        item.setOnMouseEntered(e -> {
            if (!item.getStyle().contains(SIDEBAR_ACTIVE))
                item.setStyle("-fx-background-color:" + SIDEBAR_HOVER + ";-fx-background-radius:8;");
        });
        item.setOnMouseExited(e -> {
            if (!item.getStyle().contains(SIDEBAR_ACTIVE))
                item.setStyle("-fx-background-radius: 8;");
        });

        Label iconLbl = new Label(entry.icon());
        iconLbl.setStyle(
                "-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:15px;-fx-text-fill:" + SIDEBAR_NAV_TEXT + ";");
        iconLbl.setMinWidth(28);
        iconLbl.setPrefWidth(28);
        iconLbl.setMaxWidth(28);
        iconLbl.setAlignment(Pos.CENTER);

        Label textLbl = label(entry.text(), 13, SIDEBAR_NAV_TEXT, false);
        textLbl.setMaxWidth(Double.MAX_VALUE);
        textLbl.setVisible(false);
        textLbl.setManaged(false);
        HBox.setHgrow(textLbl, Priority.ALWAYS);

        item.getChildren().addAll(iconLbl, textLbl);

        item.setOnMouseClicked(e -> {
            // Desactivar todos
            nav.getChildren().forEach(node -> {
                if (node instanceof HBox hb) {
                    hb.setStyle("-fx-background-radius: 8;");
                    hb.getChildren().forEach(child -> {
                        if (child instanceof Label l) {
                            if (l.getStyle().contains("Font Awesome")) {
                                l.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                                        + "-fx-font-size:15px;-fx-text-fill:" + SIDEBAR_NAV_TEXT + ";");
                            } else {
                                l.setTextFill(Color.web(SIDEBAR_NAV_TEXT));
                            }
                        }
                    });
                }
            });
            // Activar ítem actual
            item.setStyle("-fx-background-color:" + SIDEBAR_ACTIVE + ";-fx-background-radius:8;");
            iconLbl.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                    + "-fx-font-size:15px;-fx-text-fill:white;");
            textLbl.setTextFill(Color.WHITE);

            entry.action().run();
        });

        return item;
    }

    // ── Spacer ────────────────────────────────────────────────────
    private VBox buildSpacer() {
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    // ── Logout ────────────────────────────────────────────────────
    private HBox buildLogout(Runnable onLogout) {
        HBox logout = new HBox(10);
        logout.setPadding(new Insets(14, 8, 18, 14));
        logout.setAlignment(Pos.CENTER_LEFT);
        logout.setCursor(javafx.scene.Cursor.HAND);
        logout.setStyle("-fx-background-color: transparent;");
        logout.setOnMouseEntered(e -> logout.setStyle(
                "-fx-background-color: rgba(255,255,255,0.15); -fx-background-radius: 8;"));
        logout.setOnMouseExited(e -> logout.setStyle("-fx-background-color: transparent;"));
        logout.setOnMouseClicked(e -> onLogout.run());

        Label icon = new Label("\uf2f5");
        icon.setStyle("-fx-font-family:'Font Awesome 6 Free Solid';"
                + "-fx-font-size:14px;-fx-text-fill:" + RED + ";");
        icon.setMinWidth(28);
        icon.setAlignment(Pos.CENTER);

        logoutText = label("Cerrar sesión", 13, WHITE, true);
        logoutText.setVisible(false);
        logoutText.setManaged(false);

        logout.getChildren().addAll(icon, logoutText);
        return logout;
    }

    // ── Animación expand / collapse ───────────────────────────────
    private void setExpanded(boolean expand) {
        double target = expand ? SIDEBAR_EXPANDED : SIDEBAR_COLLAPSED;
        new Timeline(new KeyFrame(Duration.millis(180),
                new KeyValue(sidebarVBox.prefWidthProperty(), target, Interpolator.EASE_BOTH),
                new KeyValue(sidebarVBox.minWidthProperty(), target, Interpolator.EASE_BOTH),
                new KeyValue(sidebarVBox.maxWidthProperty(), target, Interpolator.EASE_BOTH)
        )).play();

        setVisible(logoText, expand);
        setVisible(logoutText, expand);

        // Textos del nav
        nav.getChildren().forEach(node -> {
            if (node instanceof HBox hb) {
                hb.getChildren().forEach(child -> {
                    if (child instanceof Label l && !l.getStyle().contains("Font Awesome")) {
                        setVisible(l, expand);
                    }
                });
            }
        });

        // Textos del user card
        sidebarVBox.getChildren().forEach(node -> {
            if (node instanceof HBox card) {
                card.getChildren().forEach(child -> {
                    if (child instanceof VBox vb) {
                        vb.getChildren().forEach(c -> {
                            if (c instanceof Label l && !l.getStyle().contains("Font Awesome"))
                                setVisible(l, expand);
                            if (c instanceof HBox row) {
                                row.getChildren().forEach(rc -> {
                                    if (rc instanceof Label rl && !rl.getStyle().contains("Font Awesome"))
                                        setVisible(rl, expand);
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    private void setVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }
}