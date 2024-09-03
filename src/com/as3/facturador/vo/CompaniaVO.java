/*
 * CompaniaVO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.vo;

import lombok.Data;

@Data
public class CompaniaVO {
    private int idfae;
    private String rfc;
    private String razonSocial;
    private String calle;
    private String exterior;
    private String interior;
    private String colonia;
    private String municipio;
    private String ciudad;
    private String estado;
    private String cp;
    private String telefono;
    private String email;
    private String regimen;
    private String regimenLetra;

    public String getDireccion() {
        return this.calle + " " + this.exterior + ", " + this.colonia + ". " + this.ciudad + " " + this.estado;
    }
}
