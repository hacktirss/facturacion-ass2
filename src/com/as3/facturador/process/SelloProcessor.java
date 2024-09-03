/* 
 * SelloProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.commons.CertificadoReaderFactory;
import com.as3.facturador.commons.Certificates;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.CertificadoReaderException;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.SelloCFDI;
import com.softcoatl.utils.logging.LogManager;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.sql.SQLException;
import com.as3.facturador.schema.ComprobanteDetisa;

public class SelloProcessor extends BaseProcessor implements FacturaProcessor {

    private static SelloProcessor INSTANCE = null;

    public static SelloProcessor getInstance() throws CertificadoReaderException {
        if (INSTANCE == null) {
            INSTANCE = new SelloProcessor();           
        }
        return INSTANCE;
    }

    private final Certificates certificates;

    private SelloProcessor() throws CertificadoReaderException {
        certificates = new CertificadoReaderFactory().getReader().getCertificates();
    }

    @Override
    public ComprobanteDetisa execute(ComprobanteDetisa comprobante) {
        try {
            
            LogManager.debug(comprobante.getCfdi().toXML());
            if (VariablesDAO.hasFacturacion()) {
                LogManager.info("Validando vigencia del certificado..." + certificates.getCertificate().getX509Certificate().getNotAfter());
                certificates.getCertificate().getX509Certificate().checkValidity(comprobante.getCfdi().getComprobante().getFecha().toGregorianCalendar().getTime());
                LogManager.info("Sellando comprobante...");
                SelloCFDI.getInstance()
                        .sella(
                                comprobante.getCfdi(), 
                                certificates.getCertificate().getPrivateKey(),
                                certificates.getCertificate().getX509Certificate());
                return comprobante;
            }
            LogManager.info("La facturación no está habilitada...");
            invalid(comprobante, "La facturación no está habilitada...");
        } catch (CertificateExpiredException ex) {
            LogManager.error("EX", ex);
            invalid(comprobante, "La fecha del comprobante es posterior a la vigencia del certificado...");
        } catch (CertificateNotYetValidException ex) {
            LogManager.error("EX", ex);
            invalid(comprobante, "La fecha del comprobante es anterior a la vigencia del certificado...");
        } catch (CFDIException | SQLException ex) {
            LogManager.error("EX", ex);
            invalid(comprobante, ex.getMessage());
        } catch (Throwable ex) {
            LogManager.error("EX", ex);
            LogManager.error("Cause", ex.getCause());
            invalid(comprobante, ex.getMessage());
        } finally {
            if (comprobante.isValid()) {
                LogManager.info("Comprobante sellado...");
            }
        }
        return comprobante;
    }
}//SelloProcessor
