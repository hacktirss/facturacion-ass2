/*
 * XSLTemplate
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since oct 2019 
 */
package com.as3.facturador.commons;

import com.softcoatl.utils.StringUtils;
import com.softcoatl.utils.logging.LogManager;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.codec.binary.Base64;

@Setter @ToString
public class XSLTemplate {

    protected Source xsl;
    protected String tipoDocumento;
    protected String tipoRelacion;
    protected String cadenaOriginal;
    protected String importeLetra;
    protected byte[] logo;
    protected byte[] qrc;

    private static TransformerFactory factory;

    protected static TransformerFactory getFactory() throws TransformerConfigurationException {
        if (factory == null) {
            factory = TransformerFactory.newInstance("net.sf.saxon.TransformerFactoryImpl", null);
            factory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);
            factory.setErrorListener(new ErrorListener() {
                @Override
                public void warning(TransformerException arg0) throws TransformerException {
                    LogManager.warn(arg0);
                }

                @Override
                public void error(TransformerException arg0) throws TransformerException {
                    LogManager.error(arg0);
                }

                @Override
                public void fatalError(TransformerException arg0) throws TransformerException {
                    LogManager.fatal(arg0);
                }
            });
        }
        LogManager.info("Factory " + factory.getClass());
        return factory;
    }

    public Transformer getTransformer() throws TransformerConfigurationException {
        Transformer transformer = XSLTemplate.getFactory().newTransformer(this.xsl);
        transformer.setParameter("tipoDocumento", this.tipoDocumento);
        transformer.setParameter("cadenaOriginal", this.cadenaOriginal);
        transformer.setParameter("importeLetra", this.importeLetra);
        transformer.setParameter("logo", "url('data:image/png;base64," + Base64.encodeBase64String(this.logo) + "')");
        transformer.setParameter("qrc", "url('data:image/png;base64," + Base64.encodeBase64String(this.qrc) + "')");
        if (!StringUtils.isNVL(this.tipoRelacion)) {
            transformer.setParameter("tipoRelacion", this.tipoRelacion);
        }
        return transformer;
    }
}
