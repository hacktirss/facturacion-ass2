/*
 * FacturaWindow
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dic 2019 
 */
package com.as3.facturador.jobs;

import com.as3.facturador.dao.BitacoraDAO;
import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.CertificadoReaderException;
import com.as3.facturador.process.FacturaProcessor;
import com.as3.facturador.process.TimbradoProcessFactory;
import com.as3.facturador.scheduler.FacturadorScheduler;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.as3.facturador.server.DocType;
import com.as3.facturador.server.Format;
import com.as3.facturador.global.FacturaPublicoGeneral;
import com.as3.facturador.global.FacturaPublicoGeneralFactory;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.utils.logging.LogManager;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FacturaWindow implements Runnable {

    private final FacturaProcessor processor;

    private static final String ENABLED = "fact_global_enabled";
    private static final String WINDOW = "fact_global_window";

    public FacturaWindow() throws CertificadoReaderException {
        processor = new TimbradoProcessFactory().create();
    }

    
    @Override
    public void run() {
        int ventana = Integer.parseInt(VariablesDAO.getVariable(WINDOW, "1"));
        // Programa la siguiente ejecución del job
        LogManager.info("Programando siguiente ejecución");
        ScheduledFuture next = FacturadorScheduler.getInstance().schedule(this, ventana == 0 ? 1 :  ventana, TimeUnit.HOURS);
        try {
            if ("1".equals(VariablesDAO.getVariable(ENABLED))) {
                if (VariablesDAO.hasStarted()) {
                    FacturaPublicoGeneral fpg = FacturaPublicoGeneralFactory.instanciate();
                    FacturaDAO dao = new FacturaDAO();
                    if (!fpg.createComprobantes()) {
                        LogManager.info("Ya se ejecutó el proceso correspondiente");
                        BitacoraDAO.evento("Ya se ejecutó el proceso correspondiente");
                    }
                    /** Bug Fixed Execute Previous CFDI */
                    fpg.comprobantes().forEach((Integer id) -> {
                        try {
                            ComprobanteDetisa comprobante = dao.getComprobantePublicoEnGeneral(id);
                            if (comprobante.getCfdi().hasConceptos()) {
                                comprobante.setFormat(Format.A1);
                                comprobante.setDocType(DocType.FG);
                                comprobante.setMailer(false);
                                comprobante.setPdfGenerator(false);
                                processor.process(comprobante);
                                BitacoraDAO.evento("CFDI generado " + comprobante.getUuid());
                            }
                        } catch (SQLException | CFDIException ex) {
                            LogManager.error(ex);
                        }
                    });
                    fpg.deslizaVentana();
                } else {
                    LogManager.info("Aún no inicia la facturación global");
                    BitacoraDAO.evento("Aún no inicia la facturación global");
                }
            } else {
                LogManager.info("La Facturación Automática no está habilitada");
                BitacoraDAO.evento("La Facturación Automática no está habilitada");
            }
        } catch (SQLException ex) {
            LogManager.fatal("Error de base de datos ", ex);
            BitacoraDAO.evento("Error de base de datos " + ex.getMessage() + ". Reintentando en 5 minutos");
            next.cancel(true);
            FacturadorScheduler.getInstance().schedule(this, 5, TimeUnit.MINUTES);
            return;
         } catch (RuntimeException rte) {
            LogManager.fatal("Error fatal de ejecución ", rte);
            BitacoraDAO.evento("Error fatal de ejecución " + rte.getMessage());
        } catch (Throwable t ){
            LogManager.fatal("Error inesperado ", t);
            BitacoraDAO.evento("Error inesperado " + t.getMessage());
        }
    }
}