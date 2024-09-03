package com.as3.facturador.commons;

import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.complemento.pagos.Pagos20;
import com.softcoatl.sat.cfdi.v40.CFDI40;
import com.softcoatl.sat.cfdi.schema.CFDI;

public class ComprobantePrintableFactory {

    public ComprobantePrintable instanciate(CFDI comprobante) throws CFDIException {
        if(comprobante instanceof CFDI40) {
            return comprobante.hasComplementoOfType(Pagos20.NAMESPACE, Pagos20.class) ? 
                    createComprobantePrintablePagos20((CFDI40) comprobante) :
                    createComprobantePrintable40((CFDI40) comprobante);
        }
        return null;
    }
    public ComprobantePrintable40 createComprobantePrintable40(CFDI40 comprobante) {
        return new ComprobantePrintable40(comprobante);
    } 
    public ComprobantePrintablePagos20 createComprobantePrintablePagos20(CFDI40 comprobante) {
        return new ComprobantePrintablePagos20(comprobante);
    } 
}
