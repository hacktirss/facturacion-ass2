/*
 * CertificateExpirationWarning
 * FacturadorOmicrom®
 * © 2020, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since ene 2020 
 */
package com.as3.facturador.exception;

public class CertificateExpirationWarning extends FacturacionException {

    private static final long serialVersionUID=1L;

    public CertificateExpirationWarning() {
        super();
    }

    public CertificateExpirationWarning(String arg0) {
        super(arg0);
    }
}
