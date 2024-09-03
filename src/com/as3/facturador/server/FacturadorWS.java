/**
 * FacturadorWS
 * Servicio de Timbrado Omicrom.
 *  Contempla diferentes posibilidades de facturación.
 *  Se preserva la lógica de facturación actual y se añaden métodos de timbrado en bloque.
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.server;

import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.vo.Cancelacion;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.CFormaPago;
import com.softcoatl.sat.cfdi.schema.CUsoCFDI;
import java.util.Date;
import java.util.List;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;

@WebService(name = "OmicromFacturador", targetNamespace = "http://server.detisa.mx.com/")
public interface FacturadorWS {

    @WebMethod
    @WebResult(name="cfdi")
    public List<Response> cfdiGlobalVentana()
    throws FacturacionException;

    @WebMethod
    @WebResult(name="cfdi")
    public List<Response> cfdiGlobalPeriodo(
            @WebParam(name = "inicio")      @XmlElement(nillable = false, required = true)  Date inicio,                    // Inicio del periodo
            @WebParam(name = "fin")         @XmlElement(nillable = false, required = true)  Date fin)                       // Fin del periodo
    throws FacturacionException;

    @WebMethod
    @WebResult(name="cfdi")
    public List<Response> cfdiGlobalFolios(
            @WebParam(name = "folios")      @XmlElement(nillable = false, required = true)  List<Folio> folios)            // Lista de Folios
    throws FacturacionException;

    @WebMethod
    @WebResult(name="cfdi")
    public Response cfdiClientePeriodo(
            @WebParam(name = "inicio")      @XmlElement(nillable = false, required = true)  Date inicio,                    // Inicio del periodo
            @WebParam(name = "fin")         @XmlElement(nillable = false, required = true)  Date fin,                       // Fin del periodo
            @WebParam(name = "cliente")     @XmlElement(nillable = false, required = true)  int cliente,                    // ID del cliente
            @WebParam(name = "forma_pago")  @XmlElement(nillable = false, required = true)  CFormaPago formaPago,           // Forma de Pago
            @WebParam(name = "uso")         @XmlElement(nillable = false, required = true)  CUsoCFDI uso,                   // Uso del CFDI
            @WebParam(name = "nota")        @XmlElement(nillable = false, required = true)  String nota)                    // Observaciones
    throws FacturacionException;

    @WebMethod
    @WebResult(name="cfdi")
    public Response cfdiClienteFolios(
            @WebParam(name = "folios")      @XmlElement(nillable = false, required = true)  List<Folio> folios,            // Lista de Folios
            @WebParam(name = "cliente")     @XmlElement(nillable = false, required = true)  int cliente,                    // ID del cliente
            @WebParam(name = "forma_pago")  @XmlElement(nillable = false, required = true)  CFormaPago formaPago,           // Forma de Pago
            @WebParam(name = "uso")         @XmlElement(nillable = false, required = true)  CUsoCFDI uso,                   // Uso del CFDI
            @WebParam(name = "nota")        @XmlElement(nillable = false, required = true)  String nota)                    // Observaciones
    throws FacturacionException;

    @WebMethod
    public Response cfdiXml(
            @WebParam(name = "cfdi")        @XmlElement(nillable = false, required = true)  String cfdi,                    // XML Data
            @WebParam(name = "formato")     @XmlElement(nillable = false, required = true)  Format formato,                 // PDF file format A1/Ticket
            @WebParam(name = "tipo")        @XmlElement(nillable = false, required = true)  DocType tipo,                   // Document type FA (Factura)/CR (Nota de credito)/RP (Recepción de Pagos)
            @WebParam(name = "idfc")        @XmlElement(nillable = false, required = true)  int idfc)                       // Folio
    throws FacturacionException, CFDIException;

    @WebMethod
    @WebResult(name = "comprobante")
    public String cfdiPOSUltimoConsumo(
            @WebParam(name = "posicion")    @XmlElement(nillable = false, required = true)  String posicion,                // Posición de carga
            @WebParam(name = "cliente")     @XmlElement(nillable = false, required = true)  int cliente,                    // ID del Cliente
            @WebParam(name = "formapago")   @XmlElement(nillable = false, required = true)  String formapago,               // Forma de pago
            @WebParam(name = "usoCFDI")     @XmlElement(nillable = false, required = true)  String usoCFDI,                 // Forma de pago
            @WebParam(name = "bankid")      @XmlElement(nillable = false, required = true)  int bankid,                     // Identificador del Banco
            @WebParam(name = "password")    @XmlElement(nillable = false, required = false)  String password)               // Password del operador
    throws FacturacionException;

    @WebMethod
    @WebResult(name = "comprobante")
    public String cfdiPOSConsumo(
            @WebParam(name = "transaccion") @XmlElement(nillable = false, required = true)  String transaccion,             // Posición de carga
            @WebParam(name = "cliente")     @XmlElement(nillable = false, required = true)  int cliente,                    // ID del Cliente
            @WebParam(name = "formapago")   @XmlElement(nillable = false, required = true)  String formapago,               // Forma de pago
            @WebParam(name = "usoCFDI")     @XmlElement(nillable = false, required = true)  String usoCFDI,                 // Forma de pago
            @WebParam(name = "bankid")      @XmlElement(nillable = false, required = true)  int bankid,                     // Identificador del Banco
            @WebParam(name = "password")    @XmlElement(nillable = false, required = false)  String password)                // Password del operador
    throws FacturacionException;

    @WebMethod
    @WebResult(name = "comprobante")
    public String cfdiPOSUltimoAditivo(
            @WebParam(name = "posicion")    @XmlElement(nillable = false, required = true)  String posicion,                // Posición de carga
            @WebParam(name = "cliente")     @XmlElement(nillable = false, required = true)  int cliente,                    // ID del Cliente
            @WebParam(name = "formapago")   @XmlElement(nillable = false, required = true)  String formapago,               // Forma de pago
            @WebParam(name = "usoCFDI")     @XmlElement(nillable = false, required = true)  String usoCFDI,                 // Forma de pago
            @WebParam(name = "bankid")      @XmlElement(nillable = false, required = true)  int bankid,                     // Identificador del Banco
            @WebParam(name = "password")    @XmlElement(nillable = false, required = false)  String password)               // Password del operador
    throws FacturacionException;

    @WebMethod
    @WebResult(name = "comprobante")
    public String cfdiPOSAditivo(
            @WebParam(name = "transaccion") @XmlElement(nillable = false, required = true)  String transaccion,             // Posición de carga
            @WebParam(name = "cliente")     @XmlElement(nillable = false, required = true)  int cliente,                    // ID del Cliente
            @WebParam(name = "formapago")   @XmlElement(nillable = false, required = true)  String formapago,               // Forma de pago
            @WebParam(name = "usoCFDI")     @XmlElement(nillable = false, required = true)  String usoCFDI,                 // Forma de pago
            @WebParam(name = "password")    @XmlElement(nillable = false, required = false)  String password)                // Password del operador
    throws FacturacionException;

    @WebMethod
    public List<Cancelacion> cancelacion(
            @WebParam(name = "uuid") List<String> uuid)                                                                     // UUIDs de los folios cancelados
    throws FacturacionException;

    @WebMethod
    public String getAcuseCancelacion(
            @WebParam(name = "uuid") String uuid) throws FacturacionException;                                                         // UUID del folio cancelado

    @WebMethod
    public byte[] generaPDFFile(
            @WebParam(name = "uuid") String uuid,                                                                     // CFDI
            @WebParam(name = "formato") Format formato,                                                               // PDF file format A1/Ticket
            @WebParam(name = "send") boolean send,
            @WebParam(name = "correo") List<String> correo
    ) throws FacturacionException, CFDIException;

    @WebMethod
    public byte[] generaDescarga(
            @WebParam(name = "cliente") int cliente,                                                               // id del cliente
            @WebParam(name = "inicio") @XmlElement(nillable = false, required = true)  Date inicio,                // Inicio del periodo
            @WebParam(name = "fin") @XmlElement(nillable = false, required = true)  Date fin                       // Fin del periodo
    ) throws FacturacionException, CFDIException;
}//FacturadorWS
