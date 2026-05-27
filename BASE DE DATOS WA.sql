-- ============================================================
--  WolertApp - Sistema de Alertas Comunitarias
--  Script completo Oracle - USRWOLERTAPP
--  Orden: Tablas > Secuencias > Constraints > Vistas > Paquetes > Triggers
-- ============================================================

-- CONNECT usrwolertapp/usrwolertapp@localhost:1521/xepdb1;


-- ============================================================
-- 1. TABLAS - Catálogos base (sin dependencias)
-- ============================================================

CREATE TABLE comunas (
    id_comuna NUMBER,
    nombre    VARCHAR2(50 BYTE)
);

CREATE TABLE roles_usuario (
    id_rol NUMBER,
    nombre VARCHAR2(30 BYTE)
);

CREATE TABLE tipos_alerta (
    id_tipo_alerta NUMBER,
    nombre         VARCHAR2(50 BYTE)
);

CREATE TABLE tipos_arma (
    id_tipo_arma NUMBER,
    nombre       VARCHAR2(30 BYTE)
);

CREATE TABLE medios_transporte (
    id_medio_transporte NUMBER,
    nombre              VARCHAR2(50 BYTE)
);


-- ============================================================
-- 2. TABLAS - Primer nivel de dependencias
-- ============================================================

CREATE TABLE barrios (
    id_barrio NUMBER,
    nombre    VARCHAR2(50 BYTE),
    id_comuna NUMBER
);

CREATE TABLE usuarios (
    id_usuario        NUMBER,
    primer_nombre     VARCHAR2(12 BYTE),
    segundo_nombre    VARCHAR2(12 BYTE),
    primer_apellido   VARCHAR2(20 BYTE),
    segundo_apellido  VARCHAR2(20 BYTE),
    cedula            VARCHAR2(11 BYTE),
    telefono          VARCHAR2(10 BYTE),
    email             VARCHAR2(40 BYTE),
    username          VARCHAR2(15 BYTE),
    password          VARCHAR2(30 BYTE),
    activo            VARCHAR2(15 BYTE),
    id_rol            NUMBER,
    id_barrio         NUMBER,
    calle             VARCHAR2(50 BYTE),
    carrera           VARCHAR2(50 BYTE),
    etapa             VARCHAR2(20 BYTE),
    manzana           VARCHAR2(20 BYTE),
    casa              VARCHAR2(10 BYTE)
);

CREATE TABLE unidades_policiales (
    id_unidad NUMBER,
    nombre    VARCHAR2(50 BYTE),
    estado    VARCHAR2(20 BYTE),
    id_barrio NUMBER,
    longitud  NUMBER,
    latitud   NUMBER
);


-- ============================================================
-- 3. TABLAS - Segundo nivel de dependencias
-- ============================================================

CREATE TABLE alertas (
    id_alerta           NUMBER,
    id_usuario          NUMBER,
    id_tipo_alerta      NUMBER,
    id_barrio           NUMBER,
    id_tipo_arma        NUMBER,
    id_medio_transporte NUMBER,
    estado              VARCHAR2(20 BYTE),
    etapa               VARCHAR2(20 BYTE),
    sector              VARCHAR2(30 BYTE),
    manzana             VARCHAR2(20 BYTE),
    casa                VARCHAR2(10 BYTE),
    calle               VARCHAR2(20 BYTE),
    carrera             VARCHAR2(20 BYTE),
    referencia          VARCHAR2(100 BYTE),
    latitud             NUMBER,
    longitud            NUMBER,
    descripcion         VARCHAR2(200 BYTE),
    fecha               TIMESTAMP(6),
    id_alarma           NUMBER
);

CREATE TABLE alarmas (
    id_alarma        NUMBER,        
    nombre           VARCHAR(30),   
    id_barrio        NUMBER,          
    latitud          NUMBER,         
    longitud         NUMBER,         
    radio_cobertura  NUMBER,         
    estado           VARCHAR2(20))
);

CREATE TABLE policias (
    id_usuario NUMBER,
    placa      VARCHAR2(10 BYTE),
    rango      VARCHAR2(25 BYTE),
    estado     VARCHAR2(15 BYTE),
    id_unidad  NUMBER
);

CREATE TABLE suscripciones (
    id_suscripcion  NUMBER,
    id_usuario      NUMBER,
    id_tipo_alerta  NUMBER,
    id_barrio       NUMBER,
    id_comuna       NUMBER,
    estado          VARCHAR2(15 BYTE)
);


-- ============================================================
-- 4. TABLAS - Tercer nivel de dependencias
-- ============================================================

CREATE TABLE asignaciones_unidad (
    id_asignacion NUMBER,
    id_alerta     NUMBER,
    id_unidad     NUMBER,
    fecha         TIMESTAMP(6),
    observacion   VARCHAR2(200 BYTE)
);

CREATE TABLE atencion_alerta (
    id_atencion                    NUMBER,
    id_alerta                      NUMBER,
    id_unidad                      NUMBER,
    fecha_atencion                 TIMESTAMP(6),
    estado_final                   VARCHAR2(20 BYTE),
    descripcion                    VARCHAR2(300 BYTE),
    id_tipo_arma                   NUMBER,   
    id_medio_transporte            NUMBER,   
    observacion                    VARCHAR2(200 BYTE)
);

CREATE TABLE notificaciones (
    id_notificacion NUMBER,
    id_usuario      NUMBER,
    mensaje         VARCHAR2(150 BYTE),
    fecha           TIMESTAMP(6),
    id_alerta       NUMBER,
    enviada         VARCHAR2(20) DEFAULT 'PENDIENTE'
);


-- ============================================================
-- 5. SECUENCIAS - Auto-incremento para PKs
-- ============================================================

CREATE SEQUENCE seq_comuna            START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_rol               START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_tipo_alerta       START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_tipo_arma         START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_medio_transporte  START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_barrio            START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_usuario           START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_unidad_policial   START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_alerta            START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_policia           START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_suscripcion       START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_asignacion        START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_atencion_alerta   START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_notificacion      START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;
CREATE SEQUENCE seq_alarmas           START WITH 1 INCREMENT BY 1 NOCACHE NOCYCLE;


-- ============================================================
-- 6. PRIMARY KEYS
-- ============================================================

ALTER TABLE comunas           ADD CONSTRAINT pk_id_comuna_comunas                    PRIMARY KEY (id_comuna);
ALTER TABLE roles_usuario     ADD CONSTRAINT pk_id_rol_roles_usuario                 PRIMARY KEY (id_rol);
ALTER TABLE tipos_alerta      ADD CONSTRAINT pk_id_tipo_alerta_tipos_alerta          PRIMARY KEY (id_tipo_alerta);
ALTER TABLE tipos_arma        ADD CONSTRAINT pk_id_tipo_arma_tipos_arma              PRIMARY KEY (id_tipo_arma);
ALTER TABLE medios_transporte ADD CONSTRAINT pk_id_medio_transporte_medios           PRIMARY KEY (id_medio_transporte);
ALTER TABLE barrios           ADD CONSTRAINT pk_id_barrio_barrios                    PRIMARY KEY (id_barrio);
ALTER TABLE usuarios          ADD CONSTRAINT pk_id_usuario_usuarios                  PRIMARY KEY (id_usuario);
ALTER TABLE unidades_policiales ADD CONSTRAINT pk_id_unidad_unidades_policiales      PRIMARY KEY (id_unidad);
ALTER TABLE alertas           ADD CONSTRAINT pk_id_alerta_alertas                    PRIMARY KEY (id_alerta);
ALTER TABLE policias          ADD CONSTRAINT pk_id_usuario_policias                  PRIMARY KEY (id_usuario);
ALTER TABLE suscripciones     ADD CONSTRAINT pk_id_suscripcion_suscripciones         PRIMARY KEY (id_suscripcion);
ALTER TABLE asignaciones_unidad ADD CONSTRAINT pk_id_asignacion_asignaciones_unidad  PRIMARY KEY (id_asignacion);
ALTER TABLE atencion_alerta   ADD CONSTRAINT pk_id_atencion_atencion_alerta          PRIMARY KEY (id_atencion);
ALTER TABLE notificaciones    ADD CONSTRAINT pk_id_notificacion_notificaciones       PRIMARY KEY (id_notificacion);
ALTER TABLE alarmas           ADD CONSTRAINT pk_id_alarma_alarmas                    PRIMARY KEY (id_alarma);

-- ============================================================
-- 7. FOREIGN KEYS
-- ============================================================

-- barrios
ALTER TABLE barrios ADD CONSTRAINT fk_id_comuna_barrios
    FOREIGN KEY (id_comuna) REFERENCES comunas (id_comuna);

-- usuarios
ALTER TABLE usuarios ADD CONSTRAINT fk_id_rol_usuarios
    FOREIGN KEY (id_rol) REFERENCES roles_usuario (id_rol);
ALTER TABLE usuarios ADD CONSTRAINT fk_id_barrio_usuarios
    FOREIGN KEY (id_barrio) REFERENCES barrios (id_barrio);

-- unidades_policiales
ALTER TABLE unidades_policiales ADD CONSTRAINT fk_id_barrio_unidades_policiales
    FOREIGN KEY (id_barrio) REFERENCES barrios (id_barrio);

-- alertas
ALTER TABLE alertas ADD CONSTRAINT fk_id_usuario_alertas
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario);
ALTER TABLE alertas ADD CONSTRAINT fk_id_tipo_alerta_alertas
    FOREIGN KEY (id_tipo_alerta) REFERENCES tipos_alerta (id_tipo_alerta);
ALTER TABLE alertas ADD CONSTRAINT fk_id_barrio_alertas
    FOREIGN KEY (id_barrio) REFERENCES barrios (id_barrio);
ALTER TABLE alertas ADD CONSTRAINT fk_id_tipo_arma_alertas
    FOREIGN KEY (id_tipo_arma) REFERENCES tipos_arma (id_tipo_arma);
ALTER TABLE alertas ADD CONSTRAINT fk_id_medio_transporte_alertas
    FOREIGN KEY (id_medio_transporte) REFERENCES medios_transporte (id_medio_transporte);
