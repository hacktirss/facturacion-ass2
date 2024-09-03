/* 
 * TimbradoProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.process;

import com.as3.facturador.context.FacturadorOmicromContext;
import com.as3.facturador.dao.PACServiceDAO;
import com.as3.facturador.exception.FacturacionException;
import com.softcoatl.utils.logging.LogManager;
import java.nio.charset.StandardCharsets;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.CFDISchemaValidator;
import com.softcoatl.sat.cfdi.CFDIXmlResolver;
import com.softcoatl.sat.cfdi.commons.NamespacesCleaner;
import com.softcoatl.sat.pac.CFDTimbradoPreviamenteException;
import com.softcoatl.sat.pac.PACException;
import com.softcoatl.utils.xml.DocumentParser;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class TimbradoProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final TimbradoProcessor INSTANCE = new TimbradoProcessor();
    }

    public static TimbradoProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private TimbradoProcessor() {}
    
    private ComprobanteDetisa setTimbreFiscalDigital(ComprobanteDetisa comprobante, String xmlTimbrado) throws CFDIException {
        comprobante.setCfdi(new CFDIXmlResolver().resolve(xmlTimbrado, FacturadorOmicromContext.getInstance().getCfdiMapper()));
        comprobante.setXml(xmlTimbrado.getBytes(StandardCharsets.UTF_8));
        comprobante.setUuid(comprobante.getCfdi().getTimbre().getUUID());
        LogManager.debug("XML Timbrado");
        LogManager.debug(xmlTimbrado);
        LogManager.info("Comprobante timbrado con UUID " + comprobante.getUuid() + "...");
        return comprobante;
    }

    private String addTimbre(String xml, String timbre) throws ParserConfigurationException, SAXException, IOException, TransformerException {
        Document xmlDocument = DocumentParser.parseDocument(xml);
        Document tfcDocument = DocumentParser.parseDocument(timbre);
        Node tfcAddopted = xmlDocument.importNode(tfcDocument.getDocumentElement(), true);
        if (xmlDocument.getElementsByTagNameNS("cfdi", "Complemento").getLength()>0) {
            xmlDocument.getElementsByTagNameNS("cfdi", "Complemento").item(0).appendChild(tfcAddopted);
        } else {
            Element complemento = xmlDocument.createElement("cfdi:Complemento");
            complemento.appendChild(tfcAddopted);
            xmlDocument.getDocumentElement().appendChild(complemento);
        }
        return NamespacesCleaner.cleanXML(new ByteArrayInputStream(DocumentParser.toXML(xmlDocument).getBytes(StandardCharsets.UTF_8)));
    }

    private ComprobanteDetisa getTimbre(ComprobanteDetisa comprobante) throws CFDIException {
        try {
            return setTimbreFiscalDigital(comprobante, addTimbre(comprobante.getCfdi().toXML(), new PACServiceDAO().active().doGetTimbre(comprobante.getCfdi())));
        } catch (SQLException | PACException | FacturacionException | ParserConfigurationException | SAXException  | IOException | TransformerException ex) {
            throw new CFDIException(ex);
        }
    }

    @Override
    public ComprobanteDetisa execute(ComprobanteDetisa comprobante) {

        LogManager.info("Timbrando comprobante " + comprobante.getFolio() + "...");
        try {
            LogManager.debug(comprobante.getCfdi().toXML());
            try (InputStream xsltCFDI =  this.getClass().getResourceAsStream(comprobante.getCfdi().getComprobante().getXSD())) {
                CFDISchemaValidator.validate(comprobante.getCfdi().toXML(), xsltCFDI);
            } catch (IOException ex) {
                LogManager.info(ex);
                throw new CFDIException(ex);
            }
            return setTimbreFiscalDigital(comprobante, new PACServiceDAO().active().doTimbrado(comprobante.getCfdi()));
        } catch (CFDTimbradoPreviamenteException ex) {
            try {
                return getTimbre(comprobante);
            } catch (CFDIException ex1) {
                invalid(comprobante, ex.getMessage());
            }
        } catch (FacturacionException | CFDIException | PACException | SQLException ex) {
            LogManager.error("Error timbrando comprobante", ex);
            invalid(comprobante, ex.getMessage());
        }

        return comprobante;
    }
}
