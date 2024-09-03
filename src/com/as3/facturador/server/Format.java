/* 
 * Format
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.server;

public enum Format {
    
    A1,     // Formato Carta
    TC,     // Formato Ticket
    TX,     // Formato Plano para POS
    OX,     // Sólo XML
    NN;     // Sin adjuntos
    public boolean genPDF() {
        return !NN.equals(this) && !OX.equals(this);
    }
    public boolean genXML() {
        return !NN.equals(this);
    }
}
