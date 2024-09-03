/*
 * BaseProcessor
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

import com.softcoatl.utils.logging.LogManager;
import lombok.Setter;
import com.as3.facturador.schema.ComprobanteDetisa;

public abstract class BaseProcessor implements FacturaProcessor {

    @Setter protected FacturaProcessor next;

    abstract ComprobanteDetisa execute(ComprobanteDetisa comprobante);

    protected ComprobanteDetisa nextExecution(ComprobanteDetisa comprobante) {
        return next!=null ? next.process(comprobante) : comprobante;
    }

    protected void invalid(ComprobanteDetisa comprobante, String cause) {
        comprobante.setValid(false);
        comprobante.setError(cause);
    }

    @Override
    public ComprobanteDetisa process(ComprobanteDetisa comprobante) {
        LogManager.info("Procesando comprobante " + this.getClass());
        return comprobante.isValid() ?  nextExecution(execute(comprobante)) : comprobante;
    }
}
