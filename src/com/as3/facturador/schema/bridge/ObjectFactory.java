package com.as3.facturador.schema.bridge;

import com.softcoatl.context.APPContext;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.sat.cfdi.schema.Comprobante;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.GregorianCalendar;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public interface ObjectFactory {

    public static ObjectFactory getInstance() throws ReflectiveOperationException {
        return (ObjectFactory) Class.forName(APPContext.getInitParameter("CFDIObjectFactory")).getConstructor().newInstance();
    }

    public static XMLGregorianCalendar createXMLGregorianCalendar(long time) throws SQLException {
        try {
            GregorianCalendar gregorianCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
            gregorianCalendar.setTimeInMillis(time);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException ex) {
            throw new SQLException(ex);
        }
        
    }

    public CFDI createCFDI();
    public CFDI createCFDI(Comprobante comprobante) throws SQLException, JAXBException, CFDIException;
    public Comprobante createComprobante();
    public Comprobante createComprobante(ResultSet resultSet) throws SQLException;
    public Comprobante.InformacionGlobal createComprobanteInformacionGlobal();
    public Comprobante.InformacionGlobal createComprobanteInformacionGlobal(ResultSet resultSet) throws SQLException;
    public Comprobante.Emisor createComprobanteEmisor();
    public Comprobante.Emisor createComprobanteEmisor(ResultSet resultSet) throws SQLException;
    public Comprobante.Receptor createComprobanteReceptor();
    public Comprobante.Receptor createComprobanteReceptor(ResultSet resultSet) throws SQLException;
    public Comprobante.Receptor createComprobanteReceptorGenerico(Comprobante comprobante);
    public Comprobante.Conceptos createComprobanteConceptos();
    public Comprobante.Conceptos.Concepto createComprobanteConceptosConcepto();
    public Comprobante.Conceptos.Concepto createComprobanteConceptosConcepto(ResultSet resultSet) throws SQLException;
    public Comprobante.Conceptos.Concepto.Impuestos createComprobanteConceptosConceptoImpuestos();
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados createComprobanteConceptosConceptoImpuestosTraslados();
    public Comprobante.Conceptos.Concepto.Impuestos.Retenciones createComprobanteConceptosConceptoImpuestosRetenciones();
    public Comprobante.Conceptos.Concepto.Impuestos.Retenciones.Retencion createComprobanteConceptosConceptoImpuestosRetencionesRetencion();
    public Comprobante.Conceptos.Concepto.Impuestos.Retenciones.Retencion createComprobanteConceptosConceptoImpuestosRetencionesRetencion(ResultSet resultSet) throws SQLException;
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIVA(ResultSet resultSet) throws SQLException;
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIEPS(ResultSet resultSet) throws SQLException;
    public Comprobante.Impuestos createComprobanteImpuestos();
    public Comprobante.Impuestos.Traslados createComprobanteImpuestosTraslados();
    public Comprobante.Impuestos.Retenciones createComprobanteImpuestosRetenciones();
    public Comprobante.Impuestos.Traslados.Traslado createComprobanteImpuestosTrasladosTraslado();
    public Comprobante.Impuestos.Traslados.Traslado createComprobanteImpuestosTrasladosTraslado(ResultSet resultSet) throws SQLException;
    public Comprobante.Impuestos.Retenciones.Retencion createComprobanteImpuestosRetencionesRetencion();
    public Comprobante.Impuestos.Retenciones.Retencion createComprobanteImpuestosRetencionesRetencion(ResultSet resultSet) throws SQLException;
    public Comprobante.Complemento createComprobanteComplemento();
    public Comprobante.Addenda createComprobanteAddenda();
}
