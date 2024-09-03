/*
 * IngresosUpdaterProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.IngresosDAO;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.as3.facturador.server.DocType;
import com.softcoatl.utils.logging.LogManager;

public class IngresosUpdaterProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final IngresosUpdaterProcessor INSTANCE = new IngresosUpdaterProcessor();
    }

    public static IngresosUpdaterProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private IngresosUpdaterProcessor() {}
    
    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        
        if (DocType.CPI.equals(comprobante.getDocType())) {
            try {
                LogManager.info("Actualizando traslado " + comprobante.getId() + " y registros relacionados.");
                IngresosDAO.updateTimbrado(comprobante.getId(), comprobante.getUuid());
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
