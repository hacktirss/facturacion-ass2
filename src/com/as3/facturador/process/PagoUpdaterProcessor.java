/*
 * PagoUpdaterProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.PagoDAO;
import com.as3.facturador.server.DocType;
import com.softcoatl.utils.logging.LogManager;
import com.as3.facturador.schema.ComprobanteDetisa;

public class PagoUpdaterProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final PagoUpdaterProcessor INSTANCE = new PagoUpdaterProcessor();
    }

    public static PagoUpdaterProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private PagoUpdaterProcessor() {}
    
    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        
        if (DocType.AN.equals(comprobante.getDocType())
                || DocType.RP.equals(comprobante.getDocType())) {
            try {
                LogManager.info("Actualizando pago " + comprobante.getId() + " y registros relacionados.");
                PagoDAO.updateTimbrado(comprobante.getId(), comprobante.getUuid());
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
