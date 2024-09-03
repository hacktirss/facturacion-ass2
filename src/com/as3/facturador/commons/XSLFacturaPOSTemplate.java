/*
 * XSLFacturaPOSTemplate
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since oct 2019 
 */
package com.as3.facturador.commons;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;

@Setter @ToString
public class XSLFacturaPOSTemplate extends XSLTemplate {

    private String tipoComprobante;
    private String usoCFDI;
    private String regimenFiscal;
    private String regimenFiscalReceptor;
    private String formaPago;
    private String metodoPago;
    private String qrcs;

    @Override
    public Transformer getTransformer() throws TransformerConfigurationException {
        Transformer transformer = XSLFacturaPOSTemplate.getFactory().newTransformer(xsl);
        transformer.setParameter("tipoComprobante", tipoComprobante);
        transformer.setParameter("usoCFDI", usoCFDI);
        transformer.setParameter("regimenFiscal", regimenFiscal);
        if (regimenFiscalReceptor!=null) {
            transformer.setParameter("regimenFiscalReceptor", regimenFiscalReceptor);
        }
        if (formaPago!=null) {
            transformer.setParameter("formaPago", formaPago);
        }
        transformer.setParameter("metodoPago", metodoPago);
        transformer.setParameter("cadenaOriginal", cadenaOriginal);
        transformer.setParameter("importeletra", importeLetra);
        transformer.setParameter("qrc", qrcs);
        transformer.setParameter("qrcB64", Base64.encodeBase64String(qrcs.getBytes()));
        return transformer;
    }
}
