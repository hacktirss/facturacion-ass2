/*
 * CertificadoReader
 * FacturadorOmicrom®
 * © 2020, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since ene 2020 
 */
package com.as3.facturador.commons;

import com.as3.facturador.exception.CertificadoReaderException;

public interface CertificadoReader {
    
    public Certificates getCertificates() throws CertificadoReaderException;
}
