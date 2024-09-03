/*
 * XSLReciboPagoTemplate
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since oct 2019 
 */
package com.as3.facturador.commons;

import com.softcoatl.utils.StringUtils;
import java.math.BigDecimal;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import lombok.Setter;
import lombok.ToString;

@Setter @ToString
public class XSLReciboPagoTemplate extends XSLTemplate {

    private String formaPago;
    private String metodoPago;
    private String tipoComprobante;
    private String direccionLetra;
    private String usoCFDI;
    private String regimenFiscal;
    private String regimenFiscalReceptor;
    private BigDecimal granTotal;

    @Override
    public Transformer getTransformer() throws TransformerConfigurationException {
        Transformer transformer = super.getTransformer();
        
        transformer.setParameter("tipoComprobante", tipoComprobante);
        transformer.setParameter("usoCFDI", usoCFDI);
        transformer.setParameter("regimenFiscal", regimenFiscal);
        if (formaPago!=null) {
            transformer.setParameter("formaPago", formaPago);
        }
        if (metodoPago!=null) {
            transformer.setParameter("metodoPago", metodoPago);
        }
        if (regimenFiscalReceptor!=null) {
            transformer.setParameter("regimenFiscalReceptor", regimenFiscalReceptor);
        }
        transformer.setParameter("granTotal", granTotal.toPlainString());
        if (!StringUtils.isNVL(direccionLetra)) {
            transformer.setParameter("direccion", this.direccionLetra);
        }
        return transformer;
    }
}
