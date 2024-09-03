/*
 * MensajesDAO
 * FacturadorOmicrom®
 * © 2020, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since ene 2020 
 */
package com.as3.facturador.dao;

import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MensajesDAO {

    public static void newMessage(String message) {
        
        String query = "INSERT INTO msj ( tipo, de, para, titulo, nota, bd, fecha, hora, vigencia ) " +
                "VALUES ( 'R', 'Facturación', 'EXXXXX', 'Aviso del Sistema de Facturación Omicrom', ?, 0, CURRENT_DATE, CURRENT_TIME, 1 )";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, message);
            ps.executeUpdate();
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
    }
}
