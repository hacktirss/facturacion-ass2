/*
 * DespachadorVO
 * FacturadorOmicrom®
 * © 2021, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since jun 2021 
 */
package com.as3.facturador.vo;

import java.sql.ResultSet;
import java.sql.SQLException;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class DespachadorVO {
    
    public static final String INVALID = "INCORRECTO";
    public static final String VALIDATED = "NO PIDE";
    
    private int id;
    private String nombre;
    private String alias;
    private String nip;
    private int posicion;
    private boolean activo;

    public static DespachadorVO parse(ResultSet rs) throws SQLException {
        return DespachadorVO.builder().id(rs.getInt("id")).nombre(rs.getString("nombre")).alias(rs.getString("alias")).nip(rs.getString("nip")).build();
    }

    public boolean isInvalid() {
        return INVALID.equals(nip);
    }
}