ALTER TABLE alertas ADD CONSTRAINT fk_id_alarma_alertas 
    FOREIGN KEY (id_alarma) REFERENCES alarmas (id_alarma);

-- policias
ALTER TABLE policias ADD CONSTRAINT fk_id_usuario_policias
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario);
ALTER TABLE policias ADD CONSTRAINT fk_id_unidad_policias
    FOREIGN KEY (id_unidad) REFERENCES unidades_policiales (id_unidad);

-- suscripciones
ALTER TABLE suscripciones ADD CONSTRAINT fk_id_usuario_suscripciones
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario);
ALTER TABLE suscripciones ADD CONSTRAINT fk_id_tipo_alerta_suscripciones
    FOREIGN KEY (id_tipo_alerta) REFERENCES tipos_alerta (id_tipo_alerta);
ALTER TABLE suscripciones ADD CONSTRAINT fk_id_barrio_suscripciones
    FOREIGN KEY (id_barrio) REFERENCES barrios (id_barrio);
ALTER TABLE suscripciones ADD CONSTRAINT fk_id_comuna_suscripciones
    FOREIGN KEY (id_comuna) REFERENCES comunas (id_comuna);

-- asignaciones_unidad
ALTER TABLE asignaciones_unidad ADD CONSTRAINT fk_id_alerta_asignaciones_unidad
    FOREIGN KEY (id_alerta) REFERENCES alertas (id_alerta);
ALTER TABLE asignaciones_unidad ADD CONSTRAINT fk_id_unidad_asignaciones_unidad
    FOREIGN KEY (id_unidad) REFERENCES unidades_policiales (id_unidad);

-- atencion_alerta
ALTER TABLE atencion_alerta ADD CONSTRAINT fk_id_alerta_atencion_alerta
    FOREIGN KEY (id_alerta) REFERENCES alertas (id_alerta);
ALTER TABLE atencion_alerta ADD CONSTRAINT fk_id_unidad_atencion_alerta
    FOREIGN KEY (id_unidad) REFERENCES unidades_policiales (id_unidad);
ALTER TABLE atencion_alerta ADD CONSTRAINT fk_id_tipo_arma_atencion_alerta
    FOREIGN KEY (id_tipo_arma) REFERENCES tipos_arma (id_tipo_arma);
ALTER TABLE atencion_alerta ADD CONSTRAINT fk_id_medio_transporte_atencion_alerta
    FOREIGN KEY (id_medio_transporte) REFERENCES medios_transporte (id_medio_transporte);

-- notificaciones
ALTER TABLE notificaciones ADD CONSTRAINT fk_id_usuario_notificaciones
    FOREIGN KEY (id_usuario) REFERENCES usuarios (id_usuario);
ALTER TABLE notificaciones ADD CONSTRAINT fk_id_alerta_notificaciones
    FOREIGN KEY (id_alerta) REFERENCES alertas (id_alerta);

--alarmas
ALTER TABLE alarmas ADD CONSTRAINT fk_id_barrio_alarmas FOREIGN KEY (id_barrio) REFERENCES barrios (id_barrio);

-- ============================================================
-- 8. TRIGGERS - Auto-incremento PKs
-- ============================================================

CREATE OR REPLACE TRIGGER trg_id_comunas
BEFORE INSERT ON comunas FOR EACH ROW
BEGIN :NEW.id_comuna := seq_comuna.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_roles_usuario
BEFORE INSERT ON roles_usuario FOR EACH ROW
BEGIN :NEW.id_rol := seq_rol.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_tipos_alerta
BEFORE INSERT ON tipos_alerta FOR EACH ROW
BEGIN :NEW.id_tipo_alerta := seq_tipo_alerta.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_tipos_arma
BEFORE INSERT ON tipos_arma FOR EACH ROW
BEGIN :NEW.id_tipo_arma := seq_tipo_arma.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_medios_transporte
BEFORE INSERT ON medios_transporte FOR EACH ROW
BEGIN :NEW.id_medio_transporte := seq_medio_transporte.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_barrios
BEFORE INSERT ON barrios FOR EACH ROW
BEGIN :NEW.id_barrio := seq_barrio.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_usuarios
BEFORE INSERT ON usuarios FOR EACH ROW
BEGIN :NEW.id_usuario := seq_usuario.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_unidades_policiales
BEFORE INSERT ON unidades_policiales FOR EACH ROW
BEGIN :NEW.id_unidad := seq_unidad_policial.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_alertas
BEFORE INSERT ON alertas FOR EACH ROW
BEGIN :NEW.id_alerta := seq_alerta.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_suscripciones
BEFORE INSERT ON suscripciones FOR EACH ROW
BEGIN :NEW.id_suscripcion := seq_suscripcion.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_asignaciones_unidad
BEFORE INSERT ON asignaciones_unidad FOR EACH ROW
BEGIN :NEW.id_asignacion := seq_asignacion.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_atencion_alerta
BEFORE INSERT ON atencion_alerta FOR EACH ROW
BEGIN :NEW.id_atencion := seq_atencion_alerta.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_notificaciones
BEFORE INSERT ON notificaciones FOR EACH ROW
BEGIN :NEW.id_notificacion := seq_notificacion.NEXTVAL; END;
/

CREATE OR REPLACE TRIGGER trg_id_alarmas
BEFORE INSERT ON alarmas FOR EACH ROW
BEGIN
    :NEW.id_alarma := seq_alarmas.NEXTVAL;
END;
/

-- Trigger de negocio: actualiza estado de alerta al registrar atención
CREATE OR REPLACE TRIGGER trg_actualizar_estado_alerta
AFTER INSERT ON atencion_alerta
FOR EACH ROW
BEGIN
    IF :NEW.estado_final = 'ATENDIDA' THEN
        UPDATE alertas SET estado = 'CERRADA'     WHERE id_alerta = :NEW.id_alerta;
    ELSIF :NEW.estado_final = 'EN_PROCESO' THEN
        UPDATE alertas SET estado = 'EN_ATENCION' WHERE id_alerta = :NEW.id_alerta;
    ELSIF :NEW.estado_final = 'FALSA_ALARMA' THEN
        UPDATE alertas SET estado = 'CANCELADA'   WHERE id_alerta = :NEW.id_alerta;
    END IF;
END;
/


-- ============================================================
-- 9. VISTAS
-- ============================================================

CREATE OR REPLACE VIEW vw_alertas_completa AS
    SELECT
        a.id_alerta,
        a.fecha,
        a.estado,
        a.descripcion,
        a.etapa,
        a.sector,
        a.manzana,
        a.casa,
        a.calle,
        a.carrera,
        a.referencia,
        a.latitud,
        a.longitud,
        u.primer_nombre || ' ' || u.primer_apellido AS nombre_usuario,
        u.cedula,
        u.telefono,
        u.username,
        ta.nombre   AS tipo_alerta,
        b.nombre    AS barrio,
        c.nombre    AS comuna,
        arm.nombre  AS tipo_arma,
        mt.nombre   AS medio_transporte,
        al.id_alarma,
        al.nombre   AS nombre_alarma,
        al.estado   AS estado_alarma
    FROM alertas a
    JOIN usuarios            u   ON a.id_usuario          = u.id_usuario
    JOIN tipos_alerta        ta  ON a.id_tipo_alerta       = ta.id_tipo_alerta
    JOIN barrios             b   ON a.id_barrio            = b.id_barrio
    JOIN comunas             c   ON b.id_comuna            = c.id_comuna
    LEFT JOIN tipos_arma     arm ON a.id_tipo_arma         = arm.id_tipo_arma
    LEFT JOIN medios_transporte mt ON a.id_medio_transporte = mt.id_medio_transporte
    LEFT JOIN alarmas        al  ON a.id_alarma            = al.id_alarma;
/


CREATE OR REPLACE VIEW vw_atenciones_completa AS
    SELECT
        aa.id_atencion,
        aa.fecha_atencion,
        aa.estado_final,
        aa.descripcion,
        aa.observacion,
        aa.id_alerta,
        a.estado      AS estado_alerta,
        a.descripcion AS descripcion_alerta,
        a.fecha       AS fecha_alerta,
        up.nombre     AS nombre_unidad,
        up.estado     AS estado_unidad,
        b.nombre      AS barrio_unidad,
        ta.nombre     AS tipo_arma,
        mt.nombre     AS medio_transporte
    FROM atencion_alerta aa
    JOIN alertas             a   ON aa.id_alerta                       = a.id_alerta
    JOIN unidades_policiales up  ON aa.id_unidad                       = up.id_unidad
    JOIN barrios             b   ON up.id_barrio                       = b.id_barrio
    LEFT JOIN tipos_arma     ta  ON aa.id_tipo_arma                    = ta.id_tipo_arma          
    LEFT JOIN medios_transporte mt ON aa.id_medio_transporte = mt.id_medio_transporte;  
/

CREATE OR REPLACE VIEW vw_usuarios AS
    SELECT
        u.primer_nombre || ' ' || u.primer_apellido AS nombre_completo,
        u.segundo_nombre,
        u.segundo_apellido,
        u.cedula,
        u.telefono,
        u.email,
        u.username,
        u.activo,
        r.nombre AS rol
    FROM usuarios u
    JOIN roles_usuario r ON u.id_rol = r.id_rol;
/

CREATE OR REPLACE VIEW vw_policias AS
    SELECT
        u.primer_nombre || ' ' || u.primer_apellido AS nombre_completo,
        u.cedula,
        u.telefono,
        u.email,
        p.placa,
        p.rango,
        p.estado     AS estado_policia,
        up.nombre    AS nombre_unidad,
        up.estado    AS estado_unidad,
        b.nombre     AS barrio_unidad,
        c.nombre     AS comuna_unidad
    FROM usuarios u
    JOIN policias            p  ON u.id_usuario = p.id_usuario
    JOIN unidades_policiales up ON p.id_unidad  = up.id_unidad
    JOIN barrios             b  ON up.id_barrio = b.id_barrio
    JOIN comunas             c  ON b.id_comuna  = c.id_comuna;
/

