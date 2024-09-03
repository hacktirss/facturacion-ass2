/*
 * FcDAO
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

public class FcDAO {

    public static boolean updateTimbrado(int id, String uuid) throws Exception {
        String sql = "UPDATE fc "
                + "JOIN fcd USING( id ) "
                + "LEFT JOIN rm ON fcd.ticket = rm.id AND fcd.producto <= 10 "
                + "LEFT JOIN vtaditivos vta ON fcd.ticket = vta.id AND fcd.producto > 10 "
                + "SET fc.status = 1, fc.uuid = ?, rm.uuid = ?, vta.uuid = ? "
                + "WHERE fc.id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            ps.setString(2, uuid);
            ps.setString(3, uuid);
            ps.setInt(4, id);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        }
    }
}
