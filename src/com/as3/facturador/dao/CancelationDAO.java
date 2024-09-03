package com.as3.facturador.dao;

import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.sat.cfdi.pac.prodigia.ProdigiaCancelationWrapper;
import com.softcoatl.sat.pac.Cancelation;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CancelationDAO {
    
    private CancelationDAO() {}

    private static Cancelation getSifei(String uuid) {
        Cancelation cancelacion = new Cancelation();
        cancelacion.setUuid(uuid);
        cancelacion.setMotivo("02");
        cancelacion.setFolioSustitucion("");
        return cancelacion;
    }

    public static Cancelation getCancelation(String uuid) throws SQLException {
        String sql = "SELECT pp.clave_pac, "
                + "ExtractValue( f.cfdi_xml, '/cfdi:Comprobante/cfdi:Emisor/@Rfc' ) Emisor, "
                + "ExtractValue( f.cfdi_xml, '/cfdi:Comprobante/cfdi:Receptor/@Rfc' ) Receptor, "
                + "ExtractValue( f.cfdi_xml, '/cfdi:Comprobante/@Total' ) Total "
                + "FROM facturas f JOIN proveedor_pac pp ON pp.activo = 1 WHERE f.uuid = ?";
        LogManager.info("Cargando cancelación del Folio " + uuid);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement st = connection.prepareStatement(sql)) {
                st.setString(1, uuid);
                LogManager.debug(st);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    if ("SIFEI".equals(rs.getString("clave_pac"))) {
                        LogManager.info("Cancelacion SIFEI");
                        return getSifei(uuid);
                    } else if ("PRODIGIA".equals(rs.getString("clave_pac"))) {
                        LogManager.info("Cancelacion PRODIGIA");
                        ProdigiaCancelationWrapper cancelacion = new ProdigiaCancelationWrapper(getSifei(uuid));
                        cancelacion.setRfcEmisor(rs.getString("Emisor"));
                        cancelacion.setRfcReceptor(rs.getString("Receptor"));
                        cancelacion.setTotal(rs.getBigDecimal("Total"));
                        return cancelacion;
                    }
                }
            }
        }
        return null;
    }
}
