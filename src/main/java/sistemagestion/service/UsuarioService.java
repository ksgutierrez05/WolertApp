/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package sistemagestion.service;

import java.sql.SQLException;
import java.util.List;
import sistemagestion.dao.UsuarioDAO;
import sistemagestion.model.Usuario;
import sistemagestion.util.Validador;

/**
 *
 * @author Maria Cristina
 */
public class UsuarioService {

    private UsuarioDAO dao;

    public UsuarioService() throws SQLException {
        dao = new UsuarioDAO();
    }

    public void insertar(Usuario u) throws SQLException {

        Validador.validarObjeto(u);

        Validador.validarCampoVacio(u.getPrimer_nombre());
        Validador.validarCampoVacio(u.getPrimer_apellido());

        Validador.validarIdentificacion(u.getIdentificacion());
        Validador.validarTelefono(u.getTelefono());
        Validador.validarCorreo(u.getCorreo());
        Validador.validarUsername(u.getUsername());
        Validador.validarPassword(u.getPassword());

        Validador.validarEnum(u.getEstado());
        Validador.validarObjeto(u.getRol());

        dao.insertar(
                u.getPrimer_nombre(),
                u.getSegundo_nombre(),
                u.getPrimer_apellido(),
                u.getSegundo_apellido(),
                u.getIdentificacion(),
                u.getTelefono(),
                u.getCorreo(),
                u.getUsername(),
                u.getPassword(),
                u.getRol().getNombre(),
                u.getDireccion().getBarrio().getNombre(),
                u.getDireccion().getCalle(),
                u.getDireccion().getCarrera(),
                u.getDireccion().getEtapa(),
                u.getDireccion().getManzana(),
                u.getDireccion().getCasa()
        );
    }

    public void actualizar(Usuario u) throws SQLException {

        Validador.validarObjeto(u);

        Validador.validarCampoVacio(u.getPrimer_nombre());
        Validador.validarCampoVacio(u.getPrimer_apellido());

        Validador.validarTelefono(u.getTelefono());
        Validador.validarCorreo(u.getCorreo());
        Validador.validarUsername(u.getUsername());
        Validador.validarPassword(u.getPassword());

        dao.actualizar(
                u.getUsername(),
                u.getPrimer_nombre(),
                u.getSegundo_nombre(),
                u.getPrimer_apellido(),
                u.getSegundo_apellido(),
                u.getTelefono(),
                u.getCorreo(),
                u.getPassword(),
                u.getRol().getNombre(),
                u.getDireccion().getBarrio().getNombre(),
                u.getDireccion().getCalle(),
                u.getDireccion().getCarrera(),
                u.getDireccion().getEtapa(),
                u.getDireccion().getManzana(),
                u.getDireccion().getCasa()
        );
    }

    public boolean suspender(String cedula) throws SQLException {
        Validador.validarIdentificacion(cedula);

        // Buscar el usuario para obtener su correo
        Usuario u = dao.buscarPorCedula(cedula);

        boolean ok = dao.inactivar(cedula); // usa inactivar que ya tienes en el DAO

        // Si se suspendió correctamente, enviar correo
        if (ok && u != null && u.getCorreo() != null) {
            String nombre = u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "Usuario";
            String cuerpo = """
            <div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;
                        border:1px solid #e5e7eb;border-radius:12px;overflow:hidden'>
              <div style='background:#e53935;padding:20px;text-align:center'>
                <h2 style='color:white;margin:0'>🐺 WolertApp</h2>
                <p style='color:#ffcdd2;margin:4px 0 0'>Sistema de alertas ciudadanas</p>
              </div>
              <div style='padding:24px'>
                <h3 style='color:#111827'>Cuenta suspendida</h3>
                <p style='color:#374151;font-size:15px'>
                  Hola <b>%s</b>, tu cuenta en WolertApp ha sido <b style='color:#e53935'>suspendida</b>
                  por un administrador del sistema.
                </p>
                <p style='color:#374151;font-size:14px'>
                  Si crees que esto es un error, comunícate con el administrador.
                </p>
                <hr style='border:none;border-top:1px solid #e5e7eb;margin:20px 0'>
                <p style='color:#6b7280;font-size:12px'>
                  Este es un mensaje automático de WolertApp.<br>
                  Por favor no responda este correo.
                </p>
              </div>
            </div>
            """.formatted(nombre);

            final String correo = u.getCorreo();
            new Thread(() -> {
                boolean enviado = EmailService.enviarCorreo(
                        correo,
                        "⚠️ Tu cuenta ha sido suspendida — WolertApp",
                        cuerpo
                );
                System.out.println(enviado
                        ? "✅ Correo de suspensión enviado a " + correo
                        : "❌ Error al enviar correo a " + correo);
            }).start();
        }

        return ok;
    }

    public boolean activar(String cedula) throws SQLException {
        Validador.validarIdentificacion(cedula);

        Usuario u = dao.buscarPorCedula(cedula);

        boolean ok = dao.activar(cedula);

        // Si se activó correctamente, enviar correo
        if (ok && u != null && u.getCorreo() != null) {
            String nombre = u.getPrimer_nombre() != null ? u.getPrimer_nombre() : "Usuario";
            String cuerpo = """
            <div style='font-family:Arial,sans-serif;max-width:500px;margin:auto;
                        border:1px solid #e5e7eb;border-radius:12px;overflow:hidden'>
              <div style='background:#43a047;padding:20px;text-align:center'>
                <h2 style='color:white;margin:0'> WolertApp</h2>
                <p style='color:#c8e6c9;margin:4px 0 0'>Sistema de alertas ciudadanas</p>
              </div>
              <div style='padding:24px'>
                <h3 style='color:#111827'>Cuenta reactivada</h3>
                <p style='color:#374151;font-size:15px'>
                  Hola <b>%s</b>, tu cuenta en WolertApp ha sido <b style='color:#43a047'>reactivada</b>.
                  Ya puedes volver a iniciar sesión normalmente.
                </p>
                <hr style='border:none;border-top:1px solid #e5e7eb;margin:20px 0'>
                <p style='color:#6b7280;font-size:12px'>
                  Este es un mensaje automático de WolertApp.<br>
                  Por favor no responda este correo.
                </p>
              </div>
            </div>
            """.formatted(nombre);

            final String correo = u.getCorreo();
            new Thread(() -> {
                boolean enviado = EmailService.enviarCorreo(
                        correo,
                        "✅ Tu cuenta ha sido reactivada — WolertApp",
                        cuerpo
                );
                System.out.println(enviado
                        ? "✅ Correo de reactivación enviado a " + correo
                        : "❌ Error al enviar correo a " + correo);
            }).start();
        }

        return ok;
    }

    public Usuario login(String username, String password) throws SQLException {
        Validador.validarUsername(username);
        Validador.validarCampoVacio(password);
        return dao.login(username, password);
    }

    public Usuario buscarPorCedula(String cedula) throws SQLException {

        Validador.validarIdentificacion(cedula);

        return dao.buscarPorCedula(cedula);
    }

    public List<Usuario> listar() throws SQLException {
        return dao.listarTodos();
    }

    public void eliminar(String cedula) throws SQLException {

        Validador.validarIdentificacion(cedula);

        dao.eliminar(cedula);
    }
}
