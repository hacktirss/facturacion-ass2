/*
 * VariablesDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.dao;

import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.vo.DespachadorVO;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VariablesDAO {

    public static String getKeyPass() throws SQLException {
        String sql = "SELECT "
                + "CASE WHEN IFNULL( v.valor, 0 ) = 1 THEN deencrypt_data( facclavesat ) ELSE facclavesat END keypass "
                + "FROM cia "
                + "LEFT JOIN (SELECT valor FROM variables_corporativo WHERE llave = 'encrypt_fields') v ON TRUE "
                + "WHERE TRUE";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("keypass");
            } else {
                throw new SQLException("No está configurado el servidor de timbrado");
            }
        }
    }

    public static boolean hasStarted() throws SQLException {
        String sql = "SELECT DATE_ADD( NOW(), INTERVAL -filtro.delay HOUR) > filtro.inicio started "
                + "FROM ( "
                + "SELECT * FROM "
                + "( SELECT valor delay FROM variables_corporativo WHERE llave = 'fact_global_delay' ) a "
                + "JOIN ( SELECT TIMESTAMP( valor ) inicio FROM variables_corporativo WHERE llave = 'fact_global_inicio' ) b ON TRUE ) filtro";
        LogManager.debug(sql);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("started");
            } else {
                throw new SQLException("No está configurado el servidor de timbrado");
            }
        }
    }
    public static boolean mustExecuteGlobal() throws SQLException {
        String sql = "SELECT fechar = finejecucion AND DATE( inicio ) < finejecucion ejecucion " +
                     "FROM (" +
                        "SELECT filtro_global.*, " + 
                            "CASE " +
                                "WHEN periodo = '01' THEN fechar " +
                                "WHEN periodo = '02' THEN semanaini " +
                                "WHEN periodo = '03' THEN quincenaini " +
                                "WHEN periodo = '04' THEN mesini " +
                                "WHEN periodo = '05' THEN biini " +
                                "ELSE CURDATE() " +
                            "END finejecucion " +
                        "FROM filtro_global) filtro";
        LogManager.debug(sql);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("ejecucion");
            } else {
                throw new SQLException("No está configurado el servidor de timbrado");
            }
        }
    }
    public static boolean mustExecute() throws FacturacionException {
        String sql = "SELECT DATE_ADD( inicio, INTERVAL 1 HOUR ) <= fechahorar ejecucion FROM filtro_global;";
        LogManager.debug(sql);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getBoolean("ejecucion");
            } else {
                throw new SQLException("No está configurado el servidor de timbrado");
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
    }
    public static boolean slideWindowPeriodo() throws FacturacionException {
        String sql = "UPDATE variables_corporativo "
                + "JOIN filtro_global ON TRUE "
                + "SET valor = DATE_FORMAT( "
                +       "CASE " 
                +           "WHEN periodo = '01' THEN fechar "
                +           "WHEN periodo = '02' THEN semanaini "
                +           "WHEN periodo = '03' THEN quincenaini "
                +           "WHEN periodo = '04' THEN mesini "
                +           "WHEN periodo = '05' THEN biini "
                +           "ELSE fechar "
                +       "END, '%Y-%m-%d %H:%i:%s' ) "
                + "WHERE llave = 'fact_global_last'";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement()) {
                return st.executeUpdate(sql)>0;
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
    }
    public static boolean slideWindow() throws FacturacionException {
        String sql = "UPDATE variables_corporativo "
                + "JOIN filtro_global ON TRUE "
                + "SET valor = DATE_FORMAT( fechahorar, '%Y-%m-%d %H:%i:%s' ) "
                + "WHERE llave = 'fact_global_last'";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement()) {
                return st.executeUpdate(sql)>0;
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
    }
    public static boolean hasFacturacion() throws SQLException {
        String sql = "SELECT facturacion FROM cia";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "Si".equals(rs.getString("facturacion"));
                }
            }
        }
        return false;
    }

    public static boolean hasEnvioCV() throws SQLException {
        String sql = "SELECT activa_envio_xml FROM cia";
        try ( Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return "1".equals(rs.getString("activa_envio_xml"));
            }
        }
        return false;
    }

    public static String getVariable(String key, String def) {
        
        String sql = "SELECT IFNULL( ( SELECT valor FROM variables_corporativo WHERE llave = '" + key + "' ), " + def + " ) valor";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement ps = connection.createStatement();
                ResultSet rs = ps.executeQuery(sql)) {
            if (rs.next()) {
                LogManager.debug("variable: " + key + " = " + rs.getString("valor"));
                return rs.getString("valor");
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return "";
    }

    public static String getVariable(String llave) throws SQLException {
        String sql = "SELECT valor FROM variables_corporativo WHERE llave = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, llave);
            try(ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valor");
                } else {
                    throw new SQLException("No existe la variable");
                }
            }
        }
    }

    public static DespachadorVO getDespachadorByNIP(String nip, int posicion) throws FacturacionException {
            String sql = "SELECT IF( v.nipfactura = '1', IFNULL( ven.id, -1 ), -1  ) id, "
                    + "IF( v.nipfactura = '1', IFNULL( ven.nombre, 'TERMINAL' ), 'TERMINAL' ) nombre, "
                    + "IF( v.nipfactura = '1', IFNULL( ven.alias, 'TERMINAL' ), 'TERMINAL' ) alias, "
                    + "( CASE "
                    +   "WHEN v.nipfactura = '1' THEN IF(  ven.nip IS NOT NULL AND ven.nip <> '', ven.nip, 'INCORRECTO' ) "
                    +   "ELSE 'NO PIDE' END ) nip, "
                    + "IF( f.fixed = '0' OR man.posicion IS NOT NULL, 'OK', 'ER' ) asignado "
                    + "FROM ( SELECT IFNULL( ( SELECT valor FROM variables_corporativo WHERE llave = 'fact_password_pos' ), '0' ) nipfactura ) v "
                    + "JOIN ( SELECT IFNULL( ( SELECT valor FROM variables_corporativo WHERE llave = 'fixed_posicion_ven' ), '0' ) fixed ) f ON TRUE "
                    + "LEFT JOIN ven ON ven.nip = ? "
                    + "LEFT JOIN man ON man.despachador = ven.id AND man.posicion = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, nip);
            ps.setInt(2, posicion);
            try (ResultSet rs = ps.executeQuery()){
                if (rs.next()) {
                    DespachadorVO despachador = DespachadorVO.parse(rs);
                    despachador.setPosicion(posicion);
                    if ("ER".equals(rs.getString("asignado"))) {
                        throw new FacturacionException("El despachador " + despachador.getNombre() + " no está asignado a la posición " + posicion);
                    }
                    return despachador;
                }
            }
        } catch (SQLException sqle) {
            throw new FacturacionException(sqle);
        }
        throw new FacturacionException("Error recuperando Despachador");
    }//getNameByNIPI
}
