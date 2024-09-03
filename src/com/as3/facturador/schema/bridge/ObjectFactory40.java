package com.as3.facturador.schema.bridge;

import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.marshalling.CFDIJAXBMarshaller;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.sat.cfdi.schema.CMetodoPago;
import com.softcoatl.sat.cfdi.schema.CMoneda;
import com.softcoatl.sat.cfdi.schema.CRegimenFiscal;
import com.softcoatl.sat.cfdi.schema.CTipoDeComprobante;
import com.softcoatl.sat.cfdi.schema.CTipoFactor;
import com.softcoatl.sat.cfdi.schema.CUsoCFDI;
import com.softcoatl.sat.cfdi.schema.Comprobante;
import com.softcoatl.sat.cfdi.v40.CFDI40;
import com.softcoatl.sat.cfdi.v40.schema.Comprobante40;
import com.softcoatl.utils.StringUtils;
import com.softcoatl.utils.logging.LogManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.TimeZone;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class ObjectFactory40 implements ObjectFactory {

    private static final com.softcoatl.sat.cfdi.v40.schema.ObjectFactory FACTORY = new com.softcoatl.sat.cfdi.v40.schema.ObjectFactory();

    private static final String GENERIC_RFC = "XAXX010101000";
    private static final String GENERIC_NAME = "PUBLICO EN GENERAL";

    @Override
    public CFDI createCFDI() {
        return new CFDI40();
    }

    @Override
    public CFDI createCFDI(Comprobante comprobante) throws SQLException, JAXBException, CFDIException {
        return new CFDI40(new CFDIJAXBMarshaller(JAXBContext.newInstance(Comprobante40.class)), (Comprobante40) comprobante);
    }

    @Override
    public Comprobante createComprobante() {
        return FACTORY.createComprobante();
    }

    @Override
    public Comprobante createComprobante(ResultSet resultSet) throws SQLException {

        LogManager.info("Create comprobante 40");
        Comprobante40 comprobante = FACTORY.createComprobante();
        comprobante.setFolio(resultSet.getString("Folio"));
        comprobante.setSerie(StringUtils.isNVL(resultSet.getString("Serie")) ? null : resultSet.getString("Serie"));
        comprobante.setFecha(ObjectFactory.createXMLGregorianCalendar(resultSet.getTimestamp("Fecha", Calendar.getInstance(TimeZone.getDefault())).getTime()));
        comprobante.setTipoDeComprobante(CTipoDeComprobante.I);
        comprobante.setVersion("4.0");
        comprobante.setFormaPago(resultSet.getString("FormaPago"));
        comprobante.setMetodoPago(CMetodoPago.PUE);
        comprobante.setMoneda(CMoneda.MXN);
        comprobante.setTipoCambio(BigDecimal.ONE);
        comprobante.setSubTotal(resultSet.getBigDecimal("Subtotal").setScale(2, RoundingMode.HALF_EVEN));
        if (resultSet.getBigDecimal("Descuento").compareTo(BigDecimal.ZERO)>0) {
            comprobante.setDescuento(resultSet.getBigDecimal("Descuento").setScale(2, RoundingMode.HALF_EVEN));
        }
        comprobante.setTotal(resultSet.getBigDecimal("Total").setScale(2, RoundingMode.HALF_EVEN));
        comprobante.setLugarExpedicion(resultSet.getString("LugarExpedicion"));
        comprobante.setExportacion("01");
        return comprobante;
    }

    @Override
    public Comprobante.InformacionGlobal createComprobanteInformacionGlobal() {
        return FACTORY.createComprobanteInformacionGlobal();
    }

    @Override
    public Comprobante.InformacionGlobal createComprobanteInformacionGlobal(ResultSet resultSet) throws SQLException {
        Comprobante40.InformacionGlobal informacionGlobal = FACTORY.createComprobanteInformacionGlobal();
        informacionGlobal.setPeriodicidad(resultSet.getString("Periodicidad"));
        informacionGlobal.setMeses(resultSet.getString("Meses"));
        informacionGlobal.setAnio(resultSet.getShort("Anio"));
        return informacionGlobal;
    }

    @Override
    public Comprobante.Emisor createComprobanteEmisor() {
        return FACTORY.createComprobanteEmisor();
    }

    @Override
    public Comprobante.Emisor createComprobanteEmisor(ResultSet resultSet) throws SQLException {
        Comprobante40.Emisor emisor = FACTORY.createComprobanteEmisor();
        emisor.setNombre(resultSet.getString("nombre"));
        emisor.setRfc(resultSet.getString("rfc"));
        emisor.setRegimenFiscal(CRegimenFiscal.fromValue(resultSet.getString("regimenFiscal")).value());
        return emisor;
    }

    @Override
    public Comprobante.Receptor createComprobanteReceptor() {
        return FACTORY.createComprobanteReceptor();
    }

    @Override
    public Comprobante.Receptor createComprobanteReceptor(ResultSet resultSet) throws SQLException {
        Comprobante40.Receptor receptor = FACTORY.createComprobanteReceptor();
        receptor.setNombre(resultSet.getString("nombre"));
        receptor.setRfc(resultSet.getString("rfc"));
        receptor.setUsoCFDI(GENERIC_RFC.equals(resultSet.getString("rfc")) ? CUsoCFDI.S_01 : CUsoCFDI.fromValue(resultSet.getString("usoCFDI")));
        receptor.setDomicilioFiscalReceptor(resultSet.getString("domicilioFiscalReceptor"));
        receptor.setRegimenFiscalReceptor(CRegimenFiscal.fromValue(resultSet.getString("regimenFiscalReceptor")).value());
        return receptor;
    }

    @Override
    public Comprobante.Receptor createComprobanteReceptorGenerico(Comprobante comprobante) {
        Comprobante40.Receptor generic = FACTORY.createComprobanteReceptor();
        generic.setRfc(GENERIC_RFC);
        generic.setNombre(GENERIC_NAME);
        generic.setUsoCFDI(CUsoCFDI.S_01);
        generic.setRegimenFiscalReceptor(CRegimenFiscal.RF_616.value());
        generic.setDomicilioFiscalReceptor(((Comprobante40) comprobante).getLugarExpedicion());
        return generic;
    }

    @Override
    public Comprobante40.Conceptos createComprobanteConceptos() {
        return FACTORY.createComprobanteConceptos();
    }

    @Override
    public Comprobante.Conceptos.Concepto createComprobanteConceptosConcepto() {
        return FACTORY.createComprobanteConceptosConcepto();
    }

    @Override
    public Comprobante.Conceptos.Concepto createComprobanteConceptosConcepto(ResultSet resultSet) throws SQLException {
        Comprobante40.Conceptos.Concepto concepto = FACTORY.createComprobanteConceptosConcepto();
        concepto.setNoIdentificacion(resultSet.getString("NoIdentificacion"));
        concepto.setClaveUnidad(resultSet.getString("ClaveUnidad"));
        concepto.setClaveProdServ(resultSet.getString("ClaveProdServ"));
        concepto.setDescripcion(resultSet.getString("Descripcion"));
        concepto.setCantidad(resultSet.getBigDecimal("Cantidad").setScale(6, RoundingMode.HALF_EVEN));
        concepto.setValorUnitario(resultSet.getBigDecimal("ValorUnitario").setScale(6, RoundingMode.HALF_EVEN));
        concepto.setImporte(resultSet.getBigDecimal("Importe").setScale(2, RoundingMode.HALF_EVEN));
        if (resultSet.getBigDecimal("Descuento").compareTo(BigDecimal.ZERO)>0) {
            concepto.setDescuento(resultSet.getBigDecimal("Descuento").setScale(2, RoundingMode.HALF_EVEN));
        }
        concepto.setObjetoImp("02");
        return concepto;
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos createComprobanteConceptosConceptoImpuestos() {
        return FACTORY.createComprobanteConceptosConceptoImpuestos();
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados createComprobanteConceptosConceptoImpuestosTraslados() {
        return FACTORY.createComprobanteConceptosConceptoImpuestosTraslados();
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Retenciones createComprobanteConceptosConceptoImpuestosRetenciones() {
        return FACTORY.createComprobanteConceptosConceptoImpuestosRetenciones();
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Retenciones.Retencion createComprobanteConceptosConceptoImpuestosRetencionesRetencion() {
        return FACTORY.createComprobanteConceptosConceptoImpuestosRetencionesRetencion();
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Retenciones.Retencion createComprobanteConceptosConceptoImpuestosRetencionesRetencion(ResultSet resultSet) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado createComprobanteConceptosConceptoImpuestosTrasladosTraslado() {
        return FACTORY.createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIVA(ResultSet resultSet) throws SQLException {
        Comprobante40.Conceptos.Concepto.Impuestos.Traslados.Traslado iva = FACTORY.createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
        iva.setBase(resultSet.getBigDecimal("base_iva").setScale(2, RoundingMode.HALF_EVEN));
        iva.setImpuesto("002");
        iva.setTasaOCuota(resultSet.getBigDecimal("factoriva"));
        iva.setTipoFactor(CTipoFactor.TASA);
        iva.setImporte(resultSet.getBigDecimal("tax_iva").setScale(2, RoundingMode.HALF_EVEN));
        return iva;
    }

    @Override
    public Comprobante.Conceptos.Concepto.Impuestos.Traslados.Traslado createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIEPS(ResultSet resultSet) throws SQLException {
        Comprobante40.Conceptos.Concepto.Impuestos.Traslados.Traslado ieps = FACTORY.createComprobanteConceptosConceptoImpuestosTrasladosTraslado();
        ieps.setBase(resultSet.getBigDecimal("base_ieps").setScale(2, RoundingMode.HALF_EVEN));
        ieps.setImpuesto("003");
        ieps.setTasaOCuota(resultSet.getBigDecimal("factorieps"));
        ieps.setTipoFactor(CTipoFactor.CUOTA);
        ieps.setImporte(resultSet.getBigDecimal("tax_ieps").setScale(2, RoundingMode.HALF_EVEN));
        return ieps;
    }

    @Override
    public Comprobante.Impuestos createComprobanteImpuestos() {
        return FACTORY.createComprobanteImpuestos();
    }

    @Override
    public Comprobante.Impuestos.Traslados createComprobanteImpuestosTraslados() {
        return FACTORY.createComprobanteImpuestosTraslados();
    }

    @Override
    public Comprobante.Impuestos.Retenciones createComprobanteImpuestosRetenciones() {
        return FACTORY.createComprobanteImpuestosRetenciones();
    }

    @Override
    public Comprobante.Impuestos.Traslados.Traslado createComprobanteImpuestosTrasladosTraslado() {
        return FACTORY.createComprobanteImpuestosTrasladosTraslado();
    }

    public Comprobante.Impuestos.Traslados.Traslado createComprobanteImpuestosTrasladosTraslado(ResultSet resultSet) throws SQLException {
        Comprobante40.Impuestos.Traslados.Traslado traslado = FACTORY.createComprobanteImpuestosTrasladosTraslado();
        traslado.setBase(resultSet.getBigDecimal("Base").setScale(2, RoundingMode.HALF_EVEN));
        traslado.setImporte(resultSet.getBigDecimal("Importe").setScale(2, RoundingMode.HALF_EVEN));
        traslado.setImpuesto(resultSet.getString("Impuesto"));
        traslado.setTasaOCuota(resultSet.getBigDecimal("TasaOCuota"));
        traslado.setTipoFactor(CTipoFactor.fromValue(resultSet.getString("TipoFactor")));
        return traslado;
    }

    @Override
    public Comprobante.Impuestos.Retenciones.Retencion createComprobanteImpuestosRetencionesRetencion() {
        return FACTORY.createComprobanteImpuestosRetencionesRetencion();
    }

    @Override
    public Comprobante.Impuestos.Retenciones.Retencion createComprobanteImpuestosRetencionesRetencion(ResultSet resultSet) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Comprobante40.Complemento createComprobanteComplemento() {
        return FACTORY.createComprobanteComplemento();
    }

    @Override
    public Comprobante40.Addenda createComprobanteAddenda() {
        return FACTORY.createComprobanteAddenda();
    }    
}
