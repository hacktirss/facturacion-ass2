/*
 * CancelacionProcessFactory
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.process;

public class CancelacionProcessFactory implements ProcessFactory {

    @Override
    public FacturaProcessor create() {
        return CancelacionProcessor.getInstance();
    }
    
}
