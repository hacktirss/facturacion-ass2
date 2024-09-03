/*
 * ComprobanteDetisa
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.schema;

import com.as3.facturador.server.DocType;
import com.as3.facturador.server.Format;
import com.as3.facturador.server.Response;
import com.as3.facturador.vo.ClienteVO;
import com.as3.facturador.vo.DespachadorVO;
import com.softcoatl.sat.pac.Cancelation;
import com.softcoatl.sat.cfdi.schema.CFDI;
import java.math.BigDecimal;
import java.util.List;

public interface ComprobanteDetisa {

    public int getId();
    public int getRmId();
    public ClienteVO getCliente();
    public ClienteVO getBanco();
    public DespachadorVO getDespachador();
    public boolean isValid();
    public String getError();
    public byte[] getPdf();
    public byte[] getXml();
    public String getTxt();
    public String getVersion();
    public String getUuid();
    public String getFolio();
    public boolean isMailer();
    public boolean isPdfGenerator();
    public List<String> getMailerList();
    public Format getFormat();
    public DocType getDocType();
    public void setId(int id);
    public void setCliente(ClienteVO cliente);
    public void setBanco(ClienteVO cliente);
    public void setDespachador(DespachadorVO despachador);
    public void setCancelation(Cancelation cancelation);
    public void setValid(boolean valid);
    public void setMailer(boolean mailer);
    public void setPdfGenerator(boolean pdfGenerator);
    public void setMailerList(List<String> list);
    public void setError(String error);
    public void setPdf(byte[] pdf);
    public void setXml(byte[] xml);
    public void setTxt(String txt);
    public void setUuid(String uuid);
    public void setFormat(Format format);
    public void setDocType(DocType docType);
    public Cancelation getCancelation();
    public Response getResponse();
    public CFDI getCfdi();
    public void setCfdi(CFDI cfdi);
    public BigDecimal getImporteTotal();
}
