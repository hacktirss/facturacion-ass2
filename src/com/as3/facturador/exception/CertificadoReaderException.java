/*
 * CertificadoReaderException
 * FacturadorOmicrom®
 * © 2020, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since ene 2020 
 */
package com.as3.facturador.exception;

public class CertificadoReaderException extends FacturacionException {

    private static final long serialVersionUID=1L;

    public CertificadoReaderException() {
        super();
    }

    public CertificadoReaderException(String string) {
        super(string);
    }

    public CertificadoReaderException(String string, Throwable thrwbl) {
        super(string, thrwbl);
    }

    public CertificadoReaderException(Throwable thrwbl) {
        super(thrwbl);
    }
}
