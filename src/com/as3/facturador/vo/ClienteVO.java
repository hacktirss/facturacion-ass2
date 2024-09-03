/*
 * ClienteVO
 * FacturadorOmicrom®
 * © 2019, DETI Desarrollo y Transferencia de Informática S.A. de C.V.
 * @author Rolando Esquivel Villafaña, Softcoatl
 * @version 1.0
 * @since nov 2019 
 */
package com.as3.facturador.vo;

import com.softcoatl.sat.cfdi.schema.CFormaPago;
import com.softcoatl.sat.cfdi.schema.CUsoCFDI;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClienteVO {
    private int id;
    private String rfc;
    private String razonSocial;
    private String regimenFiscalLetra;
    private String email;
    private String emailcc;
    private int facturacion;
    private boolean sendEmail;
    private CFormaPago formaPago;
    @Builder.Default private CUsoCFDI usoCFDI = CUsoCFDI.G_03;

    public boolean isInvalid() {
        return id <= 0;
    }

    public boolean isDisabled() {
        return facturacion == 0;
    }
}
