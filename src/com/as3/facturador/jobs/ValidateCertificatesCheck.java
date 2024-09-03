/*
 * ValidateCertificatesCheck
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
package com.as3.facturador.jobs;

import com.detisa.dao.CertificadosDAO;
import com.as3.facturador.dao.MensajesDAO;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.CertificateExpirationWarning;
import com.softcoatl.security.KeyLoader;
import com.softcoatl.utils.StringUtils;
import com.softcoatl.utils.logging.LogManager;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ValidateCertificatesCheck  implements Runnable {

    private static final String FCWARN = "Después de esta fecha no será posible timbrar facturas. Favor de tramitar un nuevo certificado ante el SAT y hacerlo llegar al área de soporte de Detisa a fin de no interrumpir el Servicio de Facturación";
    private static final String FCEXPD = "Favor de tramitar un nuevo certificado ante el SAT y hacerlo llegar al área de soporte de Detisa a fin de reestablecer el Servicio de Facturación";
    private static final String FCNFND = "No se encontró el certificado de Facturación. Favor de reportarlo al área de soporte a fin de continuar con el Servicio de Facturación.";

    private static final String CVWARN = "Después de esta fecha no será posible generar archivos de control volumétrico. Favor de tramitar un nuevo certificado y hacerlo llegar al área de soporte de Detisa a fin de no interrumpir el envío de archivos de Control Volumétricos";
    private static final String CVEXPD = "Favor de tramitar un nuevo certificado y hacerlo llegar al área de soporte de Detisa a fin de reestablecer el servicio de envío de archivos de Control Voumétrico";
    private static final String CVNFND = "No se encontró el certificado de envío de Control Volumétrico. Favor de reportarlo al área de soporte a fin de continuar con el envío de sus archivos de Control Volumétrico.";

    private static final String DATFMT = "EEEE dd 'de' MMMM 'de' yyyy 'a las' HH:mm:ss";
    private static final Locale LOCALE = new Locale("es", "MX");
    private boolean validate(X509Certificate x509Certificate) 
            throws CertificateExpirationWarning, CertificateNotYetValidException, CertificateExpiredException {

        try {
            String serial = new String(x509Certificate.getSerialNumber().toByteArray());
            LogManager.info("Certificado " + serial + " vigente del día " + 
                    new SimpleDateFormat(DATFMT, LOCALE).format(x509Certificate.getNotBefore()) + " al día " +
                    new SimpleDateFormat(DATFMT, LOCALE).format(x509Certificate.getNotAfter()));
            if (new Date().after(new Date(x509Certificate.getNotAfter().getTime() - TimeUnit.DAYS.toMillis(30)))) {
                throw new CertificateExpirationWarning("El certificado " + serial + " expira el día " + new SimpleDateFormat(DATFMT, LOCALE).format(x509Certificate.getNotAfter()));
            }
            x509Certificate.checkValidity();
        } catch (CertificateExpiredException CEE) {
            throw new CertificateExpiredException("El certificado ha expirado. " + CEE.getMessage());
        }
        return true;
    }

    private boolean validateCertificadoFC() {
        String fcFormat = CertificadosDAO.getFCCerFormat();
        X509Certificate certificate;
        String mensaje = "";
        try {
            if (VariablesDAO.hasFacturacion()) {
                certificate = "PEM".equals(fcFormat) ?
                        KeyLoader.loadX509Certificate(new String(CertificadosDAO.getBlobToByteArray("fc_cer"))) :
                        KeyLoader.loadX509Certificate(new ByteArrayInputStream(CertificadosDAO.getBlobToByteArray("fc_cer")));
                return validate(certificate);
            }
        } catch (CertificateExpirationWarning ex) {
            mensaje = "<p style=\"font-size:150%\"><b>Certificado de Facturación:</p><p>" + ex.getMessage() + "<br/><br/>" + FCWARN + "</b></p>";
        } catch (CertificateExpiredException ex) {
            mensaje = "<p style=\"font-size:150%\"><b>Certificado de Facturación:</p><p>" + ex.getMessage() + "<br/><br/>" + FCEXPD + "</b></p>";
        } catch (Exception ex) {
            mensaje = "<p style=\"font-size:150%\"><b>Certificado de Facturación:</p><p>" + ex.getMessage() + "<br/><br/>" + FCNFND + "</b></p>";
        }

        if (!StringUtils.isNVL(mensaje)) {
           MensajesDAO.newMessage(mensaje);
        }
        return false;
    }

    private boolean validateCertificadoCV() {
        String cvFormat = CertificadosDAO.getCVCerFormat();
        X509Certificate certificate;
        String mensaje = "";
        try {
            if (VariablesDAO.hasEnvioCV()) {
                certificate = "PEM".equals(cvFormat) ?
                        KeyLoader.loadX509Certificate(new String(CertificadosDAO.getBlobToByteArray("cv_cer"))) :
                        KeyLoader.loadX509Certificate(new ByteArrayInputStream(CertificadosDAO.getBlobToByteArray("cv_cer")));
                return validate(certificate);
            }
        } catch (CertificateExpirationWarning ex) {
            mensaje = "<p style=\"font-size:150%\"><b>Certificado de Control Volumétrico:</p><p>" + ex.getMessage() + "<br/><br/>" + CVWARN + "</b></p>";
        } catch (CertificateExpiredException ex) {
            mensaje = "<p style=\"font-size:150%\"><b>Certificado de Control Volumétrico:</p><p>" + ex.getMessage() + "<br/><br/>" + CVEXPD + "</b></p>";
        } catch (Exception ex) {
            mensaje = "<p style=\"font-size:150%\"><b>Certificado de Control Volumétrico:</p><p>" + ex.getMessage() + "<br/><br/>" + CVNFND + "</b></p>";
        }

        if (!StringUtils.isNVL(mensaje)) {
            MensajesDAO.newMessage(mensaje);
        }
        return false;
    }

    @Override
    public void run() {
        validateCertificadoCV();
        validateCertificadoFC();
    }
}
