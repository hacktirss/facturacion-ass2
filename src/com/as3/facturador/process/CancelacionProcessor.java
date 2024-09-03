/* 
 * CancelacionProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.commons.CertificadoReader;
import com.as3.facturador.commons.CertificadoReaderFactory;
import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.dao.PACServiceDAO;
import com.as3.facturador.exception.FacturacionException;
import com.softcoatl.utils.logging.LogManager;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.softcoatl.sat.pac.PACException;
import java.sql.SQLException;

public class CancelacionProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final CancelacionProcessor INSTANCE = new CancelacionProcessor();
    }

    public static CancelacionProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private CancelacionProcessor() {}
    
    @Override
    public ComprobanteDetisa process(ComprobanteDetisa comprobante) {
        return comprobante.isValid() ?  nextExecution(execute(comprobante)) : comprobante;
    }

    @Override
    public ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        LogManager.info("Cancelando comprobante " + comprobante.getUuid() + "...");
        CertificadoReader reader = (new CertificadoReaderFactory()).getReader();
        try {
            comprobante.setXml(
                    new PACServiceDAO().active().doCancel(
                            FacturaDAO.loadEmisor().getRfc(), 
                            comprobante.getCancelation(), 
                            reader.getCertificates().getCertificate()).getBytes());
            return comprobante;
        } catch (FacturacionException | PACException | SQLException ex) {
            LogManager.error(ex);
            invalid(comprobante, ex.getMessage());
        }

        return comprobante;
    }
}
