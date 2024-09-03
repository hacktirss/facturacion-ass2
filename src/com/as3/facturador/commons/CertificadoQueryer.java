/*
 * CertificadoQueryer
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.commons;

import com.detisa.dao.CertificadosDAO;
import com.as3.facturador.exception.CertificadoReaderException;
import com.softcoatl.security.KeyLoader;
import com.softcoatl.utils.logging.LogManager;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

public class CertificadoQueryer  implements CertificadoReader {

    private Certificates certificates = null;

    @Override
    public Certificates getCertificates() throws CertificadoReaderException {
        if (certificates == null) {
            try {
                LogManager.info("Cargando certificados...");
                certificates = load();
                LogManager.info("Certificados cargados...");
            } catch (IOException | GeneralSecurityException ex) {
                throw new CertificadoReaderException(ex);
            }
        }
        return certificates;
    }

    private static Certificates load() throws IOException, GeneralSecurityException {
        Certificates certificados = new Certificates();
        String keypass = CertificadosDAO.getFCKeyPass();
        certificados.setCertificate(
                "PEM".equals(CertificadosDAO.getFCKeyFormat()) ? 
                        KeyLoader.loadCertificates(
                                new String(CertificadosDAO.getBlobToByteArray("fc_cer"), StandardCharsets.UTF_8), 
                                new String(CertificadosDAO.getBlobToByteArray("fc_key"), StandardCharsets.UTF_8), 
                                keypass) : 
                        KeyLoader.loadCertificates(
                                new ByteArrayInputStream(CertificadosDAO.getBlobToByteArray("fc_cer")), 
                                new ByteArrayInputStream(CertificadosDAO.getBlobToByteArray("fc_key")), 
                                keypass));
        certificados.setImage(CertificadosDAO.getBlobToByteArray("fc_img"));
        return certificados;
    }
}
