/* 
 * FacturadorWSImpl
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.server;

import com.as3.facturador.context.FacturadorOmicromContext;
import com.as3.facturador.dao.BitacoraDAO;
import com.as3.facturador.dao.ClienteDAO;
import com.as3.facturador.dao.FacturaDAO;
import com.as3.facturador.dao.VariablesDAO;
import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.process.CancelacionProcessFactory;
import com.as3.facturador.process.FacturaProcessor;
import com.as3.facturador.process.PDFProcessFactory;
import com.as3.facturador.process.POSProcessFactory;
import com.as3.facturador.process.TimbradoProcessFactory;
import com.as3.facturador.vo.Cancelacion;
import com.softcoatl.sat.cfdi.CFDIXmlResolver;
import com.softcoatl.sat.cfdi.schema.CFormaPago;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.utils.StringUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.as3.facturador.schema.ComprobanteDetisaImp;
import com.as3.facturador.global.FacturaPublicoGeneral;
import com.as3.facturador.global.FacturaPublicoGeneralFactory;
import com.as3.facturador.process.POSPeripheralProcessFactory;
import com.as3.facturador.process.POSTrProcessFactory;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.CUsoCFDI;
import com.softcoatl.utils.logging.LogManager;
import java.sql.SQLException;
import javax.jws.WebService;

@WebService(
        serviceName = "OmicromFacturador",
        portName = "OmicromFacturadorPort",
        targetNamespace = "http://server.detisa.mx.com/",
        endpointInterface = "com.as3.facturador.server.FacturadorWS")
public class FacturadorWSImpl implements FacturadorWS {

    @Override
    public List<Response> cfdiGlobalVentana() throws FacturacionException {
        FacturaProcessor processor = new TimbradoProcessFactory().create();
        List<Response> responses = new ArrayList<>();
        FacturaPublicoGeneral fpg = FacturaPublicoGeneralFactory.instanciate();
        FacturaDAO dao = new FacturaDAO();
        if (!fpg.createComprobantes()) {
            LogManager.info("Ya se ejecutó el proceso correspondiente");
            BitacoraDAO.evento("Ya se ejecutó el proceso correspondiente");
        }
        fpg.comprobantes().forEach((Integer id) -> {
            try {
                ComprobanteDetisa comprobante = dao.getComprobantePublicoEnGeneral(id);
                if (comprobante.getCfdi().hasConceptos()) {
                    comprobante.setFormat(Format.A1);
                    comprobante.setDocType(DocType.FG);
                    responses.add(processor.process(comprobante).getResponse());
                }
            } catch (SQLException | CFDIException ex) {
                LogManager.error(ex);
            }
        });
        return responses;
    }

    @Override
    public List<Response> cfdiGlobalPeriodo(Date inicio, Date fin) throws FacturacionException {
        FacturaProcessor processor = new TimbradoProcessFactory().create();
        List<Response> responses = new ArrayList<>();
        new FacturaDAO().cfdiGlobalPeriodo(inicio, fin).forEach((ComprobanteDetisa comprobante) ->
            responses.add(processor.process(comprobante).getResponse()));
        return responses;
    }

    @Override
    public List<Response> cfdiGlobalFolios(List<Folio> folios) throws FacturacionException {
        FacturaProcessor processor = new TimbradoProcessFactory().create();
        List<Response> responses = new ArrayList<>();
        new FacturaDAO().cfdiGlobalFolios(folios).forEach((ComprobanteDetisa comprobante) ->
            responses.add(processor.process(comprobante).getResponse()));
        return responses;
    }

    @Override
    public Response cfdiClientePeriodo(Date inicio, Date fin, int cliente, CFormaPago formaPago, CUsoCFDI usoCFDI, String nota) throws FacturacionException {
        FacturaProcessor processor = new TimbradoProcessFactory().create();
        ComprobanteDetisa comprobante = new FacturaDAO().cfdiClientePeriodo(inicio, fin, cliente, formaPago, usoCFDI, nota);
        return processor.process(comprobante).getResponse();
    }

    @Override
    public Response cfdiClienteFolios(List<Folio> folios, int cliente, CFormaPago formaPago, CUsoCFDI usoCFDI, String nota) throws FacturacionException {
        FacturaProcessor processor = new TimbradoProcessFactory().create();
        ComprobanteDetisa comprobante = new FacturaDAO().cfdiClienteFolios(folios, cliente, formaPago, usoCFDI, nota);
        return processor.process(comprobante).getResponse();
    }

    @Override
    public Response cfdiXml(String xml, Format formato, DocType tipo, int idfc) throws FacturacionException, CFDIException {
        FacturaProcessor processor = new TimbradoProcessFactory().create();
        CFDI cfdi = new CFDIXmlResolver().resolve(xml, FacturadorOmicromContext.getInstance().getCfdiMapper());
        LogManager.debug("Resolved Object " + cfdi.getComprobante());
        ComprobanteDetisa comprobante = new ComprobanteDetisaImp(idfc, formato, tipo, cfdi);
        if ("XAXX010101000".equals(cfdi.getComprobante().getReceptor().getRfc()) && !"PUBLICO EN GENERAL".equals(cfdi.getComprobante().getReceptor().getNombre())) {
            comprobante.setCliente(ClienteDAO.getOwner(idfc, tipo));
        } else {
            comprobante.setCliente(ClienteDAO.getOwner(idfc, cfdi.getComprobante().getReceptor().getRegimenFiscalReceptor(), tipo));
        }
        return processor.process(comprobante).getResponse();
    }

    @Override
    public String cfdiPOSUltimoConsumo(String posicion, int cliente, String formapago, String usoCFDI, int bankid, String password) throws FacturacionException {
        FacturaProcessor processor = new POSProcessFactory().create();
        ComprobanteDetisaImp comprobante = new ComprobanteDetisaImp(
                ClienteDAO.get(cliente, formapago, usoCFDI), 
                VariablesDAO.getDespachadorByNIP(password, Integer.parseInt(posicion)),
                DocType.FA,
                Format.TX);
        if (bankid>0) {
            comprobante.setBanco(ClienteDAO.get(bankid, formapago, usoCFDI));
        }
        ComprobanteDetisa result = processor.process(comprobante);
        if (!result.isValid() && StringUtils.isNVL(result.getUuid())) {
            FacturaDAO.errorTimbradoPOS(result.getId());
            throw new FacturacionException(result.getError());
        }
        return result.getTxt();
    }

    @Override
    public String cfdiPOSConsumo(String transaccion, int cliente, String formapago, String usoCFDI, int bankid, String password) throws FacturacionException {
        FacturaProcessor processor = new POSTrProcessFactory().create();
        ComprobanteDetisaImp comprobante = new ComprobanteDetisaImp(
                ClienteDAO.get(cliente, formapago, usoCFDI), 
                VariablesDAO.getDespachadorByNIP(password, FacturaDAO.getPosicionConsumo(Integer.parseInt(transaccion))),
                DocType.FA,
                Format.TX);
        comprobante.setRmId(Integer.parseInt(transaccion));
        if (bankid>0) {
            comprobante.setBanco(ClienteDAO.get(bankid, formapago, usoCFDI));
        }
        ComprobanteDetisa result = processor.process(comprobante);
        if (!result.isValid() && StringUtils.isNVL(result.getUuid())) {
            FacturaDAO.errorTimbradoPOS(result.getId());
            throw new FacturacionException(result.getError());
        }
        return result.getTxt();
    }

    @Override
    public String cfdiPOSUltimoAditivo(String posicion, int cliente, String formapago, String usoCFDI, int bankid, String password) throws FacturacionException {
        FacturaProcessor processor = new POSPeripheralProcessFactory().create();
        LogManager.info(posicion);
        LogManager.info(cliente);
        LogManager.info(formapago);
        ComprobanteDetisaImp comprobante = new ComprobanteDetisaImp(
                ClienteDAO.get(cliente, formapago, usoCFDI), 
                VariablesDAO.getDespachadorByNIP(password, Integer.parseInt(posicion)),
                DocType.FA,
                Format.TX);
        if (bankid>0) {
            comprobante.setBanco(ClienteDAO.get(bankid, formapago, usoCFDI));
        }
        ComprobanteDetisa result = processor.process(comprobante);
        if (!result.isValid() && StringUtils.isNVL(result.getUuid())) {
            FacturaDAO.errorTimbradoPOS(result.getId());
            throw new FacturacionException(result.getError());
        }
        return result.getTxt();
    }

    @Override
    public String cfdiPOSAditivo(String transaccion, int cliente, String formapago, String usoCFDI, String password) throws FacturacionException {
        throw new FacturacionException("Not supported yet.");
    }

    @Override
    public List<Cancelacion> cancelacion(List<String> uuid) throws FacturacionException {
        FacturaProcessor processor = new CancelacionProcessFactory().create();
        List<Cancelacion> acuses = new ArrayList<>();
        uuid.forEach((String folio) -> {
            try {
                String[] array = folio.split("[|]");
                ComprobanteDetisa comprobante = processor.process(FacturaDAO.loadCFDI(array[1], array[2], array.length>3 ? array[3] : ""));
                acuses.add(comprobante.isValid() ? new Cancelacion(comprobante.getXml()) : new Cancelacion(comprobante.getError()));
            } catch (SQLException ex) {
                LogManager.error(ex);
            }
        });
        return acuses;
    }

    @Override
    public String getAcuseCancelacion(String uuid) throws FacturacionException {
        throw new FacturacionException("Not supported yet.");
    }

    @Override
    public byte[] generaPDFFile(String uuid, Format formato, boolean send, List<String> correo) throws FacturacionException, CFDIException {
        FacturaProcessor processor = new PDFProcessFactory().create();
        ComprobanteDetisa comprobante = FacturaDAO.loadCFDI(uuid, formato);
        comprobante.setMailer(send);
        comprobante.setMailerList(correo);
        return processor.process(comprobante).getPdf();
    }

    @Override
    public byte[] generaDescarga(int cliente, Date inicio, Date fin) throws FacturacionException, CFDIException {
        FacturaProcessor processor = new PDFProcessFactory().create();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new JarOutputStream(baos)) {
            (cliente > 0 ? FacturaDAO.loadCFDI(cliente, inicio, fin) : FacturaDAO.loadCFDI(inicio, fin)).forEach(comprobante -> {
                try {
                    comprobante.setMailer(false);
                    ComprobanteDetisa processed = (ComprobanteDetisa) processor.process(comprobante);
                    zos.putNextEntry(new ZipEntry(processed.getUuid() + ".xml"));
                    zos.write(processed.getXml());
                    zos.closeEntry();
                    zos.putNextEntry(new ZipEntry(processed.getUuid() + ".pdf"));
                    zos.write(processed.getPdf());
                    zos.closeEntry();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            });
        } catch (IOException ex) {
            throw new FacturacionException(ex);
        }
        return baos.toByteArray();
    }
}