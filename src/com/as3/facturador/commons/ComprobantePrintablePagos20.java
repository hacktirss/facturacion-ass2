package com.as3.facturador.commons;

import com.softcoatl.commons.Currency;
import com.softcoatl.commons.NumberToLetterConvertionException;
import com.softcoatl.commons.NumericalCurrencyConverter;
import com.softcoatl.commons.SpanishNumbers;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.CFormaPago;
import com.softcoatl.sat.cfdi.schema.complemento.pagos.Pagos20;
import com.softcoatl.sat.cfdi.v40.CFDI40;
import com.softcoatl.sat.cfdi.v40.schema.Comprobante40;
import com.softcoatl.utils.logging.LogManager;

public class ComprobantePrintablePagos20 extends ComprobantePrintable40 {

    public ComprobantePrintablePagos20(CFDI40 cfdi) {
        super(cfdi);
    }

    @Override
    public String getTotalLetra() throws NumberToLetterConvertionException {
        try {
            Pagos20 pagos = cfdi.getComplementoOfType(Pagos20.NAMESPACE, Pagos20.class);
            return "USD".equals(((Comprobante40) cfdi.getComprobante()).getMoneda().name()) ?
                    new NumericalCurrencyConverter(new SpanishNumbers(), new Currency("DÓLARES", "DÓLAR")).convert(pagos.getTotalPagos())+" USD" :
                    new NumericalCurrencyConverter(new SpanishNumbers(), new Currency("PESOS", "PESO")).convert(pagos.getTotalPagos())+" M.N.";
        } catch (CFDIException ex) {
            throw new NumberToLetterConvertionException(ex);
        }
    }

    @Override
    public String getDescripcionFormaDePago() {
        try {
            Pagos20 pagos = cfdi.getComplementoOfType(Pagos20.NAMESPACE, Pagos20.class);
            LogManager.info(pagos.getPago().get(0).getFormaDePagoP());
            return pagos.getPago().get(0).getFormaDePagoP() + " - " + CFormaPago.fromValue(pagos.getPago().get(0).getFormaDePagoP()).getDescripcion();
        } catch (CFDIException ex) {
            LogManager.error(ex);
        }
        return "";
    }
}
