/*
 * CuentasPorCobrarProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.CuentasPorCobrarDAO;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.softcoatl.database.DBException;
import com.softcoatl.utils.logging.LogManager;

public class CuentasPorCobrarProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final CuentasPorCobrarProcessor INSTANCE = new CuentasPorCobrarProcessor();
    }

    public static CuentasPorCobrarProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private CuentasPorCobrarProcessor() {}
    
    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        
        if (comprobante.getBanco()!=null) {
            try {
                if (CuentasPorCobrarDAO.updateDespacho(comprobante.getId(), comprobante.getBanco().getId())) {
                    CuentasPorCobrarDAO.insertDespacho(comprobante.getId(), comprobante.getBanco().getId(), comprobante.getBanco().getRazonSocial());
                }
            } catch (DBException ex) {
                LogManager.error("Error insertando registro en Cuentas Por Cobrar");
            }
        }
        return comprobante;
    }
}
