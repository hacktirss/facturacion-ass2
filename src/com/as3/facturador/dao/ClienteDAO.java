/*
 * ClienteDAO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.dao;

import com.as3.facturador.exception.FacturacionException;
import com.as3.facturador.server.DocType;
import static com.as3.facturador.server.DocType.AN;
import static com.as3.facturador.server.DocType.CPI;
import static com.as3.facturador.server.DocType.CR;
import static com.as3.facturador.server.DocType.FA;
import static com.as3.facturador.server.DocType.FG;
import static com.as3.facturador.server.DocType.RP;
import static com.as3.facturador.server.DocType.TR;
import com.as3.facturador.vo.ClienteVO;
import com.softcoatl.database.mysql.MySQLHelper;
import com.softcoatl.sat.cfdi.schema.CFormaPago;
import com.softcoatl.sat.cfdi.schema.CUsoCFDI;
import com.softcoatl.utils.StringUtils;
import com.softcoatl.utils.logging.LogManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

public class ClienteDAO {

    private static final String SELF_FIELDS = "0 id, cia.rfc, cia.cia nombre, '01' fp, false sendEmail, '' email, '' emailcc, 1 facturacion, c.descripcion regimenFiscalLetra ";
    private static final String SELECTC = "SELECT "
            + "cli.id, cli.rfc, IFNULL( c.descripcion, '' ) regimenFiscalLetra, cli.nombre, IFNULL( cfdi.clave, '01' ) fp, cli.enviarcorreo = 'Si' sendEmail, "
            + "( CASE WHEN vc.valor = 1 THEN deencrypt_data( cli.correo ) ELSE cli.correo END ) email, cli.correo2 emailcc, cli.facturacion "
            + "FROM cli "
            + "JOIN ( SELECT IFNULL( ( SELECT valor FROM variables_corporativo WHERE llave = 'encrypt_fields' ), 0 ) valor ) vc ON TRUE "
            + "LEFT JOIN cfdi33_c_fpago cfdi ON cfdi.clave = cli.formadepago "
            + "LEFT JOIN cfdi33_c_regimenes c ON c.clave = cli.regimenFiscal ";
    private static final String SELECTF = "SELECT "
            + "cli.id, cli.rfc, c.descripcion regimenFiscalLetra, cli.nombre, IFNULL( cfdi.clave, '01' ) fp, cli.enviarcorreo = 'Si' sendEmail, "
            + "( CASE WHEN vc.valor = 1 THEN deencrypt_data( cli.correo ) ELSE cli.correo END ) email, cli.correo2 emailcc, cli.facturacion "
            + "FROM cli "
            + "JOIN ( SELECT IFNULL( ( SELECT valor FROM variables_corporativo WHERE llave = 'encrypt_fields' ), 0 ) valor ) vc ON TRUE "
            + "LEFT JOIN cfdi33_c_fpago cfdi ON cfdi.clave = cli.formadepago "
            + "LEFT JOIN cfdi33_c_regimenes c ON TRUE ";

    public static final class CustomerNotFoundException extends FacturacionException {
        public CustomerNotFoundException() {
            super("No se recuperaron los datos del CLIENTE");
        }
    }

    private ClienteDAO() {}

    private static ClienteVO parseCliente(ResultSet rs) throws SQLException {
        return ClienteVO.builder()
                    .id(rs.getInt("id"))
                    .rfc(rs.getString("rfc"))
                    .razonSocial(rs.getString("nombre"))
                    .regimenFiscalLetra(rs.getString("regimenFiscalLetra"))
                    .formaPago(CFormaPago.fromValue(rs.getString("fp")))
                    .email(rs.getString("email"))
                    .emailcc(rs.getString("emailcc"))
                    .emailcc(rs.getString("emailcc"))
                    .facturacion(rs.getInt("facturacion"))
                    .sendEmail(rs.getBoolean("sendEmail")).build();
    }

    public static ClienteVO get(int id) throws FacturacionException {
        String sql = ClienteDAO.SELECTC + " WHERE id = ?";
        LogManager.info(sql);
        LogManager.info("Cliente " + id);
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    public static ClienteVO get(int id, String formaPago, String usoCDFI) throws FacturacionException {
        ClienteVO cliente = get(id);
        cliente.setFormaPago(CFormaPago.fromValue(formaPago));
        if (!StringUtils.isNVL(usoCDFI)) {
            cliente.setUsoCFDI(CUsoCFDI.fromValue(usoCDFI));
        }
        return cliente;
    }

    public static ClienteVO get(String rfc) throws FacturacionException {
        String sql = ClienteDAO.SELECTC + " WHERE rfc = ?";
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, rfc);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    private static ClienteVO getSelf() throws FacturacionException {
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement("SELECT " + ClienteDAO.SELF_FIELDS + " FROM cia LEFT JOIN cfdi33_c_regimenes c ON cia.clave_regimen = c.clave")) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    private static String decodeTable(DocType type) {
        switch(type) {
            case FA:
            case FG: return "fc";
            case CR: return "nc";
            case RP:
            case AN: return "pagos";
            case TR: return "traslados";
            case CPI: return "ingresos";
            default: return "";
        }
    }

    private static ClienteVO getClient(int id, DocType type) throws FacturacionException {
        String table = ClienteDAO.decodeTable(type);
        String sql = ClienteDAO.SELECTC + " JOIN " + table + " ON " + table + ".cliente = cli.id WHERE " + table + ".id = ?";
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    private static ClienteVO getClient(int id, String regimen, DocType type) throws FacturacionException {
        String table = ClienteDAO.decodeTable(type);
        String sql = ClienteDAO.SELECTF + " JOIN " + table + " ON " + table + ".cliente = cli.id WHERE " + table + ".id = ? AND c.clave = ?";
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            if (regimen==null) {
                ps.setNull(2, Types.NULL);
            } else {
                ps.setString(2, regimen);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    private static ClienteVO getClient(String uuid, DocType type) throws FacturacionException {
        String table = ClienteDAO.decodeTable(type);
        String sql = ClienteDAO.SELECTC + " JOIN " + table + " ON " + table + ".cliente = cli.id WHERE " + table + ".uuid = ?";
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    private static ClienteVO getClient(String uuid, String regimen, DocType type) throws FacturacionException {
        String table = ClienteDAO.decodeTable(type);
        String sql = ClienteDAO.SELECTF + " JOIN " + table + " ON " + table + ".cliente = cli.id WHERE " + table + ".uuid = ? AND c.clave = ?";
        try (Connection conn = MySQLHelper.getInstance().getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, uuid);
            if (regimen==null) {
                ps.setNull(2, Types.NULL);
            } else {
                ps.setString(2, regimen);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return parseCliente(rs);
                }
            }
        } catch (SQLException ex) {
            throw new FacturacionException(ex);
        }
        throw new CustomerNotFoundException();
    }

    public static ClienteVO getOwner(int id, String regimen, DocType type) throws FacturacionException {
        return DocType.TR.equals(type) || DocType.CPI.equals(type) ? getSelf() : getClient(id, regimen, type);
    }

    public static ClienteVO getOwner(String uuid, String regimen, DocType type) throws FacturacionException {
        return DocType.TR.equals(type) || DocType.CPI.equals(type) ? getSelf() : getClient(uuid, regimen, type);
    }

    public static ClienteVO getOwner(int id, DocType type) throws FacturacionException {
        return DocType.TR.equals(type) || DocType.CPI.equals(type) ? getSelf() : getClient(id, type);
    }

    public static ClienteVO getOwner(String uuid, DocType type) throws FacturacionException {
        return DocType.TR.equals(type) || DocType.CPI.equals(type) ? getSelf() : getClient(uuid, type);
    }
}
