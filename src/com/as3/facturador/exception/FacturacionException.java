/*
 * FacturacionException
 * FacturadorOmicrom®
 * © 2020, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since ene 2020 
 */
package com.as3.facturador.exception;

public class FacturacionException extends Exception {

    private static final long serialVersionUID=1L;

    public FacturacionException() {
        super();
    }

    public FacturacionException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public FacturacionException(Throwable thrwbl) {
        super(thrwbl);
    }

    public FacturacionException(String message) {
        super(message);
    }
}
