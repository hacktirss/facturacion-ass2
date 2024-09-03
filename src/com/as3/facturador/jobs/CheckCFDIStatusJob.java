/* 
 * CheckCFDIStatusJob
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since dec 2019 
 */
package com.as3.facturador.jobs;

import com.as3.facturador.dao.CancelationDAO;
import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.process.CancelacionProcessFactory;
import com.as3.facturador.process.FacturaProcessor;
import com.softcoatl.sat.cfdi.validator.CfdiStatusWrapper;
import com.softcoatl.sat.cfdi.validator.CfdiValidator;
import com.softcoatl.utils.logging.LogManager;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.as3.facturador.schema.ComprobanteDetisaImp;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.sat.cfdi.validator.CfdiValidatorException;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CheckCFDIStatusJob implements Runnable {

    private final FacturaProcessor processor = new CancelacionProcessFactory().create();

    private CfdiStatusWrapper getStatus(CFDI cfdi) {
        try {
            LogManager.info("Consultando estatus del folio " + cfdi.getTimbre().getUUID());
            LogManager.debug(cfdi.getExpresionImpresa());
            CfdiStatusWrapper status = CfdiValidator.getStatus(cfdi.getTimbre().getUUID(), cfdi.getExpresionImpresa());
            if (status.existe()) {
                return status;
            }
        } catch (CfdiValidatorException ex) {
            LogManager.error(ex);
        }

        LogManager.info(String.format("EL folio %s no existe en los registros del SAT", cfdi.getTimbre().getUUID()));
        return null;
    }

    private ComprobanteDetisa enCancelacion(String uuid) throws SQLException {
        
        ComprobanteDetisa enCancelacion = new ComprobanteDetisaImp(uuid);
        enCancelacion.setCancelation(CancelationDAO.getCancelation(uuid));
        return enCancelacion;
    }

    private void confirmaCancelacion(CfdiStatusWrapper status) {
        try {
            LogManager.info(String.format("Se validó la cancelación del folio %s", status.getUuid()));
            ComprobanteDetisa comprobante = processor.process(enCancelacion(status.getUuid()));
            LogManager.warn(comprobante.isValid() ? "Factura Cancelada" : "Error Cancelando " + comprobante.getError());

            FacturaDAO.updateCancelacion(status.getUuid(), "3");
            FacturaDAO.liberaConsumos(status.getUuid());
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
    }        

    private void enProceso(CfdiStatusWrapper status) {
        LogManager.info(String.format("No se ha confirmado la cancelación del folio %s", status.getUuid()));
        if (status.requiereAceptacion()) {
            LogManager.info(String.format("EL folio %s requiere aceptación del Receptor", status.getUuid()));
        }

        try {
            FacturaDAO.updateCancelacion(status.getUuid(), "2");
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
    }
    
    private void rollBack(CfdiStatusWrapper status) {
        try {
            LogManager.info(status);
            LogManager.info(String.format("Cancelación rechazada", status.getUuid()));
            FacturaDAO.rollbackCancelacion(status.getUuid());
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
    }

    private void cancela(CfdiStatusWrapper status) {
        LogManager.info(String.format("CFDI %s Vigente %s", status.getUuid(), status.getEstadoCancelacion()));
        try {
            if (status.isCancelable()) {
                if (!status.isEnProceso()) {
                    ComprobanteDetisa comprobante = processor.process(enCancelacion(status.getUuid()));
                    LogManager.warn(comprobante.isValid() ? "Factura Cancelada" : "Error Cancelando " + comprobante.getError());
                } else {
                    LogManager.info(String.format("EL folio %s está en Proceso de Cancelación", status.getUuid()));
                }
            } else {
                LogManager.info(String.format("EL folio %s no es Cancelable", status.getUuid()));
                FacturaDAO.rollbackCancelacion(status.getUuid());
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
    }        

    private boolean checkProcessingCFDI() throws FacturacionException {

        LogManager.info("Procesando facturas pendientes de cancelación");
        List<CfdiStatusWrapper> statusList = FacturaDAO.getProcesoCancelacion().stream().map(this::getStatus).filter(Objects::nonNull).collect(Collectors.toList());
        LogManager.info("Procesando facturas canceladas");
        statusList.stream().filter(CfdiStatusWrapper::isCancelada).forEach(this::confirmaCancelacion);
        LogManager.info("Procesando facturas en proceso");
        statusList.stream().filter(CfdiStatusWrapper::isEnProceso).forEach(this::enProceso);
        LogManager.info("Procesando facturas rechazadas");
        statusList.stream().filter(CfdiStatusWrapper::isVigente).forEach(this::rollBack);

        return true;
    }

    @Override
    public void run() {
        try {
            checkProcessingCFDI();
        } catch (RuntimeException rte) {
            LogManager.fatal("Error fatal de acceso al WSDL ", rte);
        } catch (Throwable t ){
            LogManager.fatal("Error inesperado ", t);
        }
    }
}
