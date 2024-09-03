/* 
 * FacturadorOmicromContext
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.dao;

import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RegimenesDAO {
    
    public static String getDescripcion(String regimen) {
        String sql = "SELECT descripcion FROM cfdi33_c_regimenes WHERE clave = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, regimen);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("descripcion");
                }
            }
        } catch (SQLException ex) {
            LogManager.error("Error recuperando Descripcion");
        }
        return "";
    }
}