CREATE OR REPLACE VIEW vw_barrios AS
    SELECT
        b.id_barrio,
        b.nombre  AS barrio,
        c.id_comuna,
        c.nombre  AS comuna
    FROM barrios b
    JOIN comunas c ON b.id_comuna = c.id_comuna;
/

CREATE OR REPLACE VIEW vw_suscripciones AS
    SELECT
        s.id_suscripcion,
        s.estado,
        u.primer_nombre || ' ' || u.primer_apellido AS nombre_usuario,
        u.cedula,
        ta.nombre AS tipo_alerta,
        c.nombre  AS comuna,
        b.nombre  AS barrio
    FROM suscripciones s
    JOIN usuarios     u  ON s.id_usuario     = u.id_usuario
    JOIN tipos_alerta ta ON s.id_tipo_alerta = ta.id_tipo_alerta
    LEFT JOIN comunas c  ON s.id_comuna      = c.id_comuna
    LEFT JOIN barrios b  ON s.id_barrio      = b.id_barrio;
/

CREATE OR REPLACE VIEW vw_asignaciones AS
    SELECT
        au.id_asignacion,
        au.fecha,
        au.observacion,
        a.estado      AS estado_alerta,
        a.descripcion AS descripcion_alerta,
        up.nombre     AS nombre_unidad,
        up.estado     AS estado_unidad,
        b.nombre      AS barrio_unidad
    FROM asignaciones_unidad au
    JOIN alertas             a  ON au.id_alerta = a.id_alerta
    JOIN unidades_policiales up ON au.id_unidad = up.id_unidad
    JOIN barrios             b  ON up.id_barrio = b.id_barrio;
/

CREATE OR REPLACE VIEW vw_notificaciones AS
    SELECT
        n.id_notificacion,
        n.mensaje,
        n.fecha,
        n.enviada,
        u.primer_nombre || ' ' || u.primer_apellido AS nombre_usuario,
        u.email,
        a.descripcion AS descripcion_alerta,
        a.estado      AS estado_alerta
    FROM notificaciones n
    JOIN usuarios u ON n.id_usuario = u.id_usuario
    JOIN alertas  a ON n.id_alerta  = a.id_alerta;
/

CREATE OR REPLACE VIEW vw_alarmas AS
     SELECT
   	a.id_alarma,
   	a.nombre,
   	b.nombre  AS barrio,
   	c.nombre  AS comuna,
   	a.latitud,
  	a.longitud,
   	a.radio_cobertura,
   	a.estado
    FROM alarmas a
    JOIN barrios b ON a.id_barrio = b.id_barrio
    JOIN comunas c ON b.id_comuna = c.id_comuna;
/


-- ============================================================
-- 10. PAQUETES
-- ============================================================

