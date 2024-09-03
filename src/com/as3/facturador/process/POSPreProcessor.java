/*
 * POSPreProcessor
 * FacturadorOmicrom®
 * © 2021, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since jun 2021 
 */
package com.as3.facturador.process;

import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.exception.FacturacionException;
import com.softcoatl.utils.logging.LogManager;
import com.as3.facturador.schema.ComprobanteDetisa;

public class POSPreProcessor extends BaseProcessor implements FacturaProcessor {

    private static final class Loader {
        static final POSPreProcessor INSTANCE = new POSPreProcessor();
    }

    public static POSPreProcessor getInstance() {
        return Loader.INSTANCE;
    }

    private POSPreProcessor() {}

    @Override
    ComprobanteDetisa execute(ComprobanteDetisa comprobante) {

        if (comprobante.getCliente().isInvalid()) {
            invalid(comprobante, "Error de Parametros. 0 No es un numero de cliente valido.");
            return comprobante;
        }

        if (comprobante.getCliente().isDisabled()) {
            invalid(comprobante, "Error de Parametros. El cliente " + comprobante.getCliente().getRazonSocial() + " no tiene permiso para facturar.");
            return comprobante;
        }

        if (comprobante.getDespachador().isInvalid()) {
            invalid(comprobante, "Error de Parametros. Password de despachador incorrecto.");
            return comprobante;
        }

        try {
            return new FacturaDAO().cfdiLastPosition(comprobante);
        } catch (FacturacionException ex) {
            LogManager.error("EX", ex);
            invalid(comprobante, ex.getMessage());
        }
        return comprobante;
    }
}
