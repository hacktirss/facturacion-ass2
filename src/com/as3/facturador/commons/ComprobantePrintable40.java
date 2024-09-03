package com.as3.facturador.commons;

import com.softcoatl.commons.Currency;
import com.softcoatl.commons.NumberToLetterConvertionException;
import com.softcoatl.commons.NumericalCurrencyConverter;
import com.softcoatl.commons.SpanishNumbers;
import com.softcoatl.sat.cfdi.v40.CFDI40;
import com.softcoatl.sat.cfdi.schema.CTipoRelacion;
import com.softcoatl.sat.cfdi.v40.schema.Comprobante40;
import com.softcoatl.sat.cfdi.schema.CFormaPago;
import java.math.BigDecimal;

public class ComprobantePrintable40 implements ComprobantePrintable {

    protected final CFDI40 cfdi;

    public ComprobantePrintable40(CFDI40 cfdi) {
        this.cfdi = cfdi;
    }

    @Override
    public String getDescripcionTipoComprobante() {
        return ((Comprobante40) cfdi.getComprobante()).getTipoDeComprobante().getDescripcion();
    }

    @Override
    public String getDescripcionUsoCFDI() {
        return ((Comprobante40) cfdi.getComprobante()).getReceptor().getUsoCFDI().getDescripcion();
    }
    
    @Override
    public String getTotalLetra() throws NumberToLetterConvertionException {
        return "USD".equals(((Comprobante40) cfdi.getComprobante()).getMoneda().name()) ?
            new NumericalCurrencyConverter(new SpanishNumbers(), new Currency("DÓLARES", "DÓLAR")).convert(((Comprobante40) cfdi.getComprobante()).getTotal())+" USD" :
            new NumericalCurrencyConverter(new SpanishNumbers(), new Currency("PESOS", "PESO")).convert(((Comprobante40) cfdi.getComprobante()).getTotal())+" M.N.";
    }
    
    @Override
    public String getDescripcionFormaDePago() {
        return ((Comprobante40) cfdi.getComprobante()).getFormaPago() == null ? "" : CFormaPago.fromValue(((Comprobante40) cfdi.getComprobante()).getFormaPago()).getDescripcion();
    }

    @Override
    public String getDescripcionMetodoDePago() {
        return ((Comprobante40) cfdi.getComprobante()).getMetodoPago() == null ? "" : ((Comprobante40) cfdi.getComprobante()).getMetodoPago().getDescripcion();
    }

    @Override
    public String getDescripcionTipoRelacion() {
        return ((Comprobante40) cfdi.getComprobante()).getCfdiRelacionados() == null ? "" :
                    CTipoRelacion.fromValue(((Comprobante40) cfdi.getComprobante()).getCfdiRelacionados().getTipoRelacion()).getDescripcion();
    }

    @Override
    public BigDecimal getImporteTotalDocumento() {
        return ((Comprobante40) cfdi.getComprobante()).getTotal();
    }

    @Override
    public String getFolioDocumento() {
        String serie = ((Comprobante40) cfdi.getComprobante()).getSerie();
        String folio = ((Comprobante40) cfdi.getComprobante()).getFolio();
        return (serie == null ? folio : serie + "-" + folio);
    }
}