-- ------------------------------------------------------------
-- PKG_CATALOGOS - Spec
-- ------------------------------------------------------------
CREATE OR REPLACE PACKAGE pkg_catalogos AS

    -- Comunas
    PROCEDURE pr_insertar_comuna(p_nombre VARCHAR2);
    PROCEDURE pr_listar_comunas(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_comuna(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2);
    PROCEDURE pr_eliminar_comuna(p_nombre VARCHAR2);

    -- Barrios
    PROCEDURE pr_insertar_barrio(p_nombre VARCHAR2, p_nombre_comuna VARCHAR2);
    PROCEDURE pr_listar_barrios(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_barrios_por_comuna(p_nombre_comuna VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_barrio(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2);
    PROCEDURE pr_eliminar_barrio(p_nombre VARCHAR2);

    -- Tipos de alerta
    PROCEDURE pr_insertar_tipo_alerta(p_nombre VARCHAR2);
    PROCEDURE pr_listar_tipos_alerta(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_tipo_alerta(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2);
    PROCEDURE pr_eliminar_tipo_alerta(p_nombre VARCHAR2);

    -- Tipos de arma
    PROCEDURE pr_insertar_tipo_arma(p_nombre VARCHAR2);
    PROCEDURE pr_listar_tipos_arma(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_tipo_arma(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2);
    PROCEDURE pr_eliminar_tipo_arma(p_nombre VARCHAR2);

    -- Medios de transporte
    PROCEDURE pr_insertar_medio(p_nombre VARCHAR2);
    PROCEDURE pr_listar_medios(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_medio(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2);
    PROCEDURE pr_eliminar_medio(p_nombre VARCHAR2);
    -- Búsquedas parciales (barra de búsqueda)
    PROCEDURE pr_buscar_comuna(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_barrio(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_tipo_alerta(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_tipo_arma(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_medio(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);

    -- Búsquedas exactas (validaciones / selects)
    PROCEDURE pr_buscar_comuna_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_barrio_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_tipo_alerta_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_tipo_arma_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_medio_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);

END pkg_catalogos;
/

-- ------------------------------------------------------------
-- PKG_CATALOGOS - Body
-- ------------------------------------------------------------
CREATE OR REPLACE PACKAGE BODY pkg_catalogos AS

    -- Comunas
    PROCEDURE pr_insertar_comuna(p_nombre VARCHAR2) IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM comunas WHERE UPPER(nombre) = UPPER(p_nombre);
        IF v > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Comuna ya existe'); RETURN; END IF;
        INSERT INTO comunas(nombre) VALUES(p_nombre);
        COMMIT;
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_comunas(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM comunas ORDER BY nombre;
    END;

    PROCEDURE pr_actualizar_comuna(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2) IS
    BEGIN
        UPDATE comunas SET nombre = p_nombre_nuevo WHERE UPPER(nombre) = UPPER(p_nombre_actual);
        COMMIT;
    END;

    PROCEDURE pr_eliminar_comuna(p_nombre VARCHAR2) IS
        v_en_uso NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_en_uso
        FROM barrios b JOIN comunas c ON b.id_comuna = c.id_comuna
        WHERE UPPER(c.nombre) = UPPER(p_nombre);
        IF v_en_uso > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Comuna tiene barrios asociados'); RETURN; END IF;
        DELETE FROM comunas WHERE UPPER(nombre) = UPPER(p_nombre);
        COMMIT;
    END;

    -- Barrios
    PROCEDURE pr_insertar_barrio(p_nombre VARCHAR2, p_nombre_comuna VARCHAR2) IS
        v_id_comuna NUMBER;
        v_existe    NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v_existe FROM barrios WHERE UPPER(nombre) = UPPER(p_nombre);
        IF v_existe > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio ya existe'); RETURN; END IF;
        SELECT id_comuna INTO v_id_comuna FROM comunas WHERE UPPER(nombre) = UPPER(p_nombre_comuna);
        INSERT INTO barrios(nombre, id_comuna) VALUES(p_nombre, v_id_comuna);
        COMMIT;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN DBMS_OUTPUT.PUT_LINE('Error: Comuna no encontrada');
        WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_barrios(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT barrio, comuna FROM vw_barrios;
    END;

    PROCEDURE pr_buscar_barrios_por_comuna(p_nombre_comuna VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT barrio, comuna FROM vw_barrios
            WHERE UPPER(comuna) = UPPER(p_nombre_comuna)
            ORDER BY barrio;
    END;

    PROCEDURE pr_actualizar_barrio(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2) IS
    BEGIN
        UPDATE barrios SET nombre = p_nombre_nuevo WHERE UPPER(nombre) = UPPER(p_nombre_actual);
        COMMIT;
    END;

    PROCEDURE pr_eliminar_barrio(p_nombre VARCHAR2) IS
    BEGIN
        DELETE FROM barrios WHERE UPPER(nombre) = UPPER(p_nombre);
        COMMIT;
    END;

    -- Tipos de alerta
    PROCEDURE pr_insertar_tipo_alerta(p_nombre VARCHAR2) IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM tipos_alerta WHERE UPPER(nombre) = UPPER(p_nombre);
        IF v > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo ya existe'); RETURN; END IF;
        INSERT INTO tipos_alerta(nombre) VALUES(p_nombre);
        COMMIT;
    END;

    PROCEDURE pr_listar_tipos_alerta(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM tipos_alerta ORDER BY nombre;
    END;

    PROCEDURE pr_actualizar_tipo_alerta(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2) IS
    BEGIN
        UPDATE tipos_alerta SET nombre = p_nombre_nuevo WHERE UPPER(nombre) = UPPER(p_nombre_actual);
        COMMIT;
    END;

    PROCEDURE pr_eliminar_tipo_alerta(p_nombre VARCHAR2) IS
    BEGIN
        DELETE FROM tipos_alerta WHERE UPPER(nombre) = UPPER(p_nombre);
        COMMIT;
    END;

    -- Tipos de arma
    PROCEDURE pr_insertar_tipo_arma(p_nombre VARCHAR2) IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM tipos_arma WHERE UPPER(nombre) = UPPER(p_nombre);
        IF v > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo ya existe'); RETURN; END IF;
        INSERT INTO tipos_arma(nombre) VALUES(p_nombre);
        COMMIT;
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_tipos_arma(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM tipos_arma ORDER BY nombre;
    END;

    PROCEDURE pr_actualizar_tipo_arma(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2) IS
    BEGIN
        UPDATE tipos_arma SET nombre = p_nombre_nuevo WHERE UPPER(nombre) = UPPER(p_nombre_actual);
        COMMIT;
    END;

    PROCEDURE pr_eliminar_tipo_arma(p_nombre VARCHAR2) IS
    BEGIN
        DELETE FROM tipos_arma WHERE UPPER(nombre) = UPPER(p_nombre);
        COMMIT;
    END;

    -- Medios de transporte
    PROCEDURE pr_insertar_medio(p_nombre VARCHAR2) IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM medios_transporte WHERE UPPER(nombre) = UPPER(p_nombre);
        IF v > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Medio ya existe'); RETURN; END IF;
        INSERT INTO medios_transporte(nombre) VALUES(p_nombre);
        COMMIT;
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_medios(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM medios_transporte ORDER BY nombre;
    END;

    PROCEDURE pr_actualizar_medio(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2) IS
    BEGIN
        UPDATE medios_transporte SET nombre = p_nombre_nuevo WHERE UPPER(nombre) = UPPER(p_nombre_actual);
        COMMIT;
    END;

    PROCEDURE pr_eliminar_medio(p_nombre VARCHAR2) IS
    BEGIN
        DELETE FROM medios_transporte WHERE UPPER(nombre) = UPPER(p_nombre);
        COMMIT;
    END;

    -- Búsquedas parciales
    PROCEDURE pr_buscar_comuna(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM comunas WHERE UPPER(nombre) LIKE '%' || UPPER(p_nombre) || '%' ORDER BY nombre;
    END;

    PROCEDURE pr_buscar_barrio(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT barrio, comuna FROM vw_barrios WHERE UPPER(barrio) LIKE '%' || UPPER(p_nombre) || '%' ORDER BY barrio;
    END;

    PROCEDURE pr_buscar_tipo_alerta(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM tipos_alerta WHERE UPPER(nombre) LIKE '%' || UPPER(p_nombre) || '%' ORDER BY nombre;
    END;

    PROCEDURE pr_buscar_tipo_arma(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM tipos_arma WHERE UPPER(nombre) LIKE '%' || UPPER(p_nombre) || '%' ORDER BY nombre;
    END;

    PROCEDURE pr_buscar_medio(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM medios_transporte WHERE UPPER(nombre) LIKE '%' || UPPER(p_nombre) || '%' ORDER BY nombre;
    END;

    -- Búsquedas exactas
    PROCEDURE pr_buscar_comuna_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM comunas WHERE UPPER(nombre) = UPPER(p_nombre);
    END;

    PROCEDURE pr_buscar_barrio_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT barrio, comuna FROM vw_barrios WHERE UPPER(barrio) = UPPER(p_nombre);
    END;

    PROCEDURE pr_buscar_tipo_alerta_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM tipos_alerta WHERE UPPER(nombre) = UPPER(p_nombre);
    END;

    PROCEDURE pr_buscar_tipo_arma_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM tipos_arma WHERE UPPER(nombre) = UPPER(p_nombre);
    END;

    PROCEDURE pr_buscar_medio_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM medios_transporte WHERE UPPER(nombre) = UPPER(p_nombre);
    END;

END pkg_catalogos;
/


-- ------------------------------------------------------------
-- PKG_USUARIOS - Spec
-- ------------------------------------------------------------
CREATE OR REPLACE PACKAGE pkg_usuarios AS

    -- Usuarios
    PROCEDURE pr_insertar_usuario(
    p_primer_nombre    VARCHAR2, p_segundo_nombre   VARCHAR2,
    p_primer_apellido  VARCHAR2, p_segundo_apellido VARCHAR2,
    p_cedula           VARCHAR2, p_telefono         VARCHAR2,
    p_email            VARCHAR2, p_username         VARCHAR2,
    p_password         VARCHAR2, p_nombre_rol       VARCHAR2,
    p_nombre_barrio    VARCHAR2,
    p_calle            VARCHAR2, p_carrera          VARCHAR2,
    p_etapa            VARCHAR2, p_manzana          VARCHAR2,
    p_casa             VARCHAR2
    );

    PROCEDURE pr_actualizar_usuario(
    p_username         VARCHAR2,
    p_primer_nombre    VARCHAR2, p_segundo_nombre   VARCHAR2,
    p_primer_apellido  VARCHAR2, p_segundo_apellido VARCHAR2,
    p_telefono         VARCHAR2, p_email            VARCHAR2,
    p_password         VARCHAR2, p_nombre_rol       VARCHAR2,
    p_nombre_barrio    VARCHAR2,
    p_calle            VARCHAR2, p_carrera          VARCHAR2,
    p_etapa            VARCHAR2, p_manzana          VARCHAR2,
    p_casa             VARCHAR2
    );

    PROCEDURE pr_eliminar_usuario(p_cedula VARCHAR2);
    PROCEDURE pr_consultar_usuario(p_cedula VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_usuarios(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_login_usuario(p_username VARCHAR2, p_password VARCHAR2, p_cursor OUT SYS_REFCURSOR);

    -- Roles
    PROCEDURE pr_insertar_rol(p_nombre VARCHAR2);
    PROCEDURE pr_actualizar_rol(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2);
    PROCEDURE pr_eliminar_rol(p_nombre VARCHAR2);
    PROCEDURE pr_listar_roles(p_cursor OUT SYS_REFCURSOR);

    -- Policías
    PROCEDURE pr_insertar_policia(
        p_cedula_usuario VARCHAR2, p_nombre_unidad VARCHAR2,
        p_placa VARCHAR2, p_rango VARCHAR2, p_estado VARCHAR2
    );
    PROCEDURE pr_actualizar_policia(
        p_cedula_usuario VARCHAR2, p_nombre_unidad VARCHAR2,
        p_placa VARCHAR2, p_rango VARCHAR2, p_estado VARCHAR2
    );
    PROCEDURE pr_eliminar_policia(p_cedula_usuario VARCHAR2);
    PROCEDURE pr_listar_policias(p_cursor OUT SYS_REFCURSOR);
    
    -- Búsquedas parciales
    PROCEDURE pr_buscar_usuario(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_policia(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_rol(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);

    -- Búsquedas exactas
    PROCEDURE pr_buscar_usuario_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_policia_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_rol_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    
    -- Activar / Inactivar
    PROCEDURE pr_inactivar_usuario(p_cedula VARCHAR2);
    PROCEDURE pr_activar_usuario(p_cedula VARCHAR2);

END pkg_usuarios;
/

-- ------------------------------------------------------------
-- PKG_USUARIOS - Body
-- ------------------------------------------------------------
CREATE OR REPLACE PACKAGE BODY pkg_usuarios AS

    -- Funciones privadas
    FUNCTION fx_obtener_id_usuario_cedula(p_cedula VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_usuario INTO v FROM usuarios WHERE cedula = p_cedula;
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_usuario_username(p_username VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_usuario INTO v FROM usuarios WHERE username = p_username;
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_rol(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_rol INTO v FROM roles_usuario WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_unidad(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_unidad INTO v FROM unidades_policiales WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    -- Usuarios
    PROCEDURE pr_insertar_usuario(
    p_primer_nombre    VARCHAR2, p_segundo_nombre   VARCHAR2,
    p_primer_apellido  VARCHAR2, p_segundo_apellido VARCHAR2,
    p_cedula           VARCHAR2, p_telefono         VARCHAR2,
    p_email            VARCHAR2, p_username         VARCHAR2,
    p_password         VARCHAR2, p_nombre_rol       VARCHAR2,
    p_nombre_barrio    VARCHAR2,
    p_calle            VARCHAR2, p_carrera          VARCHAR2,
    p_etapa            VARCHAR2, p_manzana          VARCHAR2,
    p_casa             VARCHAR2
) IS
    v_id_rol    NUMBER;
    v_id_barrio NUMBER;
    v_existe    NUMBER;
BEGIN
    SELECT COUNT(*) INTO v_existe FROM usuarios WHERE cedula = p_cedula;
    IF v_existe > 0 THEN
        DBMS_OUTPUT.PUT_LINE('Error: Ya existe un usuario con esa cédula');
        RETURN;
    END IF;
    v_id_rol := fx_obtener_id_rol(p_nombre_rol);
    IF v_id_rol IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('Error: El rol no existe');
        RETURN;
    END IF;
    IF p_nombre_barrio IS NOT NULL THEN
        SELECT id_barrio INTO v_id_barrio
        FROM barrios WHERE UPPER(nombre) = UPPER(p_nombre_barrio);
    END IF;
    INSERT INTO usuarios(
        primer_nombre, segundo_nombre, primer_apellido, segundo_apellido,
        cedula, telefono, email, username, password, activo, id_rol,
        id_barrio, calle, carrera, etapa, manzana, casa
    ) VALUES (
        p_primer_nombre, p_segundo_nombre, p_primer_apellido, p_segundo_apellido,
        p_cedula, p_telefono, p_email, p_username, p_password, 'ACTIVO', v_id_rol,
        v_id_barrio, p_calle, p_carrera, p_etapa, p_manzana, p_casa
    );
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Usuario registrado correctamente');
EXCEPTION WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

    PROCEDURE pr_actualizar_usuario(
    p_username         VARCHAR2,
    p_primer_nombre    VARCHAR2, p_segundo_nombre   VARCHAR2,
    p_primer_apellido  VARCHAR2, p_segundo_apellido VARCHAR2,
    p_telefono         VARCHAR2, p_email            VARCHAR2,
    p_password         VARCHAR2, p_nombre_rol       VARCHAR2,
    p_nombre_barrio    VARCHAR2,
    p_calle            VARCHAR2, p_carrera          VARCHAR2,
    p_etapa            VARCHAR2, p_manzana          VARCHAR2,
    p_casa             VARCHAR2
) IS
    v_id_rol     NUMBER;
    v_id_usuario NUMBER;
    v_id_barrio  NUMBER;
BEGIN
    v_id_usuario := fx_obtener_id_usuario_username(p_username);
    IF v_id_usuario IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
    v_id_rol := fx_obtener_id_rol(p_nombre_rol);
    IF v_id_rol IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Rol no encontrado'); RETURN; END IF;
    IF p_nombre_barrio IS NOT NULL THEN
        SELECT id_barrio INTO v_id_barrio
        FROM barrios WHERE UPPER(nombre) = UPPER(p_nombre_barrio);
    END IF;
    UPDATE usuarios
    SET primer_nombre    = p_primer_nombre,
        segundo_nombre   = p_segundo_nombre,
        primer_apellido  = p_primer_apellido,
        segundo_apellido = p_segundo_apellido,
        telefono         = p_telefono,
        email            = p_email,
        password         = p_password,
        id_rol           = v_id_rol,
        id_barrio        = v_id_barrio,
        calle            = p_calle,
        carrera          = p_carrera,
        etapa            = p_etapa,
        manzana          = p_manzana,
        casa             = p_casa
    WHERE id_usuario = v_id_usuario;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Usuario actualizado correctamente');
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

    PROCEDURE pr_eliminar_usuario(p_cedula VARCHAR2) IS
        v_id NUMBER;
    BEGIN
        v_id := fx_obtener_id_usuario_cedula(p_cedula);
        IF v_id IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        DELETE FROM usuarios WHERE id_usuario = v_id;
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Usuario eliminado');
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_consultar_usuario(p_cedula VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido,
                   u.cedula, u.telefono, u.email, u.username, u.activo, r.nombre AS rol
            FROM usuarios u JOIN roles_usuario r ON u.id_rol = r.id_rol
            WHERE u.cedula = p_cedula;
    END;

    PROCEDURE pr_listar_usuarios(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido,
                   u.cedula, u.telefono, u.email, u.username, u.activo, r.nombre AS rol
            FROM usuarios u JOIN roles_usuario r ON u.id_rol = r.id_rol
            ORDER BY u.primer_apellido;
    END;

    PROCEDURE pr_login_usuario(p_username VARCHAR2, p_password VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.id_usuario,
               u.primer_nombre,
               u.segundo_nombre,
               u.primer_apellido,
               u.segundo_apellido,
               u.cedula,
               u.telefono,
               u.email,
               u.username,
               u.password,
               u.activo,
               u.id_rol,
               r.nombre AS rol_nombre
        FROM usuarios u JOIN roles_usuario r ON u.id_rol = r.id_rol
        WHERE u.username = p_username AND u.password = p_password;
END;
    -- Roles
    PROCEDURE pr_insertar_rol(p_nombre VARCHAR2) IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM roles_usuario WHERE UPPER(nombre) = UPPER(p_nombre);
        IF v > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Rol ya existe'); RETURN; END IF;
        INSERT INTO roles_usuario(nombre) VALUES(p_nombre);
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Rol creado');
    END;

    PROCEDURE pr_actualizar_rol(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2) IS
        v_id NUMBER;
    BEGIN
        v_id := fx_obtener_id_rol(p_nombre_actual);
        IF v_id IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Rol no encontrado'); RETURN; END IF;
        UPDATE roles_usuario SET nombre = p_nombre_nuevo WHERE id_rol = v_id;
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Rol actualizado');
    END;

    PROCEDURE pr_eliminar_rol(p_nombre VARCHAR2) IS
        v_id     NUMBER;
        v_en_uso NUMBER;
    BEGIN
        v_id := fx_obtener_id_rol(p_nombre);
        IF v_id IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Rol no encontrado'); RETURN; END IF;
        SELECT COUNT(*) INTO v_en_uso FROM usuarios WHERE id_rol = v_id;
        IF v_en_uso > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Rol en uso'); RETURN; END IF;
        DELETE FROM roles_usuario WHERE id_rol = v_id;
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Rol eliminado');
    END;

    PROCEDURE pr_listar_roles(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT nombre FROM roles_usuario ORDER BY nombre;
    END;

    -- Policías
    PROCEDURE pr_insertar_policia(
        p_cedula_usuario VARCHAR2, p_nombre_unidad VARCHAR2,
        p_placa VARCHAR2, p_rango VARCHAR2, p_estado VARCHAR2
    ) IS
        v_id_usuario NUMBER;
        v_id_unidad  NUMBER;
        v_existe     NUMBER;
    BEGIN
        v_id_usuario := fx_obtener_id_usuario_cedula(p_cedula_usuario);
        IF v_id_usuario IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        SELECT COUNT(*) INTO v_existe FROM policias WHERE id_usuario = v_id_usuario;
        IF v_existe > 0 THEN DBMS_OUTPUT.PUT_LINE('Error: El usuario ya es policía'); RETURN; END IF;
        v_id_unidad := fx_obtener_id_unidad(p_nombre_unidad);
        IF v_id_unidad IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
        INSERT INTO policias(id_usuario, id_unidad, placa, rango, estado)
        VALUES(v_id_usuario, v_id_unidad, p_placa, p_rango, p_estado);
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Policía registrado correctamente');
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_actualizar_policia(
        p_cedula_usuario VARCHAR2, p_nombre_unidad VARCHAR2,
        p_placa VARCHAR2, p_rango VARCHAR2, p_estado VARCHAR2
    ) IS
        v_id_usuario NUMBER;
        v_id_unidad  NUMBER;
    BEGIN
        v_id_usuario := fx_obtener_id_usuario_cedula(p_cedula_usuario);
        IF v_id_usuario IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        v_id_unidad := fx_obtener_id_unidad(p_nombre_unidad);
        IF v_id_unidad IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
        UPDATE policias
        SET id_unidad = v_id_unidad, placa = p_placa, rango = p_rango, estado = p_estado
        WHERE id_usuario = v_id_usuario;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Policía no encontrado'); END IF;
        COMMIT;
    END;

    PROCEDURE pr_eliminar_policia(p_cedula_usuario VARCHAR2) IS
        v_id NUMBER;
    BEGIN
        v_id := fx_obtener_id_usuario_cedula(p_cedula_usuario);
        IF v_id IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        DELETE FROM policias WHERE id_usuario = v_id;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Policía no encontrado'); END IF;
        COMMIT;
    END;

    PROCEDURE pr_listar_policias(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT u.primer_nombre || ' ' || u.primer_apellido AS nombre_completo,
                   u.cedula, u.telefono, u.email,
                   p.placa, p.rango, p.estado,
                   up.nombre AS nombre_unidad
            FROM usuarios u
            JOIN policias            p  ON u.id_usuario = p.id_usuario
            JOIN unidades_policiales up ON p.id_unidad  = up.id_unidad
            ORDER BY u.primer_apellido;
    END;
    
-- ── BÚSQUEDAS pkg_usuarios ───────────────────────────────────

PROCEDURE pr_buscar_usuario(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.primer_nombre, u.segundo_nombre,
               u.primer_apellido, u.segundo_apellido,
               u.cedula, u.telefono, u.email,
               u.username, u.activo, r.nombre AS rol
        FROM usuarios u
        JOIN roles_usuario r ON u.id_rol = r.id_rol
        WHERE UPPER(u.primer_nombre)    LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(u.primer_apellido)  LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(u.segundo_apellido) LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(u.username)         LIKE '%' || UPPER(p_nombre) || '%'
           OR u.cedula                  LIKE '%' || p_nombre        || '%'
        ORDER BY u.primer_apellido;
END;

PROCEDURE pr_buscar_usuario_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.primer_nombre, u.segundo_nombre,
               u.primer_apellido, u.segundo_apellido,
               u.cedula, u.telefono, u.email,
               u.username, u.activo, r.nombre AS rol
        FROM usuarios u
        JOIN roles_usuario r ON u.id_rol = r.id_rol
        WHERE UPPER(u.primer_nombre)   = UPPER(p_nombre)
           OR UPPER(u.primer_apellido) = UPPER(p_nombre)
           OR UPPER(u.username)        = UPPER(p_nombre)
           OR u.cedula                 = p_nombre
        ORDER BY u.primer_apellido;
END;

PROCEDURE pr_buscar_policia(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.primer_nombre || ' ' || u.primer_apellido AS nombre_completo,
               u.cedula, u.telefono, u.email,
               p.placa, p.rango, p.estado,
               up.nombre AS nombre_unidad
        FROM usuarios u
        JOIN policias            p  ON u.id_usuario = p.id_usuario
        JOIN unidades_policiales up ON p.id_unidad  = up.id_unidad
        WHERE UPPER(u.primer_nombre)   LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(u.primer_apellido) LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(p.placa)           LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(p.rango)           LIKE '%' || UPPER(p_nombre) || '%'
        ORDER BY u.primer_apellido;
END;

PROCEDURE pr_buscar_policia_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.primer_nombre || ' ' || u.primer_apellido AS nombre_completo,
               u.cedula, u.telefono, u.email,
               p.placa, p.rango, p.estado,
               up.nombre AS nombre_unidad
        FROM usuarios u
        JOIN policias            p  ON u.id_usuario = p.id_usuario
        JOIN unidades_policiales up ON p.id_unidad  = up.id_unidad
        WHERE UPPER(u.primer_nombre)   = UPPER(p_nombre)
           OR UPPER(u.primer_apellido) = UPPER(p_nombre)
           OR UPPER(p.placa)           = UPPER(p_nombre)
           OR UPPER(p.rango)           = UPPER(p_nombre)
        ORDER BY u.primer_apellido;
END;

PROCEDURE pr_buscar_rol(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT nombre FROM roles_usuario
        WHERE UPPER(nombre) LIKE '%' || UPPER(p_nombre) || '%'
        ORDER BY nombre;
END;

PROCEDURE pr_buscar_rol_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT nombre FROM roles_usuario
        WHERE UPPER(nombre) = UPPER(p_nombre);
END;

PROCEDURE pr_inactivar_usuario(p_cedula VARCHAR2) IS
    v_id NUMBER;
BEGIN
    v_id := fx_obtener_id_usuario_cedula(p_cedula);
    IF v_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado');
        RETURN;
    END IF;
    UPDATE usuarios SET activo = 'INACTIVO' WHERE id_usuario = v_id;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Usuario inactivado');
EXCEPTION WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

PROCEDURE pr_activar_usuario(p_cedula VARCHAR2) IS
    v_id NUMBER;
BEGIN
    v_id := fx_obtener_id_usuario_cedula(p_cedula);
    IF v_id IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado');
        RETURN;
    END IF;
    UPDATE usuarios SET activo = 'ACTIVO' WHERE id_usuario = v_id;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Usuario activado');
EXCEPTION WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

END pkg_usuarios;
/


-- ------------------------------------------------------------
-- PKG_ALERTAS - Spec
-- ------------------------------------------------------------
CREATE OR REPLACE PACKAGE pkg_alertas AS

    -- Alertas
    PROCEDURE pr_insertar_alerta(
        p_username VARCHAR2, p_tipo_alerta VARCHAR2, p_nombre_barrio VARCHAR2,
        p_tipo_arma VARCHAR2, p_medio_transporte VARCHAR2, p_etapa VARCHAR2,
        p_sector VARCHAR2, p_manzana VARCHAR2, p_casa VARCHAR2,
        p_calle VARCHAR2, p_carrera VARCHAR2, p_referencia VARCHAR2,
        p_latitud NUMBER, p_longitud NUMBER, p_descripcion VARCHAR2
    );
    PROCEDURE pr_consultar_alerta(p_id_alerta NUMBER, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_alertas(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_alertas_por_usuario(p_username VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_alertas_por_barrio(p_nombre_barrio VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_estado(p_id_alerta NUMBER, p_estado VARCHAR2);
    PROCEDURE pr_eliminar_alerta(p_id_alerta NUMBER);

    -- Atención alerta
    FUNCTION  fx_atencion_existe(p_id_atencion NUMBER) RETURN NUMBER;
    PROCEDURE pr_insertar_atencion(
        p_id_alerta NUMBER, p_nombre_unidad VARCHAR2, p_estado_final VARCHAR2,
        p_descripcion VARCHAR2, p_tipo_arma VARCHAR2, p_medio_transporte VARCHAR2,
        p_observacion VARCHAR2
    );
    PROCEDURE pr_consultar_atencion(p_id_atencion NUMBER, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_atenciones(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_atenciones_por_alerta(p_id_alerta NUMBER, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_atencion(
        p_id_atencion NUMBER, p_estado_final VARCHAR2, p_descripcion VARCHAR2,
        p_tipo_arma VARCHAR2, p_medio_transporte VARCHAR2, p_observacion VARCHAR2
    );
    PROCEDURE pr_eliminar_atencion(p_id_atencion NUMBER);

    -- Notificaciones
    PROCEDURE pr_insertar_notificacion(p_id_alerta NUMBER, p_cedula_usuario VARCHAR2, p_mensaje VARCHAR2);
    PROCEDURE pr_listar_notificaciones(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_eliminar_notificacion(p_id_notificacion NUMBER);

    -- Suscripciones
    PROCEDURE pr_insertar_suscripcion(
        p_cedula_usuario VARCHAR2, p_tipo_alerta VARCHAR2,
        p_nombre_comuna VARCHAR2, p_nombre_barrio VARCHAR2, p_estado VARCHAR2
    );
    PROCEDURE pr_listar_suscripciones(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_suscripcion(
        p_id_suscripcion NUMBER, p_cedula_usuario VARCHAR2, p_tipo_alerta VARCHAR2,
        p_nombre_comuna VARCHAR2, p_nombre_barrio VARCHAR2, p_estado VARCHAR2
    );
    PROCEDURE pr_eliminar_suscripcion(p_id_suscripcion NUMBER);

    -- Unidades policiales
    PROCEDURE pr_insertar_unidad(p_nombre VARCHAR2, p_estado VARCHAR2, p_nombre_barrio VARCHAR2, p_latitud NUMBER, p_longitud NUMBER);
    PROCEDURE pr_listar_unidades(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_unidad(p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2, p_estado VARCHAR2, p_nombre_barrio VARCHAR2, p_latitud NUMBER, p_longitud NUMBER);
    PROCEDURE pr_eliminar_unidad(p_nombre VARCHAR2);

    -- Asignaciones
    PROCEDURE pr_asignar_unidad_cercana(p_id_alerta NUMBER);
    PROCEDURE pr_insertar_asignacion(p_id_alerta NUMBER, p_nombre_unidad VARCHAR2, p_observacion VARCHAR2, p_fecha TIMESTAMP);
    PROCEDURE pr_listar_asignaciones(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_asignacion(p_id_asignacion NUMBER, p_id_alerta NUMBER, p_nombre_unidad VARCHAR2, p_observacion VARCHAR2, p_fecha TIMESTAMP);
    PROCEDURE pr_eliminar_asignacion(p_id_asignacion NUMBER);
    
    -- Búsquedas parciales
    PROCEDURE pr_buscar_alerta_por_tipo(p_tipo_alerta VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_alerta_por_descripcion(p_texto VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_unidad(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_atencion_por_estado(p_estado VARCHAR2, p_cursor OUT SYS_REFCURSOR);

    -- Búsquedas exactas
    PROCEDURE pr_buscar_alerta_por_tipo_exacto(p_tipo_alerta VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_unidad_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_buscar_atencion_por_estado_exacto(p_estado VARCHAR2, p_cursor OUT SYS_REFCURSOR);
    
    -- Alarmas
    PROCEDURE pr_insertar_alarma(
    p_nombre VARCHAR2, p_nombre_barrio VARCHAR2,
    p_latitud NUMBER, p_longitud NUMBER,
    p_radio_cobertura NUMBER, p_estado VARCHAR2
    );
    PROCEDURE pr_consultar_alarma(p_id_alarma NUMBER, p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_listar_alarmas(p_cursor OUT SYS_REFCURSOR);
    PROCEDURE pr_actualizar_alarma(
    p_id_alarma NUMBER, p_nombre VARCHAR2, p_nombre_barrio VARCHAR2,
    p_latitud NUMBER, p_longitud NUMBER,
    p_radio_cobertura NUMBER, p_estado VARCHAR2
    );
    PROCEDURE pr_eliminar_alarma(p_id_alarma NUMBER);

END pkg_alertas;
/

-- ------------------------------------------------------------
-- PKG_ALERTAS - Body
-- ------------------------------------------------------------
CREATE OR REPLACE PACKAGE BODY pkg_alertas AS

    -- Funciones privadas
    FUNCTION fx_obtener_id_usuario(p_username VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_usuario INTO v FROM usuarios WHERE username = p_username;
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_usuario_cedula(p_cedula VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_usuario INTO v FROM usuarios WHERE cedula = p_cedula;
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_tipo_alerta(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_tipo_alerta INTO v FROM tipos_alerta WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_barrio(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_barrio INTO v FROM barrios WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_tipo_arma(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_tipo_arma INTO v FROM tipos_arma WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_medio(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_medio_transporte INTO v FROM medios_transporte WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_unidad(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_unidad INTO v FROM unidades_policiales WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_obtener_id_comuna(p_nombre VARCHAR2) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT id_comuna INTO v FROM comunas WHERE UPPER(nombre) = UPPER(p_nombre);
        RETURN v;
    EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
    END;

    FUNCTION fx_alerta_existe(p_id NUMBER) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM alertas WHERE id_alerta = p_id; RETURN v;
    END;

-- ── FUNCIONES ALARMAS ───────────────────────────────

    FUNCTION fx_alarma_mas_cercana(p_latitud NUMBER, p_longitud NUMBER) RETURN NUMBER IS
    v_id_alarma NUMBER;
BEGIN
    SELECT id_alarma INTO v_id_alarma
    FROM (
        SELECT id_alarma,
               SQRT(POWER(latitud - p_latitud, 2) + POWER(longitud - p_longitud, 2)) distancia
        FROM alarmas
        WHERE estado = 'ACTIVA'
        ORDER BY distancia
    )
    WHERE ROWNUM = 1;
    RETURN v_id_alarma;
EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
END;


    -- Alertas
    PROCEDURE pr_insertar_alerta(
        p_username VARCHAR2, p_tipo_alerta VARCHAR2, p_nombre_barrio VARCHAR2,
        p_tipo_arma VARCHAR2, p_medio_transporte VARCHAR2, p_etapa VARCHAR2,
        p_sector VARCHAR2, p_manzana VARCHAR2, p_casa VARCHAR2,
        p_calle VARCHAR2, p_carrera VARCHAR2, p_referencia VARCHAR2,
        p_latitud NUMBER, p_longitud NUMBER, p_descripcion VARCHAR2
    ) IS
        v_id_usuario     NUMBER;
        v_id_tipo_alerta NUMBER;
        v_id_barrio      NUMBER;
        v_id_tipo_arma   NUMBER;
        v_id_medio       NUMBER;
        v_id_alarma      NUMBER;

    BEGIN
        v_id_usuario := fx_obtener_id_usuario(p_username);
        IF v_id_usuario IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        v_id_tipo_alerta := fx_obtener_id_tipo_alerta(p_tipo_alerta);
        IF v_id_tipo_alerta IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo de alerta no encontrado'); RETURN; END 	IF;
        v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio);
        IF v_id_barrio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio no encontrado'); RETURN; END IF;
        IF p_tipo_arma IS NOT NULL THEN
            v_id_tipo_arma := fx_obtener_id_tipo_arma(p_tipo_arma);
            IF v_id_tipo_arma IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo de arma no encontrado'); RETURN; END 	IF;
        END IF;
        IF p_medio_transporte IS NOT NULL THEN
            v_id_medio := fx_obtener_id_medio(p_medio_transporte);
            IF v_id_medio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Medio de transporte no encontrado'); RETURN; 	END IF;
        END IF;
            v_id_alarma := fx_alarma_mas_cercana(p_latitud, p_longitud);
        INSERT INTO alertas(
            id_usuario, id_tipo_alerta, id_barrio, id_tipo_arma,
            id_medio_transporte, id_alarma,estado, etapa, sector, manzana,
            casa, calle, carrera, referencia, latitud, longitud, descripcion, fecha
        ) VALUES (
            v_id_usuario, v_id_tipo_alerta, v_id_barrio, v_id_tipo_arma,
            v_id_medio, v_id_alarma, 'PENDIENTE', p_etapa, p_sector, p_manzana,
            p_casa, p_calle, p_carrera, p_referencia, p_latitud, p_longitud, p_descripcion, SYSTIMESTAMP
        );
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Alerta registrada correctamente');
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_consultar_alerta(p_id_alerta NUMBER, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_alertas_completa WHERE id_alerta = p_id_alerta;
    END;

    PROCEDURE pr_listar_alertas(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_alertas_completa ORDER BY fecha DESC;
    END;

    PROCEDURE pr_listar_alertas_por_usuario(p_username VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT * FROM vw_alertas_completa WHERE username = p_username ORDER BY fecha DESC;
    END;

    PROCEDURE pr_listar_alertas_por_barrio(p_nombre_barrio VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT * FROM vw_alertas_completa
            WHERE UPPER(barrio) = UPPER(p_nombre_barrio) ORDER BY fecha DESC;
    END;

    PROCEDURE pr_actualizar_estado(p_id_alerta NUMBER, p_estado VARCHAR2) IS
    BEGIN
        UPDATE alertas SET estado = p_estado WHERE id_alerta = p_id_alerta;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Alerta no encontrada'); END IF;
        COMMIT;
    END;

    PROCEDURE pr_eliminar_alerta(p_id_alerta NUMBER) IS
    BEGIN
        DELETE FROM alertas WHERE id_alerta = p_id_alerta;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Alerta no encontrada'); END IF;
        COMMIT;
    END;

    -- Atención alerta
    FUNCTION fx_atencion_existe(p_id_atencion NUMBER) RETURN NUMBER IS
        v NUMBER;
    BEGIN
        SELECT COUNT(*) INTO v FROM atencion_alerta WHERE id_atencion = p_id_atencion; RETURN v;
    END;


    PROCEDURE pr_insertar_atencion(
        p_id_alerta NUMBER, p_nombre_unidad VARCHAR2, p_estado_final VARCHAR2,
        p_descripcion VARCHAR2, p_tipo_arma VARCHAR2, p_medio_transporte VARCHAR2,
        p_observacion VARCHAR2
    ) IS
        v_id_unidad NUMBER;
        v_id_arma   NUMBER;
        v_id_medio  NUMBER;
    BEGIN
        IF fx_alerta_existe(p_id_alerta) = 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Alerta no encontrada'); RETURN; END IF;
        v_id_unidad := fx_obtener_id_unidad(p_nombre_unidad);
        IF v_id_unidad IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
        IF p_tipo_arma IS NOT NULL THEN
            v_id_arma := fx_obtener_id_tipo_arma(p_tipo_arma);
            IF v_id_arma IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo de arma no encontrado'); RETURN; END IF;
        END IF;
        IF p_medio_transporte IS NOT NULL THEN
            v_id_medio := fx_obtener_id_medio(p_medio_transporte);
            IF v_id_medio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Medio de transporte no encontrado'); RETURN; END IF;
        END IF;
        INSERT INTO atencion_alerta(
            id_alerta, id_unidad, fecha_atencion, estado_final,
            descripcion, id_tipo_arma, id_medio_transporte, observacion  
        ) VALUES (
            p_id_alerta, v_id_unidad, SYSTIMESTAMP, p_estado_final,
            p_descripcion, v_id_arma, v_id_medio, p_observacion
        );
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Atención registrada correctamente');
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_consultar_atencion(p_id_atencion NUMBER, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_atenciones_completa WHERE id_atencion = p_id_atencion;
    END;

    PROCEDURE pr_listar_atenciones(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_atenciones_completa ORDER BY fecha_atencion DESC;
    END;

    PROCEDURE pr_listar_atenciones_por_alerta(p_id_alerta NUMBER, p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR
            SELECT * FROM vw_atenciones_completa
            WHERE id_alerta = p_id_alerta ORDER BY fecha_atencion DESC;
    END;

       PROCEDURE pr_actualizar_atencion(
        p_id_atencion NUMBER, p_estado_final VARCHAR2, p_descripcion VARCHAR2,
        p_tipo_arma VARCHAR2, p_medio_transporte VARCHAR2, p_observacion VARCHAR2
    ) IS
        v_id_arma  NUMBER;
        v_id_medio NUMBER;
    BEGIN
        IF fx_atencion_existe(p_id_atencion) = 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Atención no encontrada'); RETURN; END IF;
        IF p_tipo_arma IS NOT NULL THEN
            v_id_arma := fx_obtener_id_tipo_arma(p_tipo_arma);
            IF v_id_arma IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo de arma no encontrado'); RETURN; END IF;
        END IF;
        IF p_medio_transporte IS NOT NULL THEN
            v_id_medio := fx_obtener_id_medio(p_medio_transporte);
            IF v_id_medio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Medio no encontrado'); RETURN; END IF;
        END IF;
        UPDATE atencion_alerta
        SET estado_final                   = p_estado_final,
            descripcion                    = p_descripcion,
            id_tipo_arma                   = v_id_arma,        
            id_medio_transporte            = v_id_medio,       
            observacion                    = p_observacion
        WHERE id_atencion = p_id_atencion;
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Atención actualizada');
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_eliminar_atencion(p_id_atencion NUMBER) IS
    BEGIN
        DELETE FROM atencion_alerta WHERE id_atencion = p_id_atencion;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Atención no encontrada');
        ELSE DBMS_OUTPUT.PUT_LINE('Atención eliminada'); END IF;
        COMMIT;
    END;

    -- Notificaciones
    PROCEDURE pr_insertar_notificacion(p_id_alerta NUMBER, p_cedula_usuario VARCHAR2, p_mensaje VARCHAR2) IS
        v_id_usuario NUMBER;
    BEGIN
        IF fx_alerta_existe(p_id_alerta) = 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Alerta no encontrada'); RETURN; END IF;
        v_id_usuario := fx_obtener_id_usuario_cedula(p_cedula_usuario);
        IF v_id_usuario IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        INSERT INTO notificaciones(id_alerta, id_usuario, mensaje, fecha)
        VALUES(p_id_alerta, v_id_usuario, p_mensaje, SYSTIMESTAMP);
        COMMIT;
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_notificaciones(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_notificaciones;
    END;

    PROCEDURE pr_eliminar_notificacion(p_id_notificacion NUMBER) IS
    BEGIN
        DELETE FROM notificaciones WHERE id_notificacion = p_id_notificacion;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Notificación no encontrada'); END IF;
        COMMIT;
    END;

    -- Suscripciones
    PROCEDURE pr_insertar_suscripcion(
        p_cedula_usuario VARCHAR2, p_tipo_alerta VARCHAR2,
        p_nombre_comuna VARCHAR2, p_nombre_barrio VARCHAR2, p_estado VARCHAR2
    ) IS
        v_id_usuario     NUMBER;
        v_id_tipo_alerta NUMBER;
        v_id_comuna      NUMBER;
        v_id_barrio      NUMBER;
    BEGIN
        v_id_usuario := fx_obtener_id_usuario_cedula(p_cedula_usuario);
        IF v_id_usuario IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Usuario no encontrado'); RETURN; END IF;
        v_id_tipo_alerta := fx_obtener_id_tipo_alerta(p_tipo_alerta);
        IF v_id_tipo_alerta IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Tipo alerta no encontrado'); RETURN; END IF;
        IF p_nombre_comuna IS NOT NULL THEN
            v_id_comuna := fx_obtener_id_comuna(p_nombre_comuna);
            IF v_id_comuna IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Comuna no encontrada'); RETURN; END IF;
        END IF;
        IF p_nombre_barrio IS NOT NULL THEN
            v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio);
            IF v_id_barrio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio no encontrado'); RETURN; END IF;
        END IF;
        INSERT INTO suscripciones(id_usuario, id_tipo_alerta, id_comuna, id_barrio, estado)
        VALUES(v_id_usuario, v_id_tipo_alerta, v_id_comuna, v_id_barrio, p_estado);
        COMMIT;
        DBMS_OUTPUT.PUT_LINE('Suscripción creada');
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_suscripciones(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_suscripciones;
    END;

    PROCEDURE pr_actualizar_suscripcion(
        p_id_suscripcion NUMBER, p_cedula_usuario VARCHAR2, p_tipo_alerta VARCHAR2,
        p_nombre_comuna VARCHAR2, p_nombre_barrio VARCHAR2, p_estado VARCHAR2
    ) IS
        v_id_usuario     NUMBER;
        v_id_tipo_alerta NUMBER;
        v_id_comuna      NUMBER;
        v_id_barrio      NUMBER;
    BEGIN
        v_id_usuario     := fx_obtener_id_usuario_cedula(p_cedula_usuario);
        v_id_tipo_alerta := fx_obtener_id_tipo_alerta(p_tipo_alerta);
        IF p_nombre_comuna IS NOT NULL THEN v_id_comuna := fx_obtener_id_comuna(p_nombre_comuna); END IF;
        IF p_nombre_barrio IS NOT NULL THEN v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio); END IF;
        UPDATE suscripciones
        SET id_usuario = v_id_usuario, id_tipo_alerta = v_id_tipo_alerta,
            id_comuna = v_id_comuna, id_barrio = v_id_barrio, estado = p_estado
        WHERE id_suscripcion = p_id_suscripcion;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Suscripción no encontrada'); END IF;
        COMMIT;
    END;

    PROCEDURE pr_eliminar_suscripcion(p_id_suscripcion NUMBER) IS
    BEGIN
        DELETE FROM suscripciones WHERE id_suscripcion = p_id_suscripcion;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Suscripción no encontrada'); END IF;
        COMMIT;
    END;

    -- Unidades policiales
    PROCEDURE pr_insertar_unidad(
    p_nombre VARCHAR2, p_estado VARCHAR2,
    p_nombre_barrio VARCHAR2,
    p_latitud NUMBER, p_longitud NUMBER
) IS
    v_id_barrio NUMBER;
BEGIN
    v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio);
    IF v_id_barrio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio no encontrado'); RETURN; END IF;
    INSERT INTO unidades_policiales(nombre, estado, id_barrio, latitud, longitud)
    VALUES(p_nombre, p_estado, v_id_barrio, p_latitud, p_longitud);
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Unidad creada');
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

PROCEDURE pr_listar_unidades(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.nombre, u.estado,
               u.latitud, u.longitud,
               b.nombre AS barrio,
               c.nombre AS comuna
        FROM unidades_policiales u
        JOIN barrios b ON u.id_barrio = b.id_barrio
        JOIN comunas c ON b.id_comuna = c.id_comuna
        ORDER BY u.nombre;
END;

PROCEDURE pr_actualizar_unidad(
    p_nombre_actual VARCHAR2, p_nombre_nuevo VARCHAR2,
    p_estado VARCHAR2, p_nombre_barrio VARCHAR2,
    p_latitud NUMBER, p_longitud NUMBER
) IS
    v_id_unidad NUMBER;
    v_id_barrio NUMBER;
BEGIN
    v_id_unidad := fx_obtener_id_unidad(p_nombre_actual);
    IF v_id_unidad IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
    v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio);
    IF v_id_barrio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio no encontrado'); RETURN; END IF;
    UPDATE unidades_policiales
    SET nombre   = p_nombre_nuevo,
        estado   = p_estado,
        id_barrio = v_id_barrio,
        latitud  = p_latitud,
        longitud = p_longitud
    WHERE id_unidad = v_id_unidad;
    COMMIT;
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

    PROCEDURE pr_eliminar_unidad(p_nombre VARCHAR2) IS
        v_id NUMBER;
    BEGIN
        v_id := fx_obtener_id_unidad(p_nombre);
        IF v_id IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
        DELETE FROM unidades_policiales WHERE id_unidad = v_id;
        COMMIT;
    END;

    -- Asignaciones

FUNCTION fx_unidad_mas_cercana(p_latitud NUMBER, p_longitud NUMBER) RETURN NUMBER IS
    v_id_unidad NUMBER;
BEGIN
    SELECT id_unidad INTO v_id_unidad
    FROM (
        SELECT id_unidad,
               SQRT(
                   POWER(latitud - p_latitud, 2) +
                   POWER(longitud - p_longitud, 2)
               ) distancia
        FROM unidades_policiales
        WHERE estado = 'ACTIVA'
          AND latitud  IS NOT NULL
          AND longitud IS NOT NULL
        ORDER BY distancia
    )
    WHERE ROWNUM = 1;
    RETURN v_id_unidad;
EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
END;

PROCEDURE pr_asignar_unidad_cercana(p_id_alerta NUMBER) IS
    v_id_unidad NUMBER;
    v_latitud   NUMBER;
    v_longitud  NUMBER;
BEGIN
    -- tomar coordenadas de la alerta
    SELECT latitud, longitud INTO v_latitud, v_longitud
    FROM alertas WHERE id_alerta = p_id_alerta;

    -- buscar unidad mas cercana
    v_id_unidad := fx_unidad_mas_cercana(v_latitud, v_longitud);
    IF v_id_unidad IS NULL THEN
        DBMS_OUTPUT.PUT_LINE('Error: No hay unidades activas disponibles');
        RETURN;
    END IF;

    -- crear asignacion
    INSERT INTO asignaciones_unidad(id_alerta, id_unidad, observacion, fecha)
    VALUES(
        p_id_alerta,
        v_id_unidad,
        'Asignación automática por proximidad',
        SYSTIMESTAMP
    );

    -- cambiar estado de la alerta
    UPDATE alertas SET estado = 'EN_ATENCION'
    WHERE id_alerta = p_id_alerta;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Unidad asignada correctamente');
EXCEPTION WHEN OTHERS THEN
    DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

    PROCEDURE pr_insertar_asignacion(p_id_alerta NUMBER, p_nombre_unidad VARCHAR2, p_observacion VARCHAR2, p_fecha TIMESTAMP) IS
        v_id_unidad NUMBER;
    BEGIN
        IF fx_alerta_existe(p_id_alerta) = 0 THEN DBMS_OUTPUT.PUT_LINE('Error: Alerta no encontrada'); RETURN; END IF;
        v_id_unidad := fx_obtener_id_unidad(p_nombre_unidad);
        IF v_id_unidad IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
        INSERT INTO asignaciones_unidad(id_alerta, id_unidad, observacion, fecha)
        VALUES(p_id_alerta, v_id_unidad, p_observacion, p_fecha);
        COMMIT;
    EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
    END;

    PROCEDURE pr_listar_asignaciones(p_cursor OUT SYS_REFCURSOR) IS
    BEGIN
        OPEN p_cursor FOR SELECT * FROM vw_asignaciones;
    END;

    PROCEDURE pr_actualizar_asignacion(
        p_id_asignacion NUMBER, p_id_alerta NUMBER,
        p_nombre_unidad VARCHAR2, p_observacion VARCHAR2, p_fecha TIMESTAMP
    ) IS
        v_id_unidad NUMBER;
    BEGIN
        v_id_unidad := fx_obtener_id_unidad(p_nombre_unidad);
        IF v_id_unidad IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Unidad no encontrada'); RETURN; END IF;
        UPDATE asignaciones_unidad
        SET id_alerta = p_id_alerta, id_unidad = v_id_unidad,
            observacion = p_observacion, fecha = p_fecha
        WHERE id_asignacion = p_id_asignacion;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Asignación no encontrada'); END IF;
        COMMIT;
    END;

    PROCEDURE pr_eliminar_asignacion(p_id_asignacion NUMBER) IS
    BEGIN
        DELETE FROM asignaciones_unidad WHERE id_asignacion = p_id_asignacion;
        IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Asignación no encontrada'); END IF;
        COMMIT;
    END;

    -- ── BÚSQUEDAS pkg_alertas ────────────────────────────────────

PROCEDURE pr_buscar_alerta_por_tipo(p_tipo_alerta VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT * FROM vw_alertas_completa
        WHERE UPPER(tipo_alerta) LIKE '%' || UPPER(p_tipo_alerta) || '%'
        ORDER BY fecha DESC;
END;

PROCEDURE pr_buscar_alerta_por_tipo_exacto(p_tipo_alerta VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT * FROM vw_alertas_completa
        WHERE UPPER(tipo_alerta) = UPPER(p_tipo_alerta)
        ORDER BY fecha DESC;
END;

PROCEDURE pr_buscar_alerta_por_descripcion(p_texto VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT * FROM vw_alertas_completa
        WHERE UPPER(descripcion) LIKE '%' || UPPER(p_texto) || '%'
           OR UPPER(barrio)      LIKE '%' || UPPER(p_texto) || '%'
           OR UPPER(sector)      LIKE '%' || UPPER(p_texto) || '%'
           OR UPPER(referencia)  LIKE '%' || UPPER(p_texto) || '%'
        ORDER BY fecha DESC;
END;

-- Nota: búsqueda exacta de descripción no aplica por ser texto libre,
-- se usa la parcial también para coincidencia precisa de barrio/sector:
PROCEDURE pr_buscar_unidad(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.nombre, u.estado, b.nombre AS barrio, c.nombre AS comuna
        FROM unidades_policiales u
        JOIN barrios b ON u.id_barrio = b.id_barrio
        JOIN comunas c ON b.id_comuna = c.id_comuna
        WHERE UPPER(u.nombre) LIKE '%' || UPPER(p_nombre) || '%'
           OR UPPER(b.nombre) LIKE '%' || UPPER(p_nombre) || '%'
        ORDER BY u.nombre;
END;

PROCEDURE pr_buscar_unidad_exacto(p_nombre VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT u.nombre, u.estado, b.nombre AS barrio, c.nombre AS comuna
        FROM unidades_policiales u
        JOIN barrios b ON u.id_barrio = b.id_barrio
        JOIN comunas c ON b.id_comuna = c.id_comuna
        WHERE UPPER(u.nombre) = UPPER(p_nombre);
END;

PROCEDURE pr_buscar_atencion_por_estado(p_estado VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT * FROM vw_atenciones_completa
        WHERE UPPER(estado_final) LIKE '%' || UPPER(p_estado) || '%'
        ORDER BY fecha_atencion DESC;
END;

PROCEDURE pr_buscar_atencion_por_estado_exacto(p_estado VARCHAR2, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR
        SELECT * FROM vw_atenciones_completa
        WHERE UPPER(estado_final) = UPPER(p_estado)
        ORDER BY fecha_atencion DESC;
END;

-- ── FUNCIONES ALARMAS ───────────────────────────────

FUNCTION fx_obtener_id_alarma(p_nombre VARCHAR2) RETURN NUMBER IS
    v NUMBER;
BEGIN
    SELECT id_alarma INTO v FROM alarmas
    WHERE UPPER(nombre) = UPPER(p_nombre);
    RETURN v;
EXCEPTION WHEN NO_DATA_FOUND THEN RETURN NULL;
END;

-- ── PROCEDIMIENTOS ALARMAS ───────────────────────────────────

PROCEDURE pr_insertar_alarma(
    p_nombre VARCHAR2, p_nombre_barrio VARCHAR2,
    p_latitud NUMBER, p_longitud NUMBER,
    p_radio_cobertura NUMBER, p_estado VARCHAR2
) IS
    v_id_barrio NUMBER;
BEGIN
    v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio);
    IF v_id_barrio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio no encontrado'); RETURN; END IF;
    INSERT INTO alarmas(nombre, id_barrio, latitud, longitud, radio_cobertura, estado)
    VALUES(p_nombre, v_id_barrio, p_latitud, p_longitud, p_radio_cobertura, p_estado);
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Alarma creada');
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

PROCEDURE pr_consultar_alarma(p_id_alarma NUMBER, p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR SELECT * FROM vw_alarmas WHERE id_alarma = p_id_alarma;
END;

PROCEDURE pr_listar_alarmas(p_cursor OUT SYS_REFCURSOR) IS
BEGIN
    OPEN p_cursor FOR SELECT * FROM vw_alarmas ORDER BY nombre;
END;

PROCEDURE pr_actualizar_alarma(
    p_id_alarma NUMBER, p_nombre VARCHAR2, p_nombre_barrio VARCHAR2,
    p_latitud NUMBER, p_longitud NUMBER,
    p_radio_cobertura NUMBER, p_estado VARCHAR2
) IS
    v_id_barrio NUMBER;
BEGIN
    v_id_barrio := fx_obtener_id_barrio(p_nombre_barrio);
    IF v_id_barrio IS NULL THEN DBMS_OUTPUT.PUT_LINE('Error: Barrio no encontrado'); RETURN; END IF;
    UPDATE alarmas
    SET nombre = p_nombre, id_barrio = v_id_barrio,
        latitud = p_latitud, longitud = p_longitud,
        radio_cobertura = p_radio_cobertura, estado = p_estado
    WHERE id_alarma = p_id_alarma;
    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Alarma actualizada');
EXCEPTION WHEN OTHERS THEN DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
END;

PROCEDURE pr_eliminar_alarma(p_id_alarma NUMBER) IS
BEGIN
    DELETE FROM alarmas WHERE id_alarma = p_id_alarma;
    IF SQL%NOTFOUND THEN DBMS_OUTPUT.PUT_LINE('Alarma no encontrada'); END IF;
    COMMIT;
END;

END pkg_alertas;
/
