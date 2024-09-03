/*
 * Comprobante
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.schema;

import com.as3.facturador.commons.ComprobantePrintableFactory;
import com.as3.facturador.server.DocType;
import com.as3.facturador.server.Format;
import com.as3.facturador.server.Response;
import com.as3.facturador.vo.ClienteVO;
import com.as3.facturador.vo.DespachadorVO;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.pac.Cancelation;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.sat.cfdi.schema.complemento.Pagos;
import com.softcoatl.sat.cfdi.schema.complemento.pagos.Pagos20;
import com.softcoatl.utils.logging.LogManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString(callSuper=true, includeFieldNames=true)
public class ComprobanteDetisaImp implements com.as3.facturador.schema.ComprobanteDetisa {

    @Getter @Setter private int id;
    @Getter @Setter private int rmId;
    @Getter @Setter private ClienteVO cliente = ClienteVO.builder().build();
    @Getter @Setter private ClienteVO banco;
    @Getter @Setter private DespachadorVO despachador = DespachadorVO.builder().build();
    @Getter @Setter private boolean valid = true;
    @Getter @Setter private boolean mailer = true;
    @Getter @Setter private boolean pdfGenerator = true;
    @Getter @Setter private String error;
    @Getter @Setter @ToString.Exclude private byte[] pdf;
    @Getter @Setter @ToString.Exclude private byte[] xml;
    @Getter @Setter private String txt;
    @Getter @Setter private String uuid;
    @Getter @Setter private String version;
    @Getter @Setter private List<String> mailerList;
    @Getter @Setter private Format format;
    @Getter @Setter private DocType docType;
    @Getter         private CFDI cfdi;
    @Getter @Setter private Cancelation cancelation;

    public ComprobanteDetisaImp() {
    }

    public ComprobanteDetisaImp(int id) {
        this.id = id;
    }

    public ComprobanteDetisaImp(String uuid) {
        this.uuid = uuid;
    }

    public ComprobanteDetisaImp(int id, Format format, DocType docType) {
        this.id = id;
        this.format = format;
        this.docType = docType;
    }

    public ComprobanteDetisaImp(ClienteVO cliente, DespachadorVO despachador, DocType docType, Format format) {
        this.cliente = cliente;
        this.despachador = despachador;
        this.docType = docType;
        this.format = format;
    }

    public ComprobanteDetisaImp(int id, Format format, DocType docType, CFDI cfdi) {
        this.id = id;
        this.format = format;
        this.docType = docType;
        this.cfdi = cfdi;
        this.version = cfdi.getComprobante().getVersion();
        try {
            this.xml = cfdi.toXML().getBytes(StandardCharsets.UTF_8);
        } catch (CFDIException ex) {
            LogManager.error("Error conviertiendo XML");
        }
    }

    public void setCfdi(CFDI cfdi) {
        this.cfdi = cfdi;
        this.version = cfdi.getComprobante().getVersion();
    }

    @Override
    public Response getResponse() {
        Response response = new Response();
        response.setValid(valid);
        response.setXml(format.genXML() ? xml : null);
        response.setPdf(format.genPDF() ? pdf : null);
        response.setUuid(uuid);
        response.setError(error);
        return response;
    }

    @Override
    public String getFolio() {
        try {
            return new ComprobantePrintableFactory().instanciate(cfdi).getFolioDocumento();
        } catch (CFDIException ex) {
            LogManager.error(ex);
        }
        return "";
    }

    @Override
    public BigDecimal getImporteTotal() {
        Pagos pagos;
        try {
            if (cfdi.hasComplementoOfType(Pagos20.NAMESPACE, Pagos20.class)) {
                pagos = cfdi.getComplementoOfType(Pagos20.NAMESPACE, Pagos20.class);
            } else {
                throw new CFDIException("No se encontró el complemento de Pagos");
            }
            return pagos.getTotalPagos().setScale(2, RoundingMode.HALF_EVEN);
        } catch (CFDIException ex) {
            LogManager.error(ex);
        }
        return cfdi.getComprobante().getTotal();
    }
}
