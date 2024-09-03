/* 
 * DocType
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.server;

public enum DocType {

    AN,  // Factura de Anticipo
    FA,  // Factura
    CR,  // Nota de Crédito
    RP,  // Recibo de Pagos
    FG,  // Factura Global
    TR,  // CFDI de Traslado
    CPI; // CFDI de Ingresos con Carta Porte

    public String getFormatName() {
        switch (this) {
            case FA:
            case FG:
                return "FACTURA";
            case CR:
                return "NOTA DE CREDITO";
            case RP:
                return "RECIBO DE PAGOS";
            case AN:
                return "FACTURA DE ANTICIPO";
            case TR:
                return "CFDI DE TRASLADO";
            case CPI:
                return "CARTA PORTE";
            default:
                return "";
        }
    }
}
