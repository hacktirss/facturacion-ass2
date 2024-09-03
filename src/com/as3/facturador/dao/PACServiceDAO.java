/*
 * PACServiceDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.dao;

import com.as3.facturador.exception.FacturacionException;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.sat.cfdi.pac.prodigia.ProdigiaServiceFactory;
import com.softcoatl.sat.cfdi.pac.prodigia.ProdigiaiPACWrapper;
import com.softcoatl.sat.cfdi.pac.sifei.SifeiPACWrapper;
import com.softcoatl.sat.cfdi.pac.sifei.SifeiServiceFactory;
import com.softcoatl.sat.pac.PAC;
import com.softcoatl.sat.pac.PACException;
import com.softcoatl.sat.pac.PACService;
import com.softcoatl.sat.pac.PACServiceFactory;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PACServiceDAO {
    
    private PACServiceFactory getFactory(String pacName) throws PACException {
        switch(pacName) {
            case "SIFEI": return new SifeiServiceFactory();
            case "PRODIGIA": return new ProdigiaServiceFactory();
            default: throw new PACException("No está configurado el Proveedor Autorizado de Certificación");
        }
    }

    private PACService parse(ResultSet rs) throws SQLException, PACException {
        PAC pac = new PAC();
        pac.setName(rs.getString("clave_pac"));
        pac.setServiceURL(rs.getString("url_webservice"));
        pac.setCancelURL(rs.getString("url_cancelacion"));
        pac.setUser(rs.getString("usuario"));
        pac.setPassword(rs.getString("password"));
        if ("SIFEI".equals(pac.getName())) {
            pac = new SifeiPACWrapper(pac);
            ((SifeiPACWrapper) pac).setIdEquipo(rs.getString("clave_aux"));
            ((SifeiPACWrapper) pac).setSerie(rs.getString("clave_aux2"));
        } else if ("PRODIGIA".equals(pac.getName())) {
            pac = new ProdigiaiPACWrapper(pac);
            ((ProdigiaiPACWrapper) pac).setContrato(rs.getString("clave_aux"));
        }

        LogManager.info("Invocando servicio " + pac.getServiceURL());
        return getFactory(pac.getName()).newPACService(pac);
    }

    public PACService active() throws SQLException, PACException, FacturacionException {
        String sql = "SELECT * FROM proveedor_pac WHERE activo = 1";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return parse(rs);
            } else {
                throw new FacturacionException("No está configurado el servidor de timbrado");
            }
        }
    }
}
