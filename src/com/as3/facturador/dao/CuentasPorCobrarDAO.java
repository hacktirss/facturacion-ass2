/*
 * CentasPorCobrarDAO
 * FacturadorOmicrom®
 * © 2022, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since jul 2022 
 */
package com.as3.facturador.dao;

import com.softcoatl.database.DBException;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CuentasPorCobrarDAO {

    public static boolean insertDespacho(int idfc, int banco, String nombre) throws DBException {
        String sql = "INSERT INTO cxc ( cliente, placas, referencia, fecha, hora, concepto, tm, cantidad, importe, corte, producto, factura ) " +
                "SELECT " + banco + ", '" + nombre + "', fcd.ticket, now(), now(), IF( fcd.producto <= 10, 'VENTA COMBUSTIBLE (FACT)', 'VENTA ADITIVOS (FACT)' ), 'C', " +
                "fcd.cantidad, fcd.importe, corte, IF( fcd.producto <= 10, IFNULL( com.clavei, SUBSTRING( inv.descripcion, 1 ) ), 'A' ), fc.id " + 
                "FROM fc JOIN fcd ON fc.id = fcd.id JOIN islas ON islas.isla = 1 JOIN inv ON fcd.producto = inv.id LEFT JOIN com ON inv.descripcion = com.descripcion WHERE fc.id =  " + idfc;
        LogManager.debug(sql);
        return MySQLHelper.getInstance().execute(sql);
    }

    public static boolean updateDespacho(int idfc, int banco) throws DBException {
        String sql = "UPDATE fc JOIN fcd ON fc.id = fcd.id JOIN rm ON fcd.ticket = rm.id JOIN ct ON ct.id = rm.corte JOIN cli ON cli.id = " + banco + " " +
                    "SET rm.cliente = cli.id, rm.placas = cli.alias, rm.codigo = cli.id, rm.tipodepago = cli.tipodepago " +
                    "WHERE rm.cliente = 0 AND ct.statusctv = 'Abierto' AND fc.id = " + idfc;
        LogManager.debug(sql);
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                Statement stm = conn.createStatement()) {
            return stm.executeUpdate(sql) > 0;
        } catch (SQLException ex) {
            throw new DBException(ex);
        }
    }
}
