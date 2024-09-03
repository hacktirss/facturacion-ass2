/**
 * PDFGeneratorProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.commons.TemplateLoader;
import com.as3.facturador.commons.XSLTemplate;
import com.as3.facturador.exception.CertificadoReaderException;
import com.as3.facturador.server.Format;
import com.google.zxing.WriterException;
import com.softcoatl.commons.NumberToLetterConvertionException;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.utils.logging.LogManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.events.EventFormatter;
import com.as3.facturador.schema.ComprobanteDetisa;
import java.io.File;
import javax.xml.bind.JAXBException;

public class PDFGeneratorProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final PDFGeneratorProcessor INSTANCE = new PDFGeneratorProcessor();
    }

    public static PDFGeneratorProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private PDFGeneratorProcessor() {}

    private Fop getFop(OutputStream os) 
            throws FOPException {
        FopFactory fopFactory = FopFactory.newInstance((new File(".")).toURI());
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.getEventBroadcaster().addEventListener(event -> LogManager.debug(EventFormatter.format(event)));
        return fopFactory.newFop("application/pdf", foUserAgent, os);
    }

    private byte[] generatePDF(ComprobanteDetisa cfdi) 
            throws CFDIException, FOPException, IOException, JAXBException, TransformerException, WriterException, NumberToLetterConvertionException, CertificadoReaderException, SQLException {
        XSLTemplate template = TemplateLoader.getTemplate(cfdi);
        ByteArrayOutputStream bosPDF = new ByteArrayOutputStream();
        Fop fop = getFop(bosPDF);
        LogManager.info("Generando PDF");
        template.getTransformer().transform(new StreamSource(new ByteArrayInputStream(cfdi.getXml())), new SAXResult(fop.getDefaultHandler()));
        return bosPDF.toByteArray();
     }

    private String generateTXT(ComprobanteDetisa cfdi) 
            throws CFDIException, FOPException, IOException, JAXBException, TransformerException, WriterException, NumberToLetterConvertionException, CertificadoReaderException, SQLException {
        XSLTemplate template = TemplateLoader.getPOSTemplate(cfdi);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        LogManager.info("Generando formato POS");
        template.getTransformer().transform(new StreamSource(new ByteArrayInputStream(cfdi.getXml())), new StreamResult(bos));
        LogManager.debug(new String(bos.toByteArray(), StandardCharsets.UTF_8));
        return new String(bos.toByteArray(), StandardCharsets.UTF_8);
    }

    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {

        try {
            LogManager.info("Generando Representación impresa");
            comprobante.setTxt(Format.TX.equals(comprobante.getFormat()) ? generateTXT(comprobante) : "");
            comprobante.setPdf(comprobante.isPdfGenerator() ? generatePDF(comprobante) : null);
        } catch (CertificadoReaderException | WriterException | NumberToLetterConvertionException | CFDIException | IOException | SQLException | JAXBException | TransformerException | FOPException ex) {
            LogManager.error("Error generando PDF", ex);
            comprobante.setError(ex.getMessage());
        }
        return comprobante;
    }

    @Override
    public ComprobanteDetisa process(ComprobanteDetisa comprobante) {
        LogManager.info("Procesando comprobante " + this.getClass());
        return comprobante.isValid() ?  nextExecution(execute(comprobante)) : comprobante;
    }
}
