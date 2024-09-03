/*
 * FCUpdaterProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.FcDAO;
import com.as3.facturador.server.DocType;
import com.softcoatl.utils.logging.LogManager;
import com.as3.facturador.schema.ComprobanteDetisa;

public class FCUpdaterProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final FCUpdaterProcessor INSTANCE = new FCUpdaterProcessor();
    }

    public static FCUpdaterProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private FCUpdaterProcessor() {}
    
    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        
        if (DocType.FA.equals(comprobante.getDocType())
                || DocType.FG.equals(comprobante.getDocType())) {
            try {
                LogManager.info("Actualizando fc " + comprobante.getId() + " y registros relacionados.");
                FcDAO.updateTimbrado(comprobante.getId(), comprobante.getUuid());
            } catch (Exception ex) {
                LogManager.error(ex);
            } finally {
                if ( comprobante.isValid() ) {
                    LogManager.info("Registros actualizados...");
                }
            }
        }
        return comprobante;
    }
}
