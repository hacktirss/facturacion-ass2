/*
 * TemplateLoader
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since oct 2019 
 */
package com.as3.facturador.commons;

import com.as3.facturador.dao.CompaniaDAO;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.CertificadoReaderException;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.as3.facturador.server.DocType;
import static com.as3.facturador.server.DocType.TR;
import com.as3.facturador.server.Format;
import com.as3.facturador.vo.CompaniaVO;
import com.google.zxing.WriterException;
import com.softcoatl.commons.NumberToLetterConvertionException;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.complemento.pagos.Pagos20;
import com.softcoatl.utils.logging.LogManager;
import com.softcoatl.utils.qrc.QRCEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamSource;

public class TemplateLoader {

    private static final CompaniaVO EMISOR = CompaniaDAO.get();
    private static final QRCEncoder QRCENCODER = new QRCEncoder();
    private static final CertificadoReader CER_READER = (new CertificadoReaderFactory()).getReader();
    public static final String POS_KEY33 = "xsl_ticketpos";
    public static final String POS_KEY40 = "xsl_ticketpos_40";

    public static final int COLOR = 0x6BA5D9;

    private TemplateLoader() {}

    private static Source getSource(String key) {
        String sql = "SELECT file FROM sys_files WHERE key_file = ?";
        LogManager.info("Loading source for key " + key);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, key);
            try ( ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new StreamSource(new ByteArrayInputStream(rs.getString("file").getBytes()));
                }
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return null;
    }

    private static String getKey(Format format, DocType type, String version) {

        switch (format) {
            case A1:
                switch (type) {
                    case CPI: 
                    case TR: return "xsl_cartaporte";
                    case FG: 
                        switch (version) {
                            case "3.3": return "xsl_general";
                            case "4.0": return "xsl_general40";
                            default: return null;
                        }
                    case RP: 
                        switch (version) {
                            case "3.3": return "xsl_recibo";
                            case "4.0": return "xsl_recibo40";
                            default: return null;
                        }
                    default:
                        switch (version) {
                            case "3.2": return "xsl_factura32";
                            case "3.3": return "xsl_factura33";
                            case "4.0": return "xsl_factura40";
                            default: return null;
                        }
                }
            case TC:
            case TX:
                    switch (version) {
                        case "3.2": return "xsl_ticket32";
                        case "3.3": return "xsl_ticket33";
                        case "4.0": return "xsl_ticket40";
                        default: return null;
                    }
            default: return null;
        }
    }

    public static XSLTemplate getPOSTemplate(ComprobanteDetisa cfdi) 
            throws CFDIException, IOException, JAXBException, TransformerException, WriterException, NumberToLetterConvertionException, CertificadoReaderException, SQLException {
        XSLFacturaPOSTemplate template = new XSLFacturaPOSTemplate();
        ComprobantePrintable printable = new ComprobantePrintableFactory().instanciate(cfdi.getCfdi());
        LogManager.info("Generando POS Plain Format v " + cfdi.getVersion() + " para " + cfdi.getCfdi().getTimbre().getUUID());
        template.setXsl(getSource(cfdi.getVersion().equals("3.3") ? POS_KEY33 : POS_KEY40));
        template.setTipoComprobante(printable.getDescripcionTipoComprobante());
        template.setRegimenFiscal(EMISOR.getRegimenLetra());
        template.setRegimenFiscalReceptor(cfdi.getCliente().getRegimenFiscalLetra());
        template.setUsoCFDI(printable.getDescripcionUsoCFDI());
        template.setCadenaOriginal(cfdi.getCfdi().getCadenaOriginalTFD());
        template.setImporteLetra(printable.getTotalLetra());
        template.setQrcs(cfdi.getCfdi().getValidationURL());
        template.setFormaPago(printable.getDescripcionFormaDePago());
        template.setMetodoPago(printable.getDescripcionMetodoDePago());
        return template;
    }

    public static XSLTemplate getTemplate(ComprobanteDetisa cfdi) 
            throws CFDIException, IOException, JAXBException, TransformerException, WriterException, NumberToLetterConvertionException, CertificadoReaderException, SQLException {
        LogManager.info("Generando PDF v " + cfdi.getVersion() + " para " + cfdi.getCfdi().getTimbre().getUUID());
        if (cfdi.getCfdi().hasComplementoOfType(Pagos20.NAMESPACE, Pagos20.class)) {
            return getTemplateRecibo(cfdi);
        }
        XSLFacturaTemplate template = new XSLFacturaTemplate();
        ComprobantePrintable printable = new ComprobantePrintableFactory().instanciate(cfdi.getCfdi());
        ByteArrayOutputStream bosQRC = new ByteArrayOutputStream();
        QRCENCODER.encodev2(cfdi.getCfdi().getValidationURL(), "png", bosQRC, Format.A1.equals(cfdi.getFormat()) ? 7054809 : 0);
        template.setXsl(getSource(getKey(cfdi.getFormat(), cfdi.getDocType(), cfdi.getVersion())));
        template.setRegimenFiscal(EMISOR.getRegimenLetra());
        template.setRegimenFiscalReceptor(cfdi.getCliente().getRegimenFiscalLetra());
        template.setTipoComprobante(printable.getDescripcionTipoComprobante());
        template.setTipoDocumento(cfdi.getDocType().getFormatName());
        template.setUsoCFDI(printable.getDescripcionUsoCFDI());
        template.setCadenaOriginal(cfdi.getCfdi().getCadenaOriginalTFD());
        template.setImporteLetra(printable.getTotalLetra());
        template.setQrc(bosQRC.toByteArray());
        template.setLogo(CER_READER.getCertificates().getImage());
        template.setFormaPago(printable.getDescripcionFormaDePago());
        template.setMetodoPago(printable.getDescripcionMetodoDePago());
        template.setTipoRelacion(printable.getDescripcionTipoRelacion());
        if ("1".equals(VariablesDAO.getVariable("pdf_print_ubicacion"))) {
            template.setDireccionLetra(EMISOR.getDireccion());
        }
        return template;
    }

    public static XSLTemplate getTemplateRecibo(ComprobanteDetisa cfdi) 
            throws CFDIException, IOException, JAXBException, TransformerException, WriterException, NumberToLetterConvertionException, CertificadoReaderException, SQLException {
        XSLReciboPagoTemplate template = new XSLReciboPagoTemplate();
        ComprobantePrintable printable = new ComprobantePrintableFactory().instanciate(cfdi.getCfdi());
        ByteArrayOutputStream bosQRC = new ByteArrayOutputStream();
        LogManager.info("Generando PDF v " + cfdi.getVersion() + " para Recibo de Pago " + cfdi.getCfdi().getTimbre().getUUID());
        QRCENCODER.encodev2(cfdi.getCfdi().getValidationURL(), "png", bosQRC, 7054809);
        template.setXsl(getSource(getKey(cfdi.getFormat(), cfdi.getDocType(), cfdi.getVersion())));
        template.setRegimenFiscal(EMISOR.getRegimenLetra());
        template.setRegimenFiscalReceptor(cfdi.getCliente().getRegimenFiscalLetra());
        template.setTipoComprobante(printable.getDescripcionTipoComprobante());
        template.setTipoDocumento(cfdi.getDocType().getFormatName());
        template.setUsoCFDI(printable.getDescripcionUsoCFDI());
        template.setCadenaOriginal(cfdi.getCfdi().getCadenaOriginalTFD());
        template.setImporteLetra(printable.getTotalLetra());
        template.setFormaPago(printable.getDescripcionFormaDePago());
        template.setGranTotal(cfdi.getImporteTotal());
        template.setQrc(bosQRC.toByteArray());
        template.setLogo(CER_READER.getCertificates().getImage());
        template.setTipoRelacion(printable.getDescripcionTipoRelacion());
        if ("1".equals(VariablesDAO.getVariable("pdf_print_ubicacion"))) {
            template.setDireccionLetra(EMISOR.getDireccion());
        }
        return template;
    }
}
