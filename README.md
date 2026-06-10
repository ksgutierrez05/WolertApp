[README.md](https://github.com/user-attachments/files/28797323/README.md)
#  WolertApp — Sistema de Alertas y Gestión de Seguridad Ciudadana

> Proyecto de aula — Programación III  
> Universidad Popular del Cesar · Facultad de Ingeniería y Tecnología  
> Docente: Ing. Esp. Alfredo Bautista

**Autoras:**  
Katherine Sofía Gutiérrez Barliza · Maria Cristina Martínez Hinojosa  
Valledupar, Cesar — 2026

---

## 📋 Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Características Principales](#características-principales)
- [Arquitectura del Sistema](#arquitectura-del-sistema)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Roles del Sistema](#roles-del-sistema)
- [Modelo de Dominio](#modelo-de-dominio)
- [Base de Datos](#base-de-datos)
- [Configuración y Ejecución](#configuración-y-ejecución)
- [Patrones de Diseño Aplicados](#patrones-de-diseño-aplicados)
- [Gestión del Proyecto (Git)](#gestión-del-proyecto-git)
- [Equipo de Desarrollo](#equipo-de-desarrollo)

---

## Descripción General

**WolertApp** es una aplicación de escritorio en Java que permite a los ciudadanos de Valledupar reportar alertas de inseguridad con georreferenciación GPS, mientras que las unidades policiales reciben asignaciones automáticas generadas por lógica PL/SQL en Oracle.

El sistema conecta a cuatro actores — ciudadanos, policías, administradores policiales y administradores generales — a través de portales diferenciados con JavaFX, integrando mapas interactivos, chatbot con IA (Gemini API), envío de correos automáticos y exportación de reportes a Excel.

---

## Características Principales

- **Reporte ciudadano georreferenciado:** Registro de alertas con coordenadas GPS, tipo de incidente, arma y medio de transporte del sospechoso.
- **Asignación automática de unidades:** Trigger PL/SQL asigna la unidad policial OPERATIVA más cercana al momento de registrar la alerta.
- **Ciclo de vida completo de alertas:** `PENDIENTE → RECIBIDA → EN_ATENCION → UNIDAD_ASIGNADA → RESUELTA / CANCELADA`
- **Notificaciones automáticas por correo:** Al cambiar el estado de una alerta se notifica al ciudadano; al recibir asignación, a la unidad policial.
- **Activación de alarmas físicas:** Las alertas de prioridad ALTA o MEDIA activan automáticamente alarmas dentro de su radio de cobertura.
- **Mapas interactivos:** Visualización en tiempo real de alertas, unidades y zonas de calor con JXMapViewer.
- **Centro de Operaciones:** Mapa esquemático de la ciudad con zonas peligrosas clickeables para el administrador policial.
- **ChatBot con Gemini API:** Asistente conversacional integrado para el portal ciudadano.
- **Reportes exportables a Excel:** Generados con Apache POI desde el portal del administrador policial.
- **Suscripciones por zona:** Los ciudadanos se suscriben a alertas por tipo de incidente, barrio, comuna o general.

---

## Arquitectura del Sistema

WolertApp implementa una arquitectura en **tres capas** con flujo de dependencias estrictamente descendente:

```
┌────────────────────────────────────────────────────┐
│              CAPA VIEW  (Presentación)              │
│  JavaFX 21 · JXMapViewer · Font Awesome 6 · ~55 clases │
└────────────────────────┬───────────────────────────┘
                         ▼
┌────────────────────────────────────────────────────┐
│             CAPA SERVICE  (Lógica de Negocio)       │
│   17 servicios + 1 interfaz + 4 email senders       │
└────────────────────────┬───────────────────────────┘
                         ▼
┌────────────────────────────────────────────────────┐
│              CAPA DAO  (Acceso a Datos)             │
│  15 DAOs + ConexionDB · JDBC · Oracle PL/SQL        │
└────────────────────────┬───────────────────────────┘
                         ▼
┌────────────────────────────────────────────────────┐
│                  Oracle XE (BD)                     │
│  15 tablas · 9 vistas · 13 triggers PL/SQL          │
└────────────────────────────────────────────────────┘
```

**Paquetes principales:**

| Paquete | Capa | Descripción |
|---|---|---|
| `com.mycompany.wolertappsistemaalertas` | Main | Punto de entrada `WolertAppSistemaAlertas.java` |
| `sistemagestion.model` | Dominio | 13 clases + 1 abstracta + 8 enumeraciones |
| `sistemagestion.dao` | Datos | 15 DAOs + ConexionDB Singleton |
| `sistemagestion.service` | Negocio | 17 servicios + 1 interfaz + 4 email senders |
| `sistemagestion.view` | Presentación | ~55 clases JavaFX |
| `sistemagestion.util` | Utilidades | Validador (10 métodos) |

---

## Tecnologías Utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| Java | JDK 21 | Lenguaje principal |
| JavaFX | 21 | Interfaz gráfica de escritorio |
| Oracle XE | 21c | Motor de base de datos |
| Oracle JDBC | ojdbc8 21.9.0.0 | Conectividad BD |
| JXMapViewer2 | 2.6 | Mapas interactivos |
| Apache POI | 5.2.5 | Exportación a Excel |
| Jakarta Mail | 2.0.1 | Envío de correos SMTP |
| org.json | 20250517 | Integración ChatBot Gemini API |
| Maven | 3.x | Gestión de dependencias y build |

---

## Estructura del Proyecto

```
WolertApp/
├── pom.xml
└── src/
    └── main/
        ├── Resources/
        │   ├── LogoWolertAPP.png
        │   ├── fa-solid-900.ttf
        │   └── [pins e imágenes del mapa]
        └── java/
            ├── com/mycompany/wolertappsistemaalertas/
            │   └── WolertAppSistemaAlertas.java       ← Main
            └── sistemagestion/
                ├── model/
                │   ├── Persona.java                   ← abstracta
                │   ├── Usuario.java
                │   ├── Policia.java
                │   ├── Administrador.java
                │   ├── Alerta.java
                │   ├── AtencionAlerta.java
                │   ├── UnidadPolicial.java
                │   ├── Alarma.java
                │   ├── AsignacionUnidad.java
                │   ├── Notificacion.java
                │   ├── Suscripcion.java
                │   ├── Barrio.java
                │   ├── Comuna.java
                │   ├── Direccion.java
                │   ├── TipoAlerta.java
                │   ├── TipoArma.java
                │   ├── MedioTransporte.java
                │   ├── RolUsuario.java
                │   └── [8 enumeraciones EstadoXxx]
                ├── dao/
                │   ├── ConexionDB.java                ← Singleton
                │   ├── AlertaDAO.java
                │   ├── AtencionAlertaDAO.java
                │   ├── AlarmaDAO.java
                │   ├── AsignacionUnidadDAO.java
                │   ├── UnidadPolicialDAO.java
                │   ├── UsuarioDAO.java
                │   ├── PoliciaDAO.java
                │   ├── NotificacionDAO.java
                │   ├── SuscripcionDAO.java
                │   ├── BarrioDAO.java
                │   ├── ComunaDAO.java
                │   ├── TipoAlertaDAO.java
                │   ├── TipoArmaDAO.java
                │   ├── MedioTransporteDAO.java
                │   └── RolUsuarioDAO.java
                ├── service/
                │   ├── IDashboardStatsService.java    ← interfaz
                │   ├── AlertaService.java
                │   ├── AtencionAlertaService.java
                │   ├── AlarmaService.java
                │   ├── AsignacionUnidadService.java
                │   ├── UnidadPolicialService.java
                │   ├── UsuarioService.java
                │   ├── PoliciaService.java
                │   ├── NotificacionService.java
                │   ├── SuscripcionService.java
                │   ├── BarrioService.java
                │   ├── ComunaService.java
                │   ├── TipoAlertaService.java
                │   ├── TipoArmaService.java
                │   ├── MedioTransporteService.java
                │   ├── RolUsuarioService.java
                │   ├── AdminDashboardStatsService.java
                │   ├── UsuarioDashboardStatsService.java
                │   ├── EmailService.java
                │   ├── AlertaEmailSender.java
                │   ├── NotificacionEmailSender.java
                │   └── UsuarioEmailSender.java
                ├── view/
                │   ├── LoginApp.java
                │   ├── UsuarioApp.java
                │   ├── PoliciaApp.java
                │   ├── AdministradorPoliciaApp.java
                │   ├── AdministradorApp.java
                │   ├── MapaZonasPeligrosas.java
                │   ├── MapaAlerta.java
                │   ├── CentroOperacionesPoliciaView.java
                │   ├── ReportesAdminPoliciaView.java
                │   └── [~50 vistas adicionales]
                └── util/
                    └── Validador.java
```

---

## Roles del Sistema

| Rol | Portal | Funcionalidades principales |
|---|---|---|
| **USUARIO** (ciudadano) | `UsuarioApp` | Registrar alertas GPS, consultar estado, suscripciones por zona, ChatBot Gemini |
| **POLICIA** | `PoliciaApp` | Recibir alertas asignadas, registrar atenciones, historial de intervenciones, alarmas activas |
| **ADMIN_POLICIA** | `AdministradorPoliciaApp` | CRUD policías/unidades/alertas/alarmas, mapa de operaciones, exportar reportes Excel |
| **ADMINISTRADOR** | `AdministradorApp` | Gestión ciudadanos, catálogos (barrios, comunas, tipos), notificaciones, reportes estadísticos |

---

## Modelo de Dominio

### Jerarquía de Herencia

```
Persona  (abstracta)
├── Usuario          (ciudadano — id_usuario, rol)
├── Policia          (agente — placa, rango, estadopolicial, unidadpolicial)
└── Administrador    (admin — id_administrador, cargo)
```

### Entidades Principales

| Clase | Descripción |
|---|---|
| `Alerta` | Entidad central. Evento con GPS, tipo, estado y ciclo de vida completo. |
| `AtencionAlerta` | Respuesta policial: policía responsable, estado final, observaciones. |
| `UnidadPolicial` | Grupo policial con coordenadas GPS. Target de asignación automática. |
| `Alarma` | Dispositivo físico con radio de cobertura. Se activa por trigger PL/SQL. |
| `AsignacionUnidad` | Registro de asignación (manual o automática) de unidad a alerta. |
| `Notificacion` | Mensaje automático al ciudadano o policía por cambio de estado. |
| `Suscripcion` | Preferencia de notificación ciudadana por tipo y zona. |
| `Barrio` / `Comuna` | Unidades geográficas con coordenadas de centroide. |
| `Direccion` | Objeto de valor embebido en `Alerta` y `Persona`. |
| `TipoAlerta` | Catálogo de incidentes con prioridad `ALTA / MEDIA / BAJA`. |

### Enumeraciones (8)

`EstadoAlerta` · `EstadoAlarma` · `EstadoAtencionAlerta` · `EstadoNotificacion` · `EstadoPolicia` · `EstadoSuscripcion` · `EstadoUnidadPolicial` · `EstadoUsuario`

---

## Base de Datos

Esquema Oracle: **`USRWOLERTAPP`**

- **15 tablas** relacionales normalizadas
- **9 vistas** (`VW_ALERTAS_COMPLETA`, `VW_ATENCIONES_COMPLETA`, `VW_POLICIAS`, `VW_ALARMAS`, `VW_ASIGNACIONES`, `VW_SUSCRIPCIONES`, `VW_NOTIFICACIONES`, `VW_BARRIOS`, y otras)
- **13 triggers PL/SQL** de lógica de negocio:
  - `TRG_ID_ALERTA` — secuencia automática de IDs
  - `TRG_ACTIVAR_ALARMAS_CERCANAS` — activa alarmas en radio al registrar alerta ALTA/MEDIA
  - `TRG_ASIGNAR_UNIDAD_AUTOMATICA` — asigna la unidad OPERATIVA más cercana por GPS
  - `TRG_NO_BORRAR_ACTIVOS` — bloquea eliminación de usuarios ACTIVOS
  - y más triggers de ciclo de vida y notificaciones automáticas
- **3 paquetes PL/SQL:** `pkg_alertas`, `pkg_usuarios`, `pkg_catalogos`

**Cadena de conexión:**
```
jdbc:oracle:thin:@10.123.30.162:1521/xepdb1
```
NLS configurado con `NLS_LANGUAGE='SPANISH'` y `NLS_TERRITORY='SPAIN'`.

---

## Configuración y Ejecución

### Prerrequisitos

- JDK 21 instalado y configurado en `PATH`
- Oracle XE 21c ejecutándose localmente o en red
- Maven 3.x

### Pasos

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/ksgutierrez05/WolertApp.git
   cd WolertApp
   ```

2. **Ejecutar el script de base de datos** en Oracle SQL Developer:
   ```
   Script_WolertApp.txt
   ```
   Esto crea el esquema `USRWOLERTAPP` con todas las tablas, vistas, triggers y paquetes PL/SQL.

3. **Ajustar credenciales de BD** en `ConexionDB.java`:
   ```java
   private static final String URL  = "jdbc:oracle:thin:@<IP>:1521/xepdb1";
   private static final String USER = "usrwolertapp";
   private static final String PASS = "<contraseña>";
   ```

4. **Compilar y ejecutar con Maven:**
   ```bash
   mvn clean javafx:run
   ```

5. **Credenciales de acceso iniciales** (creadas por el script):  
   Consultar el script `Script_WolertApp.txt` para los usuarios de prueba de cada rol.

---

## Patrones de Diseño Aplicados

### GRASP

| Patrón | Aplicación en WolertApp |
|---|---|
| **Information Expert** | Cada DAO conoce su tabla/vista y es responsable de mapear sus `ResultSet`. |
| **Creator** | `AlertaService` crea `AsignacionUnidad` como efecto secundario al insertar una alerta. |
| **Controller** | Las clases `*App` (UsuarioApp, PoliciaApp, etc.) coordinan la navegación de vistas. |
| **Low Coupling** | Las capas solo dependen hacia abajo; View → Service → DAO → BD. |
| **High Cohesion** | Cada clase tiene una única responsabilidad bien definida. |
| **Polymorphism** | `IDashboardStatsService` con implementaciones `AdminDashboardStatsService` y `UsuarioDashboardStatsService`. |
| **Pure Fabrication** | `ConexionDB` — clase infraestructural sin equivalente en el dominio. |
| **Indirection** | La capa Service desacopla View del DAO; `AlertaEmailSender` desacopla el envío de correo del servicio. |
| **Protected Variations** | `NavigationRegistry` centraliza la navegación de vistas aislando cambios en la UI. |

### SOLID

| Principio | Aplicación |
|---|---|
| **SRP** | `AlertaService` gestiona alertas; `AlertaEmailSender` envía correos. Responsabilidades separadas. |
| **OCP** | Nuevos roles de dashboard implementan `IDashboardStatsService` sin modificar código existente. |
| **LSP** | `Usuario`, `Policia` y `Administrador` son intercambiables donde se espera `Persona`. |
| **ISP** | `IDashboardStatsService` expone solo los métodos necesarios para las estadísticas del dashboard. |
| **DIP** | Las apps de alto nivel dependen de la abstracción `IDashboardStatsService`, no de implementaciones concretas. |

---

## Gestión del Proyecto (Git)

**Repositorio:** https://github.com/ksgutierrez05/WolertApp

### Estrategia de Ramas

| Rama | Propósito |
|---|---|
| `main` | Producción estable — solo recibe merges desde `develop` |
| `develop` | Integración continua — fusión de todas las features |
| `feature/dao` | DAOs, ConexionDB y mapeo de ResultSet |
| `feature/modelo-service` | Modelo de dominio, enumeraciones y capa de servicios |
| `feature/view-1` | LoginApp, UsuarioApp y portal ciudadano |
| `feature/view-2` | PoliciaApp, AdministradorPoliciaApp y mapas |

---

## Equipo de Desarrollo

| Integrante | Rol | Contribuciones principales |
|---|---|---|
| **Katherine Sofía Gutiérrez Barliza** | Frontend / Integradora | Capa View (JavaFX), mapas JXMapViewer, AlertaDAO, resolución de conflictos, exportación Excel |
| **Maria Cristina Martínez Hinojosa** | Backend | Modelo de dominio, capa Service, EmailService, Validador, ChatBot Gemini API |

---

*WolertApp — Universidad Popular del Cesar · Programación III · 2025*
