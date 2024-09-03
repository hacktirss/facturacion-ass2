/*
 * BitacoraDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
package com.as3.facturador.dao;

import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class BitacoraDAO {

    private BitacoraDAO() {}

    public static boolean evento(String descripcion) {
        String sql = "INSERT INTO bitacora_eventos ( fecha_evento, hora_evento, usuario, tipo_evento, descripcion_evento ) "
                + "VALUES( CURRENT_DATE(), CURRENT_TIME(), 'WSFacturacion', 'PIN', ? )";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, descripcion);
            return ps.executeUpdate()>0;
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return false;
    }
    
    public static boolean vencidos() {
        String sql = "INSERT INTO bitacora_eventos ( fecha_evento, hora_evento, usuario, tipo_evento, descripcion_evento ) "
                + "SELECT CURRENT_DATE(), CURRENT_TIME(), 'WSFacturacion', 'PIN', CONCAT( 'Cancelando folio vencido sin timbrar. Id ', fc.id, '( ', fc.serie, '-', fc.folio, ' )' ) "
                + "FROM fc "
                + "WHERE fc.status = 0 AND TIMESTAMPDIFF( HOUR, fc.fecha, NOW() ) > 72";
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                Statement stm = conn.createStatement()) {
            return stm.executeUpdate(sql) > 0;
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return false;
    }
}
