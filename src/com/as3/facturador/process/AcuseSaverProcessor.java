/*
 * AcuseSaverProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.FacturaDAO;
import com.softcoatl.utils.logging.LogManager;
import com.as3.facturador.schema.ComprobanteDetisa;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

public class AcuseSaverProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final AcuseSaverProcessor INSTANCE = new AcuseSaverProcessor();
    }

    public static AcuseSaverProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private AcuseSaverProcessor() {}
    
    @Override
    public ComprobanteDetisa process(ComprobanteDetisa comprobante) {
        LogManager.debug("Procesando comprobante " + this.getClass() + " " + comprobante);
        return comprobante.isValid() ?  nextExecution(execute(comprobante)) : comprobante;
    }

    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {

        try {
            LogManager.info("Guardando Acuse en facturas.");
            FacturaDAO.saveAcuse(comprobante.getUuid(), new String(comprobante.getXml(), StandardCharsets.UTF_8));
        } catch (SQLException ex) {
            comprobante.setError(ex.getMessage());
        } finally {
            if ( comprobante.isValid() ) {
                LogManager.info("CFDI guardado...");
            }
        }
        return comprobante;
    }
}
