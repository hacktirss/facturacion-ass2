/*
 * IngresosDAO
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

public class IngresosDAO {

    public static boolean updateTimbrado(int id, String uuid) throws Exception {
        String sql = "UPDATE ingresos SET ingresos.status = 1, ingresos.uuid = ? "
                + "WHERE ingresos.id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setInt(2, id);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        }
    }
}
