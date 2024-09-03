/* 
 * FacturaDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since abr 2019 
 */
package com.as3.facturador.dao;

import com.as3.facturador.context.FacturadorOmicromContext;
import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.schema.ComprobanteDetisa;
import com.as3.facturador.schema.ComprobanteDetisaImp;
import com.as3.facturador.schema.bridge.ObjectFactory;
import com.as3.facturador.server.DocType;
import com.as3.facturador.server.Folio;
import com.as3.facturador.server.Format;
import com.ass2.sat.cfdi.schema.addenda.Nota;
import com.softcoatl.database.AutoTransaction;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.sat.cfdi.CFDIException;
import com.softcoatl.sat.cfdi.schema.CFDI;
import com.softcoatl.sat.cfdi.schema.CFormaPago;
import com.softcoatl.sat.cfdi.schema.CUsoCFDI;
import com.softcoatl.sat.cfdi.schema.Comprobante;
import com.softcoatl.sat.cfdi.CFDIXmlResolver;
import com.softcoatl.utils.logging.LogManager;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class FacturaDAO {

    private static final String SQL_CONCEPTOS_POS = "com/detisa/facturador/sql/CreaConceptosPosicion.sql";
    private static final String SQL_CONCEPTOS_POS_TR = "com/detisa/facturador/sql/CreaConceptosTransaccion.sql";

    private static final String SQL_CONCEPTOS_VTA_POS = "com/detisa/facturador/sql/CreaConceptosPeripheralPosicion.sql";

    private static final String SQL_FC_TOTALIZA = "com/detisa/facturador/sql/TotalizaFactura.sql";

    private static final String SQL_FCPG_CONCEPTOS = "com/detisa/facturador/sql/ConceptosFacturaPG.sql";
    private static final String SQL_FC_CONCEPTOS = "com/detisa/facturador/sql/ConceptosFactura.sql";
    private static final String SQL_FCPG_IMPUESTOS = "com/detisa/facturador/sql/ImpuestosFacturaPG.sql";
    private static final String SQL_FC_IMPUESTOS = "com/detisa/facturador/sql/ImpuestosFactura.sql";

    private static final String SQL_GLOBAL_RESERVE_RM = "com/detisa/facturador/sql/ReserveRMFacturacionGlobal.sql";
    private static final String SQL_GLOBAL_RESERVE_RMG = "com/detisa/facturador/sql/ReserveRMFacturacionGlobalGlobal.sql";
    private static final String SQL_GLOBAL_RESERVE_VA = "com/detisa/facturador/sql/ReserveVTAFacturacionGlobal.sql";
    private static final String SQL_GLOBAL_RESERVE_VAG = "com/detisa/facturador/sql/ReserveVTAFacturacionGlobalGlobal.sql";
    private static final String SQL_GLOBAL_FC = "com/detisa/facturador/sql/CreaFacturaPublicoEnGeneral.sql";
    private static final String SQL_GLOBAL_FCD = "com/detisa/facturador/sql/CreaConceptosPublicoEnGeneral.sql";
    private static final String SQL_GLOBAL_FCG = "com/detisa/facturador/sql/CreaFacturaPublicoEnGeneralGlobal.sql";
    private static final String SQL_GLOBAL_FCDG = "com/detisa/facturador/sql/CreaConceptosPublicoEnGeneralGlobal.sql";
    private static final String SQL_GLOBAL_TOTALIZA = "com/detisa/facturador/sql/TotalizaFacturaPublicoEnGeneral.sql";

    private static final String SQL_CLIENTE_FP_FC = "com/detisa/facturador/sql/CreaFacturaClienteFP.sql";

    private static final QueryMap QRY = new QueryMap();

    private final ObjectFactory factory;

    public FacturaDAO() throws FacturacionException {
        try {
            factory = ObjectFactory.getInstance();
        } catch (ReflectiveOperationException ex) {
            throw new FacturacionException(ex);
        }
    }

    public static Comprobante.Emisor loadEmisor() {

        String sql = "SELECT rfc, TRIM( REGEXP_REPLACE( UPPER( cia ), '([, ]{1,4})?[S][.]?[A][.]?[ ]{1,3}[DE]{2}[ ]{1,3}([C][.]?[V][.]?|[R][.]?[L][.]?)$', '' ) ) nombre, IFNULL( clave_regimen,  '601' ) regimenFiscal FROM cia";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement stmnt = connection.createStatement();
                ResultSet rs = stmnt.executeQuery(sql)) {
            if (rs.next()) {
                return ObjectFactory.getInstance().createComprobanteEmisor(rs);
            }
        } catch (SQLException | ReflectiveOperationException ex) {
            LogManager.fatal("Error configurando el sistema. No es posible obtener los datos del Emisor.", ex);
        }
        throw new RuntimeException("No es posible configurar el servidor de timbrado. No se recuperaron los datos del EMISOR");        
    }

    private static Comprobante.Receptor loadReceptor(int id, String usoCFDI) throws FacturacionException {

        String sql = "SELECT TRIM( rfc ) rfc, "
                + "TRIM( REGEXP_REPLACE( UPPER( nombre ), '([, ]{1,4})?[S][.]?[A][.]?[ ]{1,3}[DE]{2}[ ]{1,3}([C][.]?[V][.]?|[R][.]?[L][.]?)$', '' ) ) nombre, "
                + "regimenFiscal regimenFiscalReceptor, codigo domicilioFiscalReceptor, "
                + "IF( TRIM( ? ) = '', 'G03', ? ) usoCFDI FROM cli WHERE id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setString(1, usoCFDI);
            ps.setString(2, usoCFDI);
            ps.setInt(3, id);
            LogManager.debug(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ObjectFactory.getInstance().createComprobanteReceptor(rs);
                }
            }
        } catch (SQLException | ReflectiveOperationException ex) {
            throw new FacturacionException(ex);
        }
        throw new FacturacionException("No se recuperaron los datos del RECEPTOR");        
    }

    private static boolean insert(int folio, int tabla, CFDI cfdi, byte[] xml) throws SQLException {
        String sql = "INSERT INTO facturas( id_fc_fk, tabla, fecha_emision, version, fecha_timbrado, cfdi_xml, clave_pac, emisor, receptor, uuid ) "
                        + "SELECT ?, ?, ?, ?, ?, ?, clave_pac, ?, ?, ? "
                        + "FROM proveedor_pac WHERE activo = 1 ON DUPLICATE KEY UPDATE "
                        +       "id_fc_fk = VALUES( id_fc_fk ), "
                        +       "tabla = VALUES( tabla ), "
                        +       "fecha_emision = VALUES( fecha_emision ), "
                        +       "version = VALUES ( version ), "
                        +       "fecha_timbrado = VALUES( fecha_timbrado ), "
                        +       "cfdi_xml = VALUES( cfdi_xml ), "
                        +       "clave_pac = VALUES( clave_pac ), "
                        +       "emisor = VALUES( emisor ), "
                        +       "receptor = VALUES( receptor ), "
                        +       "uuid = VALUES( uuid )";
        LogManager.debug(new String(xml));
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                //PreparedStatement psConfigure = connection.prepareStatement("SET GLOBAL max_allowed_packet=10000000000;");
                PreparedStatement psInsert = connection.prepareStatement(sql)) {
            psInsert.setInt(1, folio);
            psInsert.setInt(2, tabla);
            psInsert.setTimestamp(3, new Timestamp(cfdi.getComprobante().getFecha().toGregorianCalendar().getTimeInMillis()));
            psInsert.setString(4, cfdi.getComprobante().getVersion());
            psInsert.setTimestamp(5, new Timestamp(cfdi.getTimbre().getFechaTimbrado().toGregorianCalendar().getTimeInMillis()));
            psInsert.setString(6, new String(xml, StandardCharsets.UTF_8));
            psInsert.setString(7, cfdi.getComprobante().getEmisor().getRfc());
            psInsert.setString(8, cfdi.getComprobante().getReceptor().getRfc());
            psInsert.setString(9, cfdi.getTimbre().getUUID());
            //psConfigure.execute();
            return psInsert.executeUpdate()>0;
        }
    }

    public static boolean save(int folio, int tabla, CFDI cfdi, byte[] xml) throws SQLException {

        return insert(folio, tabla, cfdi, xml);
    }

    public static boolean saveAcuse(String uuid, String acuse) throws SQLException {
        String sql = "UPDATE facturas f "
                + "LEFT JOIN fc ON fc.uuid = f.uuid "
                + "LEFT JOIN nc ON nc.uuid = f.uuid "
                + "LEFT JOIN pagos ON pagos.uuid = f.uuid "
                + "LEFT JOIN traslados ON traslados.uuid = f.uuid "
                + "SET "
                +       "fc.status = 2, fc.stCancelacion = 1, "
                +       "nc.status = 2, nc.stCancelacion = 1, "
                +       "pagos.statusCFDI = 2, pagos.stCancelacion = 1, "
                +       "traslados.status = 2, traslados.stCancelacion = 1, "
                +       "acuse_cancelacion = ?, fecha_cancelacion = NOW() "
                + "WHERE f.uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, acuse);
            ps.setString(2, uuid);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        }
    }

    public static boolean rollbackCancelacion(String uuid) throws SQLException {
        String sql = "UPDATE facturas f "
                + "LEFT JOIN fc ON fc.uuid = f.uuid "
                + "LEFT JOIN nc ON nc.uuid = f.uuid "
                + "LEFT JOIN pagos ON pagos.uuid = f.uuid "
                + "LEFT JOIN traslados ON traslados.uuid = f.uuid "
                + "SET "
                +       "fc.status = 1, fc.stCancelacion = 0, "
                +       "nc.status = 1, nc.stCancelacion = 0, "
                +       "pagos.statusCFDI = 1, pagos.stCancelacion = 0, "
                +       "traslados.status = 1, traslados.stCancelacion = 0 "
                + "WHERE f.uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        }
    }

    public static boolean updateCancelacion(String uuid, String status) throws SQLException {
        String sql = "UPDATE facturas f "
                + "LEFT JOIN fc ON fc.uuid = f.uuid "
                + "LEFT JOIN nc ON nc.uuid = f.uuid "
                + "LEFT JOIN pagos ON pagos.uuid = f.uuid "
                + "LEFT JOIN traslados ON traslados.uuid = f.uuid "
                + "SET "
                +       "fc.status = 2, fc.stCancelacion = ?, "
                +       "nc.status = 2, nc.stCancelacion = ?, "
                +       "pagos.statusCFDI = 2, pagos.stCancelacion = ?, "
                +       "traslados.status = 2, traslados.stCancelacion = ? "
                + "WHERE f.uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, status);
            ps.setString(3, status);
            ps.setString(4, status);
            ps.setString(5, uuid);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        }
    }

    public static boolean liberaConsumos(String uuid) throws SQLException {
        String sql = "UPDATE facturas f "
                + "JOIN fc USING( uuid ) "
                + "JOIN fcd USING( id ) "
                + "JOIN inv ON fcd.producto = inv.id "
                + "LEFT JOIN rm ON inv.rubro = 'Combustible' AND fcd.ticket = rm.id "
                + "LEFT JOIN vtaditivos vta ON inv.rubro = 'Aceites' AND fcd.ticket = vta.id "
                + "SET "
                +       "rm.uuid = '-----', vta.uuid = '-----', fcd.ticket = -fcd.ticket, "
                +       "fc.cantidad = 0, fc.importe = 0, fc.iva = 0, fc.ieps = 0, fc.total = 0 "
                + "WHERE f.uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        }
    }

    public static boolean errorTimbradoPOS(int id) {
        String sql = "UPDATE fc "
                + "JOIN fcd USING( id ) "
                + "JOIN inv ON fcd.producto = inv.id "
                + "LEFT JOIN rm ON inv.rubro = 'Combustible' AND fcd.ticket = rm.id "
                + "LEFT JOIN vtaditivos vta ON inv.rubro = 'Aceites' AND fcd.ticket = vta.id "
                + "SET "
                +       "rm.uuid = '-----', vta.uuid = '-----', fcd.ticket = -fcd.ticket, "
                +       "fc.cantidad = 0, fc.importe = 0, fc.iva = 0, fc.ieps = 0, fc.total = 0, "
                +       "fc.status = 3, fc.stCancelacion = 3 "
                + "WHERE fc.id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            LogManager.debug(ps.toString());
            return ps.executeUpdate()>0;
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return false;
    }

    public static boolean liberaFoliosVencidos() throws SQLException {
        String sql = "UPDATE fc "
                + "LEFT JOIN fcd USING( id ) "
                + "SET "
                +       "fcd.ticket = -fcd.ticket, fc.status = 3, "
                +       "fc.cantidad = 0, fc.importe = 0, fc.iva = 0, fc.ieps = 0, fc.total = 0 "
                + "WHERE fc.status = 0 AND TIMESTAMPDIFF( HOUR, fc.fecha, NOW() ) > 72";
        LogManager.info("Liberando folios vencidos");
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                Statement st = connection.createStatement()) {
            return st.executeUpdate(sql)>0;
        }
    }

    private static List<String> getUUIDEnCancelacion() {
        List<String> uuids = new ArrayList<>();
        String sql = "SELECT fc.uuid FROM fc  " +
                     "WHERE fc.status = 2 AND fc.stCancelacion < 3 AND DATE(fc.fecha) >= DATE('2017-01-01') AND TRIM( fc.uuid ) != '' AND fc.uuid != '-----' " +
                     "UNION ALL " +
                     "SELECT nc.uuid FROM nc " +
                     "WHERE nc.status = 2 AND nc.stCancelacion < 3 AND DATE(nc.fecha) >= DATE('2017-01-01') AND TRIM( nc.uuid ) != '' AND nc.uuid != '-----' " +
                     "UNION ALL " +
                     "SELECT p.uuid FROM pagos p " +
                     "WHERE p.statusCFDI = 2 AND p.stCancelacion < 3 AND DATE(p.fecha) >= DATE('2017-01-01') AND TRIM( p.uuid ) != '' AND p.uuid != '-----' " +
                     "UNION ALL " +
                     "SELECT t.uuid FROM traslados t " +
                     "WHERE t.status = 2 AND t.stCancelacion < 3 AND DATE(t.fecha) >= DATE('2017-01-01')  AND TRIM( t.uuid ) != '' AND t.uuid != '-----'";
        LogManager.debug(sql);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    uuids.add(rs.getString("uuid"));
                }
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return uuids;
    }

    public static int getPosicionConsumo(int id) {
        String sql = "SELECT posicion FROM rm WHERE id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("posicion");
                }
            }
        } catch (SQLException ex) {
            LogManager.error("Error", ex);
        }
        return -1;
    }

    private static CFDI getCFDIByUUID(String uuid) {
        String sql = "SELECT cfdi_xml FROM facturas WHERE uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new CFDIXmlResolver().resolve(rs.getString("cfdi_xml"), FacturadorOmicromContext.getInstance().getCfdiMapper());
                }
            }
        } catch (SQLException | CFDIException ex) {
            LogManager.error("Error", ex);
        }
        return null;
    }

    public static List<CFDI> getProcesoCancelacion() {
        return getUUIDEnCancelacion().stream().map(FacturaDAO::getCFDIByUUID).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static String expresionImpresa(String uuid) throws SQLException {
        String sql = "SELECT "
                + "CONCAT("
                +       "'?re=', REPLACE(emisor,  '&',  '%26' ), "
                +       "'&rr=', REPLACE(receptor,  '&',  '%26' ), "
                +       "'&tt=', IF(ExtractValue(cfdi_xml, '/cfdi:Comprobante/@Total') <> '', "
                +               "ExtractValue(cfdi_xml, '/cfdi:Comprobante/@Total'), "
                +               "ExtractValue(cfdi_xml, '/cfdi:Comprobante/@total')), '&id=', uuid) ExpresionImpresa "
                + "FROM facturas "
                + "WHERE uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    LogManager.warn("Expresión Impresa" + rs.getString("ExpresionImpresa"));
                    return rs.getString("ExpresionImpresa");
                }
            }
        }
        throw new SQLException("No se encontró el CFDI con UUID " + uuid);
    }


    public List<ComprobanteDetisa> cfdiGlobalPeriodo(Date inicio, Date fin) throws FacturacionException {
        throw new UnsupportedOperationException();
    }

    public List<ComprobanteDetisa> cfdiGlobalFolios(List<Folio> folios) throws FacturacionException {
        throw new UnsupportedOperationException();
    }


    public ComprobanteDetisa cfdiClientePeriodo(Date inicio, Date fin, int idCliente, CFormaPago formaPago, CUsoCFDI usoCFDI, String observaciones) throws FacturacionException {
        throw new UnsupportedOperationException();
    }

    public ComprobanteDetisa cfdiClienteFolios(List<Folio> folios, int idCliente, CFormaPago formaPago, CUsoCFDI usoCFDI, String observaciones) throws FacturacionException {
        throw new UnsupportedOperationException();
    }

    public static List<Integer> loadComprobantePublicoEnGeneral() throws FacturacionException {
        List<Integer> comprobantes = new ArrayList<>();
        String sql = "SELECT fc.id id FROM fc WHERE fc.status = 0 AND fc.total > 0 AND fc.uuid LIKE 'FPG-%'";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comprobantes.add(rs.getInt("id"));
                }
            }        
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        return comprobantes;
    }

    private static void addObservaciones(String comentario, Comprobante comprobante) {
        try {
            Comprobante.Addenda addenda = ObjectFactory.getInstance().createComprobanteAddenda();
            Nota nota = new Nota();
            nota.getTexto().add(comentario);
            JAXBContext jc = JAXBContext.newInstance(Nota.class);
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Marshaller marshaller = jc.createMarshaller();
            marshaller.marshal(nota, document);
            document.getDocumentElement().setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            LogManager.debug("Adding Observaciones " + document.getDocumentElement());
            addenda.getAny().add(document.getDocumentElement());
            comprobante.setAddenda(addenda);
        } catch (ReflectiveOperationException | JAXBException | ParserConfigurationException ex) {
            LogManager.error(ex);
        }
    }

    private static ComprobanteDetisa loadComprobantePublicoEnGeneral(int id) throws SQLException {
        ComprobanteDetisa comprobante;
        String sql = 
                "SELECT " +
                        "fc.id id, fc.folio Folio, fc.serie Serie, fc.periodo Periodicidad, fc.meses Meses, fc.ano Anio, " +
                        "fc.fecha Fecha, cia.version_cfdi Version," +
                        "fc.formadepago FormaPago, fc.metododepago MetodoPago, IFNULL( fc.observaciones, '' ) Observaciones,  " +
                        "SUM( fcd.importe + IF( IFNULL( var.incorporaieps, 'N' ) = 'N', 0.00, fcd.importeieps ) + fcd.diferencia ) Subtotal, " +
                        "SUM( fcd.imported ) Descuento, " +
                        "ROUND( fc.total - fc.descuento , 2 ) Total, " +
                        "cia.codigo LugarExpedicion, cia.zonahoraria timezone " +
                "FROM fc " +
                "JOIN v_fcd fcd USING( id ) " +
                "JOIN cia ON TRUE " +
                "JOIN variables var ON TRUE " +
                "WHERE fc.id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    comprobante = new ComprobanteDetisaImp(rs.getInt("id"));
                    comprobante.setCfdi(ObjectFactory.getInstance().createCFDI(ObjectFactory.getInstance().createComprobante(rs)));
                    Comprobante.InformacionGlobal ig = ObjectFactory.getInstance().createComprobanteInformacionGlobal(rs);
                    comprobante.getCfdi().getComprobante().setInformacionGlobal(ig);
                    if (rs.getString("Observaciones")!=null && !"".equals(rs.getString("Observaciones").trim())) {
                        addObservaciones(rs.getString("Observaciones"), comprobante.getCfdi().getComprobante());
                    }
                    return comprobante;
                }
            } catch (JAXBException | ReflectiveOperationException | CFDIException ex) {
                throw new SQLException(ex);
            }
        }
        return null;
    }

    public ComprobanteDetisa getComprobantePublicoEnGeneral(int id) throws SQLException, CFDIException {
        ComprobanteDetisa comprobante = loadComprobantePublicoEnGeneral(id);
        if (comprobante!=null) {
            comprobante.getCfdi().getComprobante().setEmisor(FacturaDAO.loadEmisor());
            comprobante.getCfdi().getComprobante().setReceptor(factory.createComprobanteReceptorGenerico(comprobante.getCfdi().getComprobante()));
            comprobante.getCfdi().getComprobante().setConceptos(cfdiConceptosFacturaPublicoEnGeneral(comprobante.getId()));
            if (!comprobante.getCfdi().getComprobante().getConceptos().getConcepto().isEmpty()) {
                comprobante.getCfdi().getComprobante().setImpuestos(cfdiImpuestosFacturaPG(comprobante.getId()));
            }
            return comprobante;
        }
        throw new CFDIException("Error cargando Comprobante con id " + id);
    }

    private static Comprobante loadComprobante(int idFc) throws SQLException, ReflectiveOperationException, JAXBException {

        String sql = 
                "SELECT " +
                        "fc.id id, fc.folio Folio, fc.serie Serie, " +
                        "fc.fecha Fecha, cia.version_cfdi Version," +
                        "fc.formadepago FormaPago, fc.metododepago MetodoPago, IFNULL( fc.observaciones, '' ) Observaciones,  " +
                        "SUM( fcd.importe + IF( IFNULL( cli.desgloseIEPS, 'N' ) = 'S', 0.00, fcd.importeieps ) + fcd.diferencia ) Subtotal, " +
                        "SUM( fcd.imported ) Descuento, " +
                        "ROUND( fc.total - fc.descuento , 2 ) Total, " +
                        "cia.codigo LugarExpedicion, cia.zonahoraria timezone " +
                "FROM fc " +
                "JOIN v_fcd fcd USING( id )" +
                "JOIN cia ON TRUE JOIN cli ON fc.cliente = cli.id " +
                "WHERE fc.id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFc);
            LogManager.debug(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return ObjectFactory.getInstance().createComprobante(rs);
                } else {
                    throw new SQLException("No se cargó el comprobante " + idFc);
                }
            }
        }
    }

    public static List<Folio> loadFolios(int idFc) throws SQLException {
        List<Folio> folios = new ArrayList<>();
        String sql = 
                "SELECT "
                +       "CASE WHEN rm.id IS NULL THEN CASE WHEN va.id IS NULL THEN 'OT' ELSE 'VA' END ELSE 'RM' END tipo, "
                +       "IFNULL( rm.id, IFNULL( va.id, 0 ) ) folio, "
                +       "IFNULL( rm.uuid, IFNULL( va.uuid, fc.uuid ) ) uuid "
                + "FROM fc JOIN fcd ON fc.id = fcd.id JOIN inv ON inv.id = fcd.producto "
                + "LEFT JOIN rm ON rm.id = fcd.ticket AND inv.rubro = 'Combustible' "
                + "LEFT JOIN vtaditivos va ON va.id = fcd.ticket AND inv.rubro = 'Aceites' "
                + "WHERE fc.id = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, idFc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    folios.add(new Folio(Folio.TipoFolio.valueOf(rs.getString("tipo")), rs.getInt("folio"), rs.getString("uuid")));
                } else {
                    throw new SQLException("No se cargó el comprobante " + idFc);
                }
            }
        }
        return folios;
    }

    private static DocType parseDocType(int tabla) {
        switch(tabla) {
            case 1: return DocType.FA;
            case 2: return DocType.CR;
            case 3: return DocType.RP;
            case 4: return DocType.AN;
            case 5: return DocType.TR;
            case 6: return DocType.CPI;
            default:return DocType.FA;
        }
    }

    public static ComprobanteDetisa loadCFDI(String uuid, String motivo, String sustitucion) throws SQLException {
        ComprobanteDetisa comprobante;
        String sql = "SELECT id_fc_fk, version, tabla, uuid, cfdi_xml FROM facturas WHERE uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    comprobante = new ComprobanteDetisaImp(
                                        rs.getInt("id_fc_fk"), 
                                        Format.A1, 
                                        parseDocType(rs.getInt("tabla")), 
                                        new CFDIXmlResolver().resolve(rs.getString("cfdi_xml"), FacturadorOmicromContext.getInstance().getCfdiMapper()));
                    comprobante.setCancelation(CancelationDAO.getCancelation(uuid));
                    comprobante.setUuid(uuid);
                    return comprobante;
                }
            }
        } catch (Exception ex) {
            LogManager.error("No fue posible obtener el CFDI con uuid " + uuid, ex);
        }
        throw new SQLException("No fue posible obtener el CFDI con uuid " + uuid);        
    }

    public static ComprobanteDetisa loadCFDI(String uuid, Format format) throws FacturacionException, CFDIException {
        ComprobanteDetisa comprobante;
        String sql = "SELECT id_fc_fk, version, tabla, uuid, cfdi_xml FROM facturas WHERE uuid = ?";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setString(1, uuid);
            LogManager.debug(ps);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    comprobante = new ComprobanteDetisaImp(
                                        rs.getInt("id_fc_fk"), 
                                        format, 
                                        parseDocType(rs.getInt("tabla")), 
                                        new CFDIXmlResolver().resolve(
                                                rs.getString("cfdi_xml"), FacturadorOmicromContext.getInstance().getCfdiMapper()));
                    if ("XAXX010101000".equals(comprobante.getCfdi().getComprobante().getReceptor().getRfc()) && !"PUBLICO EN GENERAL".equals(comprobante.getCfdi().getComprobante().getReceptor().getNombre())) {
                        comprobante.setCliente(ClienteDAO.getOwner(uuid, comprobante.getDocType()));
                    } else {
                        comprobante.setCliente(ClienteDAO.getOwner(uuid, comprobante.getCfdi().getComprobante().getReceptor().getRegimenFiscalReceptor(), comprobante.getDocType()));
                    }
                    comprobante.setUuid(rs.getString("uuid"));
                    comprobante.setXml(rs.getBytes("cfdi_xml"));
                    LogManager.debug(comprobante);
                    return comprobante;
                }
            }
        } catch (SQLException ex) {
            LogManager.error("No fue posible obtener el CFDI con uuid " + uuid, ex);
        }
        throw new FacturacionException("No fue posible obtener el CFDI con uuid " + uuid);        
    }

    public static List<ComprobanteDetisa> loadCFDI(int cliente, Date inicio, Date fin) throws FacturacionException, CFDIException {
        List <ComprobanteDetisa> comprobantes = new ArrayList<>();
        ComprobanteDetisa comprobante;
        String sql = "SELECT f.id_fc_fk, f.tabla, f.uuid, f.cfdi_xml "
                + "FROM facturas f "
                + "JOIN ( "
                +       "SELECT cliente, uuid FROM fc WHERE fc.cliente = ? "
                +       "UNION ALL "
                +       "SELECT cliente, uuid FROM nc WHERE nc.cliente = ? "
                +       "UNION ALL "
                +       "SELECT cliente, uuid FROM pagos p WHERE p.cliente = ? ) docs USING( uuid ) "
        + "WHERE f.fecha_timbrado BETWEEN ? AND ? AND f.acuse_cancelacion IS NULL AND f.receptor <> 'XAXX010101000'";
        LogManager.info("Consultando CFDIs para el cliente " + cliente + " en el periodo " + inicio + "/" + fin);
        try (Connection connection = MySQLHelper.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setInt(1, cliente);
            ps.setInt(2, cliente);
            ps.setInt(3, cliente);
            ps.setTimestamp(4, new Timestamp(inicio.getTime()));
            ps.setTimestamp(5, new Timestamp(fin.getTime()));
            LogManager.debug(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comprobante = new ComprobanteDetisaImp(
                                        rs.getInt("id_fc_fk"), 
                                        Format.A1, 
                                        parseDocType(rs.getInt("tabla")), 
                                        new CFDIXmlResolver().resolve(rs.getString("cfdi_xml"), FacturadorOmicromContext.getInstance().getCfdiMapper()));
                    comprobante.setCliente(ClienteDAO.getOwner(rs.getString("uuid"), comprobante.getCfdi().getComprobante().getReceptor().getRegimenFiscalReceptor(), comprobante.getDocType()));
                    comprobante.setUuid(rs.getString("uuid"));
                    comprobante.setXml(rs.getBytes("cfdi_xml"));
                    comprobantes.add(comprobante);
                }
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return comprobantes;
    }

    public static List<ComprobanteDetisa> loadCFDI(Date inicio, Date fin) throws FacturacionException, CFDIException {
        List <ComprobanteDetisa> comprobantes = new ArrayList<>();
        ComprobanteDetisa comprobante;
        String sql = "SELECT f.id_fc_fk, f.tabla, f.uuid, f.cfdi_xml "
                + "FROM facturas f "
                + "WHERE f.fecha_timbrado BETWEEN ? AND ? AND f.acuse_cancelacion IS NULL AND f.receptor <> 'XAXX010101000'";
        try (Connection connection = MySQLHelper.getInstance().getConnection();
            PreparedStatement ps = connection.prepareStatement(sql)) {
            
            ps.setTimestamp(1, new Timestamp(inicio.getTime()));
            ps.setTimestamp(2, new Timestamp(fin.getTime()));
            LogManager.debug(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    comprobante = new ComprobanteDetisaImp(
                                        rs.getInt("id_fc_fk"), 
                                        Format.A1, parseDocType(rs.getInt("tabla")), 
                                        new CFDIXmlResolver().resolve(rs.getString("cfdi_xml"), FacturadorOmicromContext.getInstance().getCfdiMapper()));
                    comprobante.setCliente(ClienteDAO.getOwner(rs.getString("uuid"), comprobante.getCfdi().getComprobante().getReceptor().getRegimenFiscalReceptor(), comprobante.getDocType()));
                    comprobante.setUuid(rs.getString("uuid"));
                    comprobante.setXml(rs.getBytes("cfdi_xml"));
                    comprobantes.add(comprobante);
                }
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }
        return comprobantes;
    }

    private int reserveRMFacturaGlobal(Connection connection) throws SQLException {
        try(Statement st = connection.createStatement()) {

            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_RESERVE_RM));
        }
    }

    private int reserveRMFacturaGlobalGlobal(Connection connection) throws SQLException {
        try(Statement st = connection.createStatement()) {

            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_RESERVE_RMG));
        }
    }

    private int reserveVTAFacturaGlobal(Connection connection) throws SQLException {
        try(Statement st = connection.createStatement()) {

            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_RESERVE_VA));
        }
    }

    private int reserveVTAFacturaGlobalGlobal(Connection connection) throws SQLException {
        try(Statement st = connection.createStatement()) {

            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_RESERVE_VAG));
        }
    }

    private Comprobante.Conceptos cfdiConceptosFactura(int id) throws SQLException {
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos();
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(FacturaDAO.QRY.get(SQL_FC_CONCEPTOS))) {

            ps.setInt(1, id);
            LogManager.debug(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto(rs);
                    Comprobante.Conceptos.Concepto.Impuestos.Traslados traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados();
                    traslados.getTraslado().add(factory.createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIVA(rs));
                    if (BigDecimal.ZERO.compareTo(rs.getBigDecimal("tax_ieps"))<0) {
                        traslados.getTraslado().add(factory.createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIEPS(rs));
                    }
                    Comprobante.Conceptos.Concepto.Impuestos impuestos = factory.createComprobanteConceptosConceptoImpuestos();
                    impuestos.setTraslados(traslados);
                    concepto.setImpuestos(impuestos);
                    conceptos.getConcepto().add(concepto);
                }
            }
        }
        return conceptos;
    }

    private Comprobante.Conceptos cfdiConceptosFacturaPublicoEnGeneral(int id) {
        Comprobante.Conceptos conceptos = factory.createComprobanteConceptos();

        LogManager.info("Cargando conceptos fc.id = " + id);
        LogManager.debug(FacturaDAO.QRY.get(SQL_FCPG_CONCEPTOS));
        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(FacturaDAO.QRY.get(SQL_FCPG_CONCEPTOS))) {
            ps.setInt(1, id);
            LogManager.debug(ps);
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    Comprobante.Conceptos.Concepto concepto = factory.createComprobanteConceptosConcepto(rs);
                    Comprobante.Conceptos.Concepto.Impuestos.Traslados traslados = factory.createComprobanteConceptosConceptoImpuestosTraslados();
                    traslados.getTraslado().add(factory.createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIVA(rs));
                    if (BigDecimal.ZERO.compareTo(rs.getBigDecimal("tax_ieps"))<0) {
                        traslados.getTraslado().add(factory.createComprobanteConceptosConceptoImpuestosTrasladosTrasladoIEPS(rs));
                    }
                    Comprobante.Conceptos.Concepto.Impuestos impuestos = factory.createComprobanteConceptosConceptoImpuestos();
                    impuestos.setTraslados(traslados);
                    concepto.setImpuestos(impuestos);
                    conceptos.getConcepto().add(concepto);
                }
            }
        } catch (SQLException ex) {
            LogManager.error(ex);
        }

        return conceptos;
    }

    private Comprobante.Impuestos cfdiImpuestosFacturaPG(int id) {

        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos();
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados();
        Comprobante.Impuestos.Traslados.Traslado traslado;
        BigDecimal totalImpuestosTrasladados = BigDecimal.ZERO;

        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(FacturaDAO.QRY.get(SQL_FCPG_IMPUESTOS))) {

            ps.setInt(1, id);
            ps.setInt(2, id);
            LogManager.debug(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    traslado = factory.createComprobanteImpuestosTrasladosTraslado(rs);
                    if(BigDecimal.ZERO.compareTo(traslado.getImporte())<0) {
                        traslados.getTraslado().add(traslado);
                        totalImpuestosTrasladados = totalImpuestosTrasladados.add(traslado.getImporte());
                    }
                }
            }

            impuestos.setTraslados(traslados);
            impuestos.setTotalImpuestosTrasladados(totalImpuestosTrasladados.setScale(2, RoundingMode.HALF_EVEN));
        } catch (SQLException ex) {
            LogManager.error(ex);
        }

        return impuestos;
    }

    private Comprobante.Impuestos cfdiImpuestosFactura(int id) throws SQLException {
        Comprobante.Impuestos impuestos = factory.createComprobanteImpuestos();
        Comprobante.Impuestos.Traslados traslados = factory.createComprobanteImpuestosTraslados();
        Comprobante.Impuestos.Traslados.Traslado traslado;
        BigDecimal totalImpuestosTrasladados = BigDecimal.ZERO;

        try (Connection connection = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = connection.prepareStatement(FacturaDAO.QRY.get(SQL_FC_IMPUESTOS))) {

            ps.setInt(1, id);
            ps.setInt(2, id);
            LogManager.debug(ps);

            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    traslado = factory.createComprobanteImpuestosTrasladosTraslado(rs);
                    if(BigDecimal.ZERO.compareTo(traslado.getImporte())<0) {
                        traslados.getTraslado().add(traslado);
                        totalImpuestosTrasladados = totalImpuestosTrasladados.add(traslado.getImporte());
                    }
                }
            }
        }
        impuestos.setTraslados(traslados);
        impuestos.setTotalImpuestosTrasladados(totalImpuestosTrasladados.setScale(2, RoundingMode.HALF_EVEN));
        return impuestos;
    }

    private int createFacturaCliente(int cliente, String despachador, CFormaPago formadepago, Connection connection) throws SQLException {

        try(PreparedStatement psFc = connection.prepareStatement(FacturaDAO.QRY.get(SQL_CLIENTE_FP_FC), Statement.RETURN_GENERATED_KEYS)) {
            psFc.setString(1, formadepago.value());
            psFc.setString(2, despachador);
            psFc.setInt(3, cliente);
            LogManager.debug(psFc);
            psFc.execute();
            try (ResultSet rs = psFc.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        throw new SQLException("No se creó el comprobante para el cliente " + cliente);            
    }
    
    private boolean createFacturaDetallePosition(int id, int position, Connection connection) throws SQLException {

        try(PreparedStatement psFc = connection.prepareStatement(FacturaDAO.QRY.get(SQL_CONCEPTOS_POS))) {
            psFc.setInt(1, id);
            psFc.setInt(2, position);
            psFc.setInt(3, position);
            LogManager.debug(psFc);
            if (psFc.executeUpdate()>0) {
                return true;
           }
        }
        throw new SQLException("No se encontro el consmo en la posicion " + position + " o no puede ser facturado");
    }

    private boolean createFacturaPeripheralDetallePosition(int id, int position, Connection connection) throws SQLException {

        try(PreparedStatement psFc = connection.prepareStatement(FacturaDAO.QRY.get(SQL_CONCEPTOS_VTA_POS))) {
            psFc.setInt(1, id);
            psFc.setInt(2, position);
            LogManager.debug(psFc);
            if (psFc.executeUpdate()>0) {
                return true;
           }
        }
        throw new SQLException("No se encontro el consmo en la posicion " + position + " o no puede ser facturado");
    }

    private boolean createFacturaDetalleTransaccion(int id, int rmId, Connection connection) throws SQLException {

        try(PreparedStatement psFc = connection.prepareStatement(FacturaDAO.QRY.get(SQL_CONCEPTOS_POS_TR))) {
            psFc.setInt(1, id);
            psFc.setInt(2, rmId);
            psFc.setInt(3, rmId);
            LogManager.debug(psFc);
            if (psFc.executeUpdate()>0) {
                return true;
           }
        }
        throw new SQLException("No se encontro el consmo " + rmId + " o no puede ser facturado");
    }

    private int createFacturaPublicoEnGeneralGlobal(Connection connection) throws SQLException {

        try(Statement st = connection.createStatement()) {
            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_FCG));
        }
    }

    private int createFacturaPublicoEnGeneral(Connection connection) throws SQLException {

        try(Statement st = connection.createStatement()) {
            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_FC));
        }
    }

    private int createFacturaDetallePublicoEnGeneralGlobal(Connection connection) throws SQLException {

        try(Statement st = connection.createStatement()) {
            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_FCDG));
        }
    }

    private int createFacturaDetallePublicoEnGeneral(Connection connection) throws SQLException {

        try(Statement st = connection.createStatement()) {
            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_FCD));
        }
    }

    private int totalizaFacturaPublicoEnGeneral(Connection connection) throws SQLException {

        try(Statement st = connection.createStatement()) {
            return st.executeUpdate(FacturaDAO.QRY.get(SQL_GLOBAL_TOTALIZA));
        }
    }

    private boolean totalizaFactura(int id, Connection connection) throws SQLException {

        try(PreparedStatement psFc = connection.prepareStatement(FacturaDAO.QRY.get(SQL_FC_TOTALIZA))) {
            psFc.setInt(1, id);
            psFc.setInt(2, id);
            LogManager.debug(psFc);
            if (psFc.executeUpdate()>0) {
                return true;
           }
        }
        throw new SQLException("Error totalizando factura " + id);
    }

    public boolean cfdiPublicoEnGeneralVentanaGlobal() throws FacturacionException {

        int rmCount;
        int vtCount;
        int trCount;
        int fcCount;
        int fcdCount;
        int totalCount;

        try (Connection conn = MySQLHelper.getInstance().getConnection();
                AutoTransaction transaction = new AutoTransaction(conn)) {
            rmCount = reserveRMFacturaGlobalGlobal(conn);
            LogManager.info("Reservados " + rmCount + " registros de rm");
            vtCount = reserveVTAFacturaGlobalGlobal(conn);
            LogManager.info("Reservados " + vtCount + " registros de vtaditivos");
            trCount = rmCount + vtCount;
            if ( ( rmCount + vtCount )>0 ) {
                LogManager.info("Generando Facturas al Público en General. Registros rm " + rmCount + ". Registros vtaditivos " + vtCount + ". Registros Totales " + trCount);
                fcCount = createFacturaPublicoEnGeneralGlobal(conn);
                LogManager.info("Registros creados en fc " + fcCount);
                fcdCount = createFacturaDetallePublicoEnGeneralGlobal(conn);
                LogManager.info("Registros creados en fcd " + fcdCount);
                totalCount = totalizaFacturaPublicoEnGeneral(conn);
                LogManager.info("Registros totalizados " + totalCount);
            }
            transaction.commit();
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }

        LogManager.info("Cargando comprobantes al Público en General");
        return true;
    }

    public boolean cfdiPublicoEnGeneralVentana() throws FacturacionException {

        int rmCount;
        int vtCount;
        int trCount;

        try (Connection conn = MySQLHelper.getInstance().getConnection();
                AutoTransaction transaction = new AutoTransaction(conn)) {
            rmCount = reserveRMFacturaGlobal(conn);
            LogManager.info("Reservados " + rmCount + " registros de rm");
            vtCount = reserveVTAFacturaGlobal(conn);
            LogManager.info("Reservados " + vtCount + " registros de vtaditivos");
            trCount = rmCount + vtCount;
            if ( (rmCount + vtCount) > 0 ) {
                LogManager.info("Generando Facturas al Público en General. Registros rm " + rmCount + ". Registros vtaditivos " + vtCount + ". Registros Totales " + trCount);
                createFacturaPublicoEnGeneral(conn);
                createFacturaDetallePublicoEnGeneral(conn);
                totalizaFacturaPublicoEnGeneral(conn);
            }
            transaction.commit();
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }

        LogManager.info("Cargando comprobantes al Público en General");
        return true;
    }

    public ComprobanteDetisa cfdiPeripheralLastPosition(ComprobanteDetisa comprobante) throws FacturacionException {

        Comprobante.Receptor receptor = FacturaDAO.loadReceptor(comprobante.getCliente().getId(), CUsoCFDI.G_03.value());
        Comprobante.Emisor emisor = FacturaDAO.loadEmisor();
        Comprobante cfdi;

        int id;

        try (Connection conn = MySQLHelper.getInstance().getConnection();
                AutoTransaction transaction = new AutoTransaction(conn)) {
            LogManager.info("Querying folio position " + comprobante.getDespachador().getPosicion());
            id = createFacturaCliente(comprobante.getCliente().getId(), comprobante.getDespachador().getAlias(), comprobante.getCliente().getFormaPago(), conn);
            createFacturaPeripheralDetallePosition(id, comprobante.getDespachador().getPosicion(), conn);
            totalizaFactura(id, conn);
            transaction.commit();
        } catch (Exception ex) {
            LogManager.error("Error", ex);
            throw new FacturacionException(ex);
        }

        comprobante.setId(id);
        comprobante.setFormat(Format.TX);
        comprobante.setDocType(DocType.FA);

        try {
            LogManager.info("Cargando comprobante con id " + id);
            cfdi = FacturaDAO.loadComprobante(id);
            cfdi.setEmisor(emisor);
            cfdi.setReceptor(receptor);
            cfdi.setConceptos(cfdiConceptosFactura(comprobante.getId()));
            cfdi.setImpuestos(cfdiImpuestosFactura(comprobante.getId()));
            comprobante.setCfdi(factory.createCFDI(cfdi));
            LogManager.info("Comprobante " + comprobante);
            return comprobante;
        } catch (ReflectiveOperationException | JAXBException | CFDIException | SQLException ex) {
            throw new FacturacionException(ex);
        }
    }

    public ComprobanteDetisa cfdiLastPosition(ComprobanteDetisa comprobante) throws FacturacionException {

        Comprobante.Receptor receptor = FacturaDAO.loadReceptor(comprobante.getCliente().getId(), comprobante.getCliente().getUsoCFDI().value());
        Comprobante.Emisor emisor = FacturaDAO.loadEmisor();
        Comprobante cfdi;

        int id;

        try (Connection conn = MySQLHelper.getInstance().getConnection();
                AutoTransaction transaction = new AutoTransaction(conn)) {
            LogManager.info("Querying folio position " + comprobante.getDespachador().getPosicion());
            id = createFacturaCliente(comprobante.getCliente().getId(), comprobante.getDespachador().getAlias(), comprobante.getCliente().getFormaPago(), conn);
            createFacturaDetallePosition(id, comprobante.getDespachador().getPosicion(), conn);
            totalizaFactura(id, conn);
            transaction.commit();
        } catch (Exception ex) {
            LogManager.error("Error", ex);
            throw new FacturacionException(ex);
        }

        comprobante.setId(id);
        comprobante.setFormat(Format.TX);
        comprobante.setDocType(DocType.FA);

        try {
            LogManager.info("Cargando comprobante con id " + id);
            cfdi = FacturaDAO.loadComprobante(id);
            cfdi.setEmisor(emisor);
            cfdi.setReceptor(receptor);
            cfdi.setConceptos(cfdiConceptosFactura(comprobante.getId()));
            cfdi.setImpuestos(cfdiImpuestosFactura(comprobante.getId()));
            comprobante.setCfdi(factory.createCFDI(cfdi));
            LogManager.info("Comprobante " + comprobante);
            return comprobante;
        } catch (ReflectiveOperationException | JAXBException | CFDIException | SQLException ex) {
            throw new FacturacionException(ex);
        }
    }

    public ComprobanteDetisa cfdiTransaction(ComprobanteDetisa comprobante) throws FacturacionException {

        Comprobante.Receptor receptor = FacturaDAO.loadReceptor(comprobante.getCliente().getId(), comprobante.getCliente().getUsoCFDI().value());
        Comprobante.Emisor emisor = FacturaDAO.loadEmisor();
        Comprobante cfdi;

        int id;

        try (Connection conn = MySQLHelper.getInstance().getConnection();
                AutoTransaction transaction = new AutoTransaction(conn)) {
            LogManager.info("Querying folio position " + comprobante.getDespachador().getPosicion());
            id = createFacturaCliente(comprobante.getCliente().getId(), comprobante.getDespachador().getAlias(), comprobante.getCliente().getFormaPago(), conn);
            createFacturaDetalleTransaccion(id, comprobante.getRmId(), conn);
            totalizaFactura(id, conn);
            transaction.commit();
        } catch (Exception ex) {
            LogManager.error("Error", ex);
            throw new FacturacionException(ex);
        }

        comprobante.setId(id);
        comprobante.setFormat(Format.TX);
        comprobante.setDocType(DocType.FA);

        try {
            LogManager.info("Cargando comprobante con id " + id);
            cfdi = FacturaDAO.loadComprobante(id);
            cfdi.setEmisor(emisor);
            cfdi.setReceptor(receptor);
            cfdi.setConceptos(cfdiConceptosFactura(comprobante.getId()));
            cfdi.setImpuestos(cfdiImpuestosFactura(comprobante.getId()));
            comprobante.setCfdi(factory.createCFDI(cfdi));
            LogManager.info("Comprobante " + comprobante);
            return comprobante;
        } catch (ReflectiveOperationException | JAXBException | CFDIException | SQLException ex) {
            throw new FacturacionException(ex);
        }
    }
}
