/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.util;

import java.time.LocalDateTime;
import sistemagestion.model.Alerta;
import sistemagestion.model.EstadoAlerta;
import sistemagestion.model.EstadoPolicia;
import sistemagestion.model.EstadoUnidadPolicial;
import sistemagestion.model.Policia;
import sistemagestion.model.UnidadPolicial;

/**
 *
 * @author Maria Cristina
 */
public class Validador {

    // VALIDAR CAMPOS VACÍOS
    public static void validarCampoVacio(String valor) {

        if (valor == null || valor.trim().isEmpty()) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR CORREO
    public static void validarCorreo(String correo) {

        validarCampoVacio(correo);

        if (!correo.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR TELÉFONO
    public static void validarTelefono(String telefono) {

        validarCampoVacio(telefono);

        if (!telefono.matches("\\d{10}")) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR PASSWORD
    public static void validarPassword(String password) {

        validarCampoVacio(password);

        if (password.length() < 8) {

            throw new IllegalArgumentException();
        }

        if (!password.matches("^(?=.*[A-Z])(?=.*\\d).+$")) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR USERNAME
    public static void validarUsername(String username) {

        validarCampoVacio(username);

        if (username.length() < 4) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR IDENTIFICACIÓN
    public static void validarIdentificacion(String identificacion) {

        validarCampoVacio(identificacion);

        if (!identificacion.matches("\\d+")) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR FECHA
    public static void validarFecha(LocalDateTime fecha) {

        if (fecha == null) {

            throw new IllegalArgumentException();
        }

        if (fecha.isAfter(LocalDateTime.now())) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR OBJETO NULO
    public static void validarObjeto(Object objeto) {

        if (objeto == null) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR ENUM
    public static void validarEnum(Object valor) {

        if (valor == null) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR POLICÍA DISPONIBLE
    public static void validarPoliciaDisponible(Policia policia) {

        validarObjeto(policia);

        if (policia.getEstadopolicial()
                == EstadoPolicia.OCUPADO
                || policia.getEstadopolicial()
                == EstadoPolicia.EN_SERVICIO
                || policia.getEstadopolicial()
                == EstadoPolicia.FUERA_DE_SERVICIO) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR UNIDAD OPERATIVA
    public static void validarUnidadOperativa(
            UnidadPolicial unidad) {

        validarObjeto(unidad);

        if (unidad.getEstado()
                != EstadoUnidadPolicial.OPERATIVA) {

            throw new IllegalArgumentException();
        }
    }

    // VALIDAR ALERTA ACTIVA
    public static void validarAlertaActiva(Alerta alerta) {

        validarObjeto(alerta);

        if (alerta.getEstado()
                == EstadoAlerta.CANCELADA
                || alerta.getEstado()
                == EstadoAlerta.RESUELTA) {

            throw new IllegalArgumentException();
        }
    }
}
