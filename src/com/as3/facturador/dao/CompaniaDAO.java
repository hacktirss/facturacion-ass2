/*
 * CompaniaDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.dao;

import com.as3.facturador.vo.CompaniaVO;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CompaniaDAO {

    private static final String FIELDS = "cia.idfae, cia.rfc, cia.cia razonSocial, cia.direccion calle, cia.numeroext exterior, cia.numeroint interior, "
            + "cia.colonia, cia.ciudad, cia.estado, cia.telefono, cia.codigo cp, c.clave regimen, c.descripcion regimenLetra";

    private static CompaniaVO parseCompania(ResultSet rs) throws SQLException {
        CompaniaVO compania = new CompaniaVO();
        compania.setIdfae(rs.getInt("idfae"));
        compania.setRfc(rs.getString("rfc"));
        compania.setRazonSocial(rs.getString("razonSocial"));
        compania.setCalle(rs.getString("calle"));
        compania.setExterior(rs.getString("exterior"));
        compania.setInterior(rs.getString("interior"));
        compania.setColonia(rs.getString("colonia"));
        compania.setCiudad(rs.getString("ciudad"));
        compania.setEstado(rs.getString("estado"));
        compania.setTelefono(rs.getString("telefono"));
        compania.setCp(rs.getString("cp"));
        compania.setRegimen(rs.getString("regimen"));
        compania.setRegimenLetra(rs.getString("regimenLetra"));
        return compania;
    }

    public static CompaniaVO get() {
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery("SELECT " + CompaniaDAO.FIELDS + " FROM cia JOIN cfdi33_c_regimenes c ON cia.clave_regimen = c.clave WHERE TRUE")) {
            if (rs.next()) {
                return parseCompania(rs);
            }
        } catch (SQLException ex) {
            LogManager.fatal("Error configurando el sistema. No es posible obtener los datos de la Estación.", ex);
        }
        throw new RuntimeException("No es posible configurar el servidor de timbrado. No se recuperaron los datos de la Estación");        
    }
}
