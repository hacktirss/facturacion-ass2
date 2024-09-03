/*
 * VentaDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since jul 2022 
 */
package com.as3.facturador.dao;

import com.softcoatl.database.mysql.MySQLHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VentaDAO {

    public static int asigna(int idcliente) throws SQLException {
        String sql = "UPDATE rm SET cliente = ? WHERE cliente = 0";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement st = connection.prepareStatement(sql)) {
            st.setInt(1, idcliente);
            return st.executeUpdate();
        }
    }
}
