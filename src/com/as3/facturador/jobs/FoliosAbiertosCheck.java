/*
 * FoliosAbiertosCheck
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
package com.as3.facturador.jobs;

import com.as3.facturador.dao.BitacoraDAO;
import com.as3.facturador.dao.FacturaDAO;
import com.softcoatl.utils.logging.LogManager;

public class FoliosAbiertosCheck  implements Runnable {

    @Override
    public void run() {
        try {
            BitacoraDAO.vencidos();
            FacturaDAO.liberaFoliosVencidos();
        } catch (RuntimeException rte) {
            LogManager.fatal("Error fatal de ejecución ", rte);
        } catch (Throwable t ){
            LogManager.fatal("Error inesperado ", t);
        }
    }
}
